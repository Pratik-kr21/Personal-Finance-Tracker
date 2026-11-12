package com.finance.tracker.ui;

import com.finance.tracker.model.Category;
import com.finance.tracker.model.Transaction;
import com.finance.tracker.model.TransactionType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class AddTransactionDialog extends JDialog {
    private final JRadioButton expenseRadio;
    private final JRadioButton incomeRadio;
    private final JTextField amountField;
    private final JComboBox<String> categoryCombo;
    private final JTextField dateField;
    private final JComboBox<String> paymentMethodCombo;
    private final JTextField noteField;
    private boolean saved = false;
    private Transaction transaction;

    public AddTransactionDialog(Frame parent) {
        super(parent, "Add New Transaction", true);

        setSize(480, 480);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 76, 120));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("➕ Add New Transaction");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 4, 8, 4);

        // 1. Transaction Type (Income vs Expense)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        JLabel typeLbl = new JLabel("Type:");
        typeLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(typeLbl, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        expenseRadio = new JRadioButton("Expense", true);
        expenseRadio.setFont(new Font("Segoe UI", Font.BOLD, 13));
        expenseRadio.setForeground(new Color(180, 40, 40));

        incomeRadio = new JRadioButton("Income", false);
        incomeRadio.setFont(new Font("Segoe UI", Font.BOLD, 13));
        incomeRadio.setForeground(new Color(30, 130, 60));

        ButtonGroup group = new ButtonGroup();
        group.add(expenseRadio);
        group.add(incomeRadio);

        radioPanel.add(expenseRadio);
        radioPanel.add(incomeRadio);
        formPanel.add(radioPanel, gbc);

        // 2. Amount
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        JLabel amtLbl = new JLabel("Amount ($):");
        amtLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(amtLbl, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        amountField = new JTextField();
        amountField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(amountField, gbc);

        // 3. Category
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel catLbl = new JLabel("Category:");
        catLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(catLbl, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        categoryCombo = new JComboBox<>();
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(categoryCombo, gbc);

        // 4. Date
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        JLabel dateLbl = new JLabel("Date (YYYY-MM-DD):");
        dateLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(dateLbl, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        dateField = new JTextField(LocalDate.now().format(Transaction.DATE_FORMATTER));
        dateField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(dateField, gbc);

        // 5. Payment Method
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        JLabel payLbl = new JLabel("Payment Method:");
        payLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(payLbl, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        paymentMethodCombo = new JComboBox<>(Category.PAYMENT_METHODS.toArray(new String[0]));
        paymentMethodCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(paymentMethodCombo, gbc);

        // 6. Note
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.0;
        JLabel noteLbl = new JLabel("Note / Description:");
        noteLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(noteLbl, gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        noteField = new JTextField();
        noteField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(noteField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Listeners for type change to update categories
        expenseRadio.addActionListener(e -> updateCategoryList(TransactionType.EXPENSE));
        incomeRadio.addActionListener(e -> updateCategoryList(TransactionType.INCOME));
        updateCategoryList(TransactionType.EXPENSE);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        buttonPanel.setBorder(new EmptyBorder(0, 15, 10, 15));

        JButton saveBtn = new JButton("Save Transaction");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setBackground(new Color(24, 76, 120));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> onSave());

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void updateCategoryList(TransactionType type) {
        categoryCombo.removeAllItems();
        List<String> list = Category.getCategoriesForType(type);
        for (String c : list) {
            categoryCombo.addItem(c);
        }
    }

    private void onSave() {
        String amtStr = amountField.getText().trim();
        if (amtStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an amount.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amtStr);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be greater than zero.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number entered for amount.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateField.getText().trim(), Transaction.DATE_FORMATTER);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        TransactionType type = expenseRadio.isSelected() ? TransactionType.EXPENSE : TransactionType.INCOME;
        String category = (String) categoryCombo.getSelectedItem();
        String payment = (String) paymentMethodCombo.getSelectedItem();
        String note = noteField.getText().trim();

        this.transaction = new Transaction(type, category, amount, date, payment, note);
        this.saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    public Transaction getTransaction() {
        return transaction;
    }
}

