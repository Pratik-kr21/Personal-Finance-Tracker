package com.finance.tracker.db;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MongoDBConnection {
    private static final String ENV_FILE = ".env";

    // Strictly dynamic: no hardcoded credentials or database URIs in code
    private static String connectionUri = "";
    private static String databaseName = "personal_finance";

    private static MongoClient mongoClient = null;
    private static MongoDatabase database = null;
    private static boolean isConnected = false;
    private static String lastErrorMessage = "";

    static {
        loadEnv();
    }

    public static synchronized void loadEnv() {
        Map<String, String> envMap = readEnvFile();

        // 1. Try reading from local .env file
        if (envMap.containsKey("MONGO_URI") && !envMap.get("MONGO_URI").trim().isEmpty()) {
            connectionUri = envMap.get("MONGO_URI").trim();
        } else if (System.getenv("MONGO_URI") != null && !System.getenv("MONGO_URI").trim().isEmpty()) {
            connectionUri = System.getenv("MONGO_URI").trim();
        } else {
            connectionUri = "";
        }

        if (envMap.containsKey("MONGO_DATABASE") && !envMap.get("MONGO_DATABASE").trim().isEmpty()) {
            databaseName = envMap.get("MONGO_DATABASE").trim();
        } else if (System.getenv("MONGO_DATABASE") != null && !System.getenv("MONGO_DATABASE").trim().isEmpty()) {
            databaseName = System.getenv("MONGO_DATABASE").trim();
        }
    }

    public static synchronized void saveConfig(String uri, String dbName) throws IOException {
        connectionUri = uri != null ? uri.trim() : "";
        databaseName = (dbName != null && !dbName.trim().isEmpty()) ? dbName.trim() : "personal_finance";

        Map<String, String> envMap = readEnvFile();
        envMap.put("MONGO_URI", connectionUri);
        envMap.put("MONGO_DATABASE", databaseName);

        writeEnvFile(envMap);

        // Force reconnection on next getDatabase() call
        disconnect();
    }

    public static Map<String, String> readEnvFile() {
        Map<String, String> map = new LinkedHashMap<>();
        File file = new File(ENV_FILE);
        if (!file.exists()) {
            return map;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eqIdx = line.indexOf('=');
                if (eqIdx > 0) {
                    String key = line.substring(0, eqIdx).trim();
                    String val = line.substring(eqIdx + 1).trim();
                    if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                        val = val.substring(1, val.length() - 1);
                    }
                    map.put(key, val);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read .env file: " + e.getMessage());
        }
        return map;
    }

    public static void writeEnvFile(Map<String, String> map) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ENV_FILE))) {
            pw.println("# MongoDB Atlas & Application Configuration");
            pw.println("# Keep this file secure and do not commit secrets to Git");
            pw.println();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                pw.println(entry.getKey() + "=" + entry.getValue());
            }
        }
    }

    public static String getEnv(String key, String defaultValue) {
        Map<String, String> map = readEnvFile();
        if (map.containsKey(key)) {
            return map.get(key);
        }
        String sys = System.getenv(key);
        return sys != null ? sys : defaultValue;
    }

    public static double getMonthlyBudget() {
        String val = getEnv("MONTHLY_BUDGET", "1500.00");
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return 1500.00;
        }
    }

    public static void setMonthlyBudget(double budget) {
        try {
            Map<String, String> map = readEnvFile();
            map.put("MONTHLY_BUDGET", String.format(Locale.US, "%.2f", budget));
            writeEnvFile(map);
        } catch (IOException e) {
            System.err.println("Could not save budget to .env: " + e.getMessage());
        }
    }

    public static synchronized MongoDatabase getDatabase() {
        if (database == null) {
            connect();
        }
        return database;
    }

    public static synchronized boolean connect() {
        if (connectionUri == null || connectionUri.trim().isEmpty()) {
            isConnected = false;
            lastErrorMessage = "MongoDB URI is not configured in .env. Click 'Configure Atlas' to connect.";
            return false;
        }

        try {
            disconnect();

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionUri))
                    .applyToClusterSettings(builder ->
                            builder.serverSelectionTimeout(4000, TimeUnit.MILLISECONDS))
                    .applyToSocketSettings(builder ->
                            builder.connectTimeout(4000, TimeUnit.MILLISECONDS)
                                   .readTimeout(4000, TimeUnit.MILLISECONDS))
                    .build();

            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(databaseName);

            // Ping database to verify connection
            database.runCommand(new Document("ping", 1));

            isConnected = true;
            lastErrorMessage = "";
            return true;
        } catch (Exception e) {
            isConnected = false;
            lastErrorMessage = maskSensitiveInfo(e.getMessage() != null ? e.getMessage() : e.toString());
            System.err.println("MongoDB Atlas connection failed: " + lastErrorMessage);
            return false;
        }
    }

    public static synchronized void disconnect() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
            } catch (Exception ignored) {
            }
            mongoClient = null;
            database = null;
            isConnected = false;
        }
    }

    /**
     * Masks any password or credentials inside connection strings so they are never leaked in logs or error dialogs.
     */
    public static String maskSensitiveInfo(String input) {
        if (input == null) return "";
        // Match ://username:password@
        Pattern p = Pattern.compile("(://[^:]+:)([^@]+)(@)");
        Matcher m = p.matcher(input);
        if (m.find()) {
            return m.replaceAll("$1********$3");
        }
        return input;
    }

    public static String getMaskedConnectionUri() {
        return maskSensitiveInfo(connectionUri);
    }

    public static boolean isConnected() {
        return isConnected;
    }

    public static String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public static String getConnectionUri() {
        return connectionUri;
    }

    public static String getDatabaseName() {
        return databaseName;
    }
}
