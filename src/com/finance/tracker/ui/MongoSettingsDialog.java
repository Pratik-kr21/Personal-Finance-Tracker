package com.finance.tracker.ui;

import com.finance.tracker.db.MongoDBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MongoSettingsDialog extends JDialog {
    private final JTextField uriField;
    private final JTextField dbField;
    private final JLabel statusLabel;
    private final JButton testBtn;
    private final JButton saveBtn;
    private final Runnable onConnectionUpdated;

    public MongoSettingsDialog(Frame parent, Runnable onConnectionUpdated) {
        super(parent, "MongoDB Atlas Configuration", true);
        this.onConnectionUpdated = onConnectionUpdated;

        setSize(620, 360);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(19, 42, 69));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Connect to MongoDB Atlas");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Paste your MongoDB Atlas connection string below to sync your transactions in the cloud.");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(200, 215, 230));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        // Connection URI label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        JLabel uriLbl = new JLabel("MongoDB URI:");
        uriLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(uriLbl, gbc);

        // Connection URI field
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        uriField = new JTextField(MongoDBConnection.getConnectionUri());
        uriField.setFont(new Font("Consolas", Font.PLAIN, 12));
        uriField.setToolTipText("Paste your MongoDB connection string here");
        formPanel.add(uriField, gbc);

        // Hint under URI
        gbc.gridx = 1;
        gbc.gridy = 1;
        JLabel hintLabel = new JLabel("<html><i>Stored securely in your local .env file. Never exposed in code.</i></html>");
        hintLabel.setForeground(new Color(100, 116, 139));
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        formPanel.add(hintLabel, gbc);

        // Database Name label
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel dbLbl = new JLabel("Database Name:");
        dbLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(dbLbl, gbc);

        // Database Name field
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        dbField = new JTextField(MongoDBConnection.getDatabaseName());
        dbField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(dbField, gbc);

        // Status Label
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        statusLabel = new JLabel(MongoDBConnection.isConnected() ? "\u25CF Status: Connected" : "\u25CF Status: Not Connected");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        if (MongoDBConnection.isConnected()) {
            statusLabel.setForeground(new Color(34, 139, 34));
        } else {
            statusLabel.setForeground(new Color(180, 100, 0));
        }
        formPanel.add(statusLabel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.setBorder(new EmptyBorder(0, 15, 10, 15));

        testBtn = StyledButton.secondary("Test Connection");
        testBtn.setPreferredSize(new Dimension(140, 32));
        testBtn.addActionListener(e -> testConnection());

        saveBtn = StyledButton.primary("Save & Connect");
        saveBtn.setPreferredSize(new Dimension(140, 32));
        saveBtn.addActionListener(e -> saveAndConnect());

        JButton cancelBtn = StyledButton.secondary("Cancel");
        cancelBtn.setPreferredSize(new Dimension(90, 32));
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(testBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void testConnection() {
        String uri = uriField.getText().trim();
        String dbName = dbField.getText().trim();
        if (uri.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a MongoDB connection URI.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        testBtn.setEnabled(false);
        saveBtn.setEnabled(false);
        statusLabel.setText("⏳ Testing connection to MongoDB Atlas... please wait...");
        statusLabel.setForeground(new Color(70, 70, 70));

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            private String error = "";

            @Override
            protected Boolean doInBackground() {
                try {
                    MongoDBConnection.saveConfig(uri, dbName);
                    return MongoDBConnection.connect();
                } catch (Exception ex) {
                    error = ex.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                testBtn.setEnabled(true);
                saveBtn.setEnabled(true);
                try {
                    boolean success = get();
                    if (success) {
                        statusLabel.setText("\u25CF Successfully connected to MongoDB Atlas!");
                        statusLabel.setForeground(new Color(34, 139, 34));
                        JOptionPane.showMessageDialog(MongoSettingsDialog.this,
                                "Successfully connected to MongoDB Atlas!", "Connection Succeeded", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        statusLabel.setText("\u25CF Connection failed. Check URI and network.");
                        statusLabel.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(MongoSettingsDialog.this,
                                "Failed to connect to MongoDB:\n" + MongoDBConnection.getLastErrorMessage(),
                                "Connection Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    statusLabel.setText("\u25CF Error: " + ex.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        };
        worker.execute();
    }

    private void saveAndConnect() {
        String uri = uriField.getText().trim();
        String dbName = dbField.getText().trim();
        if (uri.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a MongoDB connection URI.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            MongoDBConnection.saveConfig(uri, dbName);
            boolean connected = MongoDBConnection.connect();
            if (onConnectionUpdated != null) {
                onConnectionUpdated.run();
            }
            if (connected) {
                JOptionPane.showMessageDialog(this, "MongoDB Atlas configuration saved and connected successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Configuration saved, but could not connect:\n" + MongoDBConnection.getLastErrorMessage() + "\n(Local backup is active in the meantime)", "Saved with Warning", JOptionPane.WARNING_MESSAGE);
            }
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving configuration: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

