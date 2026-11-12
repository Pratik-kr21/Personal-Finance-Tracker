package com.finance.tracker.dao;

import com.finance.tracker.db.MongoDBConnection;
import com.finance.tracker.model.Transaction;
import com.finance.tracker.model.TransactionType;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class TransactionDAO {
    private static final String COLLECTION_NAME = "transactions";
    private static final String FALLBACK_FILE = "transactions_local.csv";
    private final List<Transaction> fallbackCache = new ArrayList<>();

    public TransactionDAO() {
        loadFallbackFromFile();
    }

    private MongoCollection<Document> getCollection() {
        try {
            MongoDatabase db = MongoDBConnection.getDatabase();
            if (db != null && MongoDBConnection.isConnected()) {
                return db.getCollection(COLLECTION_NAME);
            }
        } catch (Exception e) {
            System.err.println("Database access error: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(Transaction transaction) {
        MongoCollection<Document> collection = getCollection();
        if (collection != null) {
            try {
                Document doc = new Document()
                        .append("type", transaction.getType().name())
                        .append("category", transaction.getCategory())
                        .append("amount", transaction.getAmount())
                        .append("date", transaction.getDate().format(Transaction.DATE_FORMATTER))
                        .append("paymentMethod", transaction.getPaymentMethod())
                        .append("note", transaction.getNote());

                collection.insertOne(doc);
                ObjectId id = doc.getObjectId("_id");
                if (id != null) {
                    transaction.setId(id.toHexString());
                }
                return true;
            } catch (Exception e) {
                System.err.println("Mongo insert error: " + e.getMessage());
            }
        }

        // Fallback local persistence if MongoDB is not available
        if (transaction.getId() == null || transaction.getId().isEmpty()) {
            transaction.setId(UUID.randomUUID().toString());
        }
        fallbackCache.add(transaction);
        saveFallbackToFile();
        return true;
    }

    public List<Transaction> getAll() {
        MongoCollection<Document> collection = getCollection();
        if (collection != null) {
            try {
                List<Transaction> list = new ArrayList<>();
                FindIterable<Document> docs = collection.find().sort(new Document("date", -1));
                for (Document doc : docs) {
                    list.add(documentToTransaction(doc));
                }
                return list;
            } catch (Exception e) {
                System.err.println("Mongo fetch error: " + e.getMessage());
            }
        }
        // Fallback
        List<Transaction> copy = new ArrayList<>(fallbackCache);
        copy.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        return copy;
    }

    public boolean delete(String id) {
        if (id == null || id.isEmpty()) return false;

        MongoCollection<Document> collection = getCollection();
        if (collection != null) {
            try {
                if (ObjectId.isValid(id)) {
                    collection.deleteOne(new Document("_id", new ObjectId(id)));
                } else {
                    collection.deleteOne(new Document("_id", id));
                }
                return true;
            } catch (Exception e) {
                System.err.println("Mongo delete error: " + e.getMessage());
            }
        }

        boolean removed = fallbackCache.removeIf(t -> id.equals(t.getId()));
        if (removed) {
            saveFallbackToFile();
        }
        return removed;
    }

    public double getTotalIncome() {
        return getAll().stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpense() {
        return getAll().stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getNetBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    public Map<String, Double> getCategoryTotals(TransactionType type) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (Transaction t : getAll()) {
            if (t.getType() == type) {
                map.put(t.getCategory(), map.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
            }
        }
        return map;
    }

    private Transaction documentToTransaction(Document doc) {
        String id = "";
        Object rawId = doc.get("_id");
        if (rawId instanceof ObjectId) {
            id = ((ObjectId) rawId).toHexString();
        } else if (rawId != null) {
            id = rawId.toString();
        }

        TransactionType type = TransactionType.fromString(doc.getString("type"));
        String category = doc.getString("category");
        Number amt = (Number) doc.get("amount");
        double amount = amt != null ? amt.doubleValue() : 0.0;
        String dateStr = doc.getString("date");
        LocalDate date = (dateStr != null && !dateStr.isEmpty())
                ? LocalDate.parse(dateStr, Transaction.DATE_FORMATTER)
                : LocalDate.now();
        String payment = doc.getString("paymentMethod");
        String note = doc.getString("note");

        return new Transaction(id, type, category, amount, date, payment, note);
    }

    private void loadFallbackFromFile() {
        File f = new File(FALLBACK_FILE);
        if (!f.exists()) return;
        fallbackCache.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(";", -1);
                if (parts.length >= 7) {
                    Transaction t = new Transaction(
                            parts[0],
                            TransactionType.fromString(parts[1]),
                            parts[2],
                            Double.parseDouble(parts[3]),
                            LocalDate.parse(parts[4], Transaction.DATE_FORMATTER),
                            parts[5],
                            parts[6]
                    );
                    fallbackCache.add(t);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load local fallback transactions: " + e.getMessage());
        }
    }

    private void saveFallbackToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FALLBACK_FILE))) {
            for (Transaction t : fallbackCache) {
                pw.println(String.format("%s;%s;%s;%.2f;%s;%s;%s",
                        t.getId(),
                        t.getType().name(),
                        t.getCategory(),
                        t.getAmount(),
                        t.getDate().format(Transaction.DATE_FORMATTER),
                        t.getPaymentMethod() != null ? t.getPaymentMethod() : "",
                        t.getNote() != null ? t.getNote().replace(";", ",") : ""
                ));
            }
        } catch (Exception e) {
            System.err.println("Failed to save local fallback transactions: " + e.getMessage());
        }
    }
}

