package com.finance.tracker.ui;

import com.finance.tracker.dao.TransactionDAO;
import com.finance.tracker.db.MongoDBConnection;
import com.finance.tracker.model.Category;
import com.finance.tracker.model.Transaction;
import com.finance.tracker.model.TransactionType;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private final TransactionDAO dao;
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");

    // UI Components
    private JLabel balanceValueLabel;
    private JLabel incomeValueLabel;
    private JLabel expenseValueLabel;
    private JLabel dbStatusLabel;

    // Monthly Budget Components
    private JLabel budgetValueLabel;
    private JLabel budgetRemainingLabel;
    private JProgressBar budgetProgressBar;
    private JLabel budgetProgressLabel;

    private JComboBox<String> typeFilterCombo;
    private JComboBox<String> categoryFilterCombo;
    private JTextField searchField;

    private JTable transactionTable;
    private DefaultTableModel tableModel;

    // Cache of loaded transactions for quick filtering
    private List<Transaction> currentTransactions = new ArrayList<>();

    public MainFrame() {
        super("Personal Finance Tracker (MongoDB Cloud)");
        this.dao = new TransactionDAO();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(850, 580));
        setLocationRelativeTo(null);

        // Main layout
        JPanel contentPane = new JPanel(new BorderLayout(0, 10));
        contentPane.setBackground(new Color(245, 247, 250));
        contentPane.setBorder(new EmptyBorder(12, 16, 12, 16));
        setContentPane(contentPane);

        // 1. Top Section: Header, Stat Cards, & Budget Progress Bar
        JPanel topContainer = new JPanel(new BorderLayout(0, 10));
        topContainer.setOpaque(false);
        topContainer.add(createHeaderPanel(), BorderLayout.NORTH);
        topContainer.add(createStatsPanel(), BorderLayout.CENTER);
        topContainer.add(createBudgetProgressBanner(), BorderLayout.SOUTH);
        contentPane.add(topContainer, BorderLayout.NORTH);

        // 2. Center Section: Filter bar + Table
        JPanel centerContainer = new JPanel(new BorderLayout(0, 8));
        centerContainer.setOpaque(false);
        centerContainer.add(createFilterPanel(), BorderLayout.NORTH);
        centerContainer.add(createTablePanel(), BorderLayout.CENTER);
        contentPane.add(centerContainer, BorderLayout.CENTER);

        // 3. Bottom Section: Action buttons
        contentPane.add(createActionPanel(), BorderLayout.SOUTH);

        // Initial Data Load
        refreshData();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel appTitle = new JLabel("💰 Personal Finance Tracker");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        appTitle.setForeground(new Color(30, 41, 59));

        dbStatusLabel = new JLabel("Checking MongoDB Atlas...");
        dbStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dbStatusLabel.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 224, 230), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));

        panel.add(appTitle, BorderLayout.WEST);
        panel.add(dbStatusLabel, BorderLayout.EAST);
        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        statsPanel.setOpaque(false);

        // Total Balance Card
        JPanel balanceCard = createStatCard("Total Net Balance", new Color(30, 41, 59));
        balanceValueLabel = new JLabel("$0.00");
        balanceValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        balanceValueLabel.setForeground(new Color(30, 41, 59));
        balanceCard.add(balanceValueLabel, BorderLayout.CENTER);

        // Income Card
        JPanel incomeCard = createStatCard("Total Income", new Color(16, 149, 93));
        incomeValueLabel = new JLabel("+$0.00");
        incomeValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        incomeValueLabel.setForeground(new Color(16, 149, 93));
        incomeCard.add(incomeValueLabel, BorderLayout.CENTER);

        // Expense Card
        JPanel expenseCard = createStatCard("Total Expenses", new Color(220, 38, 38));
        expenseValueLabel = new JLabel("-$0.00");
        expenseValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        expenseValueLabel.setForeground(new Color(220, 38, 38));
        expenseCard.add(expenseValueLabel, BorderLayout.CENTER);

        // Monthly Budget Card
        JPanel budgetCard = createStatCard("Monthly Budget", new Color(59, 130, 246));
        budgetValueLabel = new JLabel("$" + currencyFormat.format(MongoDBConnection.getMonthlyBudget()));
        budgetValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        budgetValueLabel.setForeground(new Color(59, 130, 246));

        budgetRemainingLabel = new JLabel("Remaining: $0.00");
        budgetRemainingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        budgetRemainingLabel.setForeground(new Color(100, 116, 139));

        JPanel budgetCenter = new JPanel(new BorderLayout(0, 2));
        budgetCenter.setOpaque(false);
        budgetCenter.add(budgetValueLabel, BorderLayout.NORTH);
        budgetCenter.add(budgetRemainingLabel, BorderLayout.SOUTH);
        budgetCard.add(budgetCenter, BorderLayout.CENTER);

        statsPanel.add(balanceCard);
        statsPanel.add(incomeCard);
        statsPanel.add(expenseCard);
        statsPanel.add(budgetCard);
        return statsPanel;
    }

    private JPanel createBudgetProgressBanner() {
        JPanel banner = new JPanel(new BorderLayout(14, 0));
        banner.setBackground(Color.WHITE);
        banner.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));

        budgetProgressLabel = new JLabel("📅 Budget Usage: $0.00 / $1,500.00 (0.0%)");
        budgetProgressLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        budgetProgressLabel.setForeground(new Color(71, 85, 105));

        budgetProgressBar = new JProgressBar(0, 100);
        budgetProgressBar.setStringPainted(true);
        budgetProgressBar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        budgetProgressBar.setForeground(new Color(16, 149, 93));
        budgetProgressBar.setPreferredSize(new Dimension(320, 22));

        JButton setBudgetBtn = new JButton("✏️ Set Monthly Budget");
        setBudgetBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        setBudgetBtn.setFocusPainted(false);
        setBudgetBtn.addActionListener(e -> openSetBudgetDialog());

        banner.add(budgetProgressLabel, BorderLayout.WEST);
        banner.add(budgetProgressBar, BorderLayout.CENTER);
        banner.add(setBudgetBtn, BorderLayout.EAST);
        return banner;
    }

    private JPanel createStatCard(String title, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(new Color(100, 116, 139));
        card.add(titleLbl, BorderLayout.NORTH);

        return card;
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterPanel.setOpaque(false);

        JLabel filterLbl = new JLabel("Filters:");
        filterLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Type filter
        typeFilterCombo = new JComboBox<>(new String[]{"All Types", "Income Only", "Expense Only"});
        typeFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        typeFilterCombo.addActionListener(e -> applyFilter());

        // Category filter
        categoryFilterCombo = new JComboBox<>();
        categoryFilterCombo.addItem("All Categories");
        for (String c : Category.EXPENSE_CATEGORIES) categoryFilterCombo.addItem(c);
        for (String c : Category.INCOME_CATEGORIES) {
            if (!Category.EXPENSE_CATEGORIES.contains(c)) categoryFilterCombo.addItem(c);
        }
        categoryFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        categoryFilterCombo.addActionListener(e -> applyFilter());

        // Search text
        searchField = new JTextField(14);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setToolTipText("Search note or payment method...");
        JButton searchBtn = new JButton("Search");
        searchBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchBtn.addActionListener(e -> applyFilter());

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resetBtn.addActionListener(e -> {
            typeFilterCombo.setSelectedIndex(0);
            categoryFilterCombo.setSelectedIndex(0);
            searchField.setText("");
            applyFilter();
        });

        filterPanel.add(filterLbl);
        filterPanel.add(new JLabel("Type:"));
        filterPanel.add(typeFilterCombo);
        filterPanel.add(new JLabel("Category:"));
        filterPanel.add(categoryFilterCombo);
        filterPanel.add(new JLabel("Keyword:"));
        filterPanel.add(searchField);
        filterPanel.add(searchBtn);
        filterPanel.add(resetBtn);

        return filterPanel;
    }

    private JPanel createTablePanel() {
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));

        String[] columns = {"Date", "Type", "Category", "Amount", "Payment Method", "Note / Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        transactionTable = new JTable(tableModel);
        transactionTable.setRowHeight(32);
        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.setShowVerticalLines(false);
        transactionTable.setGridColor(new Color(241, 245, 249));

        JTableHeader header = transactionTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        // Center align Date, Type, Payment
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        transactionTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        transactionTable.getColumnModel().getColumn(0).setPreferredWidth(95);

        // Custom renderer for Type & Amount to color-code
        transactionTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                c.setHorizontalAlignment(JLabel.CENTER);
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                if ("Income".equalsIgnoreCase(String.valueOf(value))) {
                    c.setForeground(new Color(16, 149, 93));
                } else {
                    c.setForeground(new Color(220, 38, 38));
                }
                return c;
            }
        });
        transactionTable.getColumnModel().getColumn(1).setPreferredWidth(85);

        // Amount Column
        transactionTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                c.setHorizontalAlignment(JLabel.RIGHT);
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                String text = String.valueOf(value);
                if (text.startsWith("+")) {
                    c.setForeground(new Color(16, 149, 93));
                } else {
                    c.setForeground(new Color(220, 38, 38));
                }
                return c;
            }
        });
        transactionTable.getColumnModel().getColumn(3).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        return tableContainer;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        // Left Actions
        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.setOpaque(false);

        JButton addBtn = new JButton("+ Add Transaction");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addBtn.setBackground(new Color(30, 41, 59));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setPreferredSize(new Dimension(160, 36));
        addBtn.addActionListener(e -> openAddDialog());

        JButton deleteBtn = new JButton("🗑 Delete Selected");
        deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        deleteBtn.setPreferredSize(new Dimension(140, 36));
        deleteBtn.addActionListener(e -> deleteSelectedTransaction());

        JButton analyticsBtn = new JButton("📊 View Analytics");
        analyticsBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        analyticsBtn.setPreferredSize(new Dimension(140, 36));
        analyticsBtn.addActionListener(e -> openAnalytics());

        leftActions.add(addBtn);
        leftActions.add(deleteBtn);
        leftActions.add(analyticsBtn);

        // Right Actions
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        JButton atlasConfigBtn = new JButton("☁ Configure Atlas");
        atlasConfigBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        atlasConfigBtn.setPreferredSize(new Dimension(140, 36));
        atlasConfigBtn.addActionListener(e -> openAtlasConfig());

        JButton exportBtn = new JButton("📥 Export CSV");
        exportBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        exportBtn.setPreferredSize(new Dimension(120, 36));
        exportBtn.addActionListener(e -> exportToCsv());

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        refreshBtn.setPreferredSize(new Dimension(100, 36));
        refreshBtn.addActionListener(e -> refreshData());

        rightActions.add(atlasConfigBtn);
        rightActions.add(exportBtn);
        rightActions.add(refreshBtn);

        panel.add(leftActions, BorderLayout.WEST);
        panel.add(rightActions, BorderLayout.EAST);
        return panel;
    }

    public void refreshData() {
        // Run database fetch asynchronously so UI never hangs
        SwingWorker<List<Transaction>, Void> worker = new SwingWorker<List<Transaction>, Void>() {
            private boolean connected = false;

            @Override
            protected List<Transaction> doInBackground() {
                connected = MongoDBConnection.connect();
                return dao.getAll();
            }

            @Override
            protected void done() {
                try {
                    currentTransactions = get();
                    updateDbStatusBadge(connected);
                    updateStats();
                    applyFilter();
                } catch (Exception e) {
                    System.err.println("Error refreshing data: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateDbStatusBadge(boolean connected) {
        if (connected) {
            dbStatusLabel.setText("🟢 MongoDB Atlas: Connected (" + MongoDBConnection.getDatabaseName() + ")");
            dbStatusLabel.setForeground(new Color(16, 149, 93));
        } else {
            dbStatusLabel.setText("🟡 MongoDB: Local Backup Mode (Click 'Configure Atlas')");
            dbStatusLabel.setForeground(new Color(180, 100, 0));
        }
    }

    private void updateStats() {
        double totalIncome = 0;
        double totalExpense = 0;
        double currentMonthExpense = 0;

        java.time.LocalDate now = java.time.LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        for (Transaction t : currentTransactions) {
            if (t.getType() == TransactionType.INCOME) {
                totalIncome += t.getAmount();
            } else {
                totalExpense += t.getAmount();
                if (t.getDate().getMonthValue() == currentMonth && t.getDate().getYear() == currentYear) {
                    currentMonthExpense += t.getAmount();
                }
            }
        }

        double netBalance = totalIncome - totalExpense;

        incomeValueLabel.setText("+$" + currencyFormat.format(totalIncome));
        expenseValueLabel.setText("-$" + currencyFormat.format(totalExpense));

        if (netBalance >= 0) {
            balanceValueLabel.setText("+$" + currencyFormat.format(netBalance));
            balanceValueLabel.setForeground(new Color(16, 149, 93));
        } else {
            balanceValueLabel.setText("-$" + currencyFormat.format(Math.abs(netBalance)));
            balanceValueLabel.setForeground(new Color(220, 38, 38));
        }

        // Budget Calculations
        double monthlyBudget = MongoDBConnection.getMonthlyBudget();
        budgetValueLabel.setText("$" + currencyFormat.format(monthlyBudget));

        double remaining = monthlyBudget - currentMonthExpense;
        double percent = (monthlyBudget > 0) ? (currentMonthExpense / monthlyBudget) * 100.0 : 0.0;

        if (remaining >= 0) {
            budgetRemainingLabel.setText("Left: $" + currencyFormat.format(remaining));
            budgetRemainingLabel.setForeground(new Color(16, 149, 93));
        } else {
            budgetRemainingLabel.setText("⚠️ Over: $" + currencyFormat.format(Math.abs(remaining)));
            budgetRemainingLabel.setForeground(new Color(220, 38, 38));
        }

        int progressVal = Math.min(100, (int) Math.round(percent));
        budgetProgressBar.setValue(progressVal);
        budgetProgressBar.setString(String.format(java.util.Locale.US, "%.1f%% used", percent));

        String monthName = now.getMonth().name().charAt(0) + now.getMonth().name().substring(1).toLowerCase();
        budgetProgressLabel.setText(String.format("📅 %s Spending: $%s of $%s budget",
                monthName,
                currencyFormat.format(currentMonthExpense),
                currencyFormat.format(monthlyBudget)));

        if (percent >= 100.0) {
            budgetProgressBar.setForeground(new Color(220, 38, 38)); // Red
        } else if (percent >= 75.0) {
            budgetProgressBar.setForeground(new Color(234, 115, 0)); // Amber
        } else {
            budgetProgressBar.setForeground(new Color(16, 149, 93)); // Green
        }
    }

    private void openSetBudgetDialog() {
        double current = MongoDBConnection.getMonthlyBudget();
        String input = JOptionPane.showInputDialog(this,
                "Enter your target monthly budget limit ($):",
                String.format(java.util.Locale.US, "%.2f", current));

        if (input != null && !input.trim().isEmpty()) {
            try {
                double val = Double.parseDouble(input.trim());
                if (val <= 0) {
                    JOptionPane.showMessageDialog(this, "Budget must be greater than zero.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                MongoDBConnection.setMonthlyBudget(val);
                updateStats();
                JOptionPane.showMessageDialog(this,
                        "Monthly budget updated to $" + currencyFormat.format(val),
                        "Budget Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void applyFilter() {
        tableModel.setRowCount(0);

        String typeSelection = (String) typeFilterCombo.getSelectedItem();
        String catSelection = (String) categoryFilterCombo.getSelectedItem();
        String keyword = searchField.getText().trim().toLowerCase();

        for (Transaction t : currentTransactions) {
            // Type check
            if ("Income Only".equals(typeSelection) && t.getType() != TransactionType.INCOME) {
                continue;
            }
            if ("Expense Only".equals(typeSelection) && t.getType() != TransactionType.EXPENSE) {
                continue;
            }

            // Category check
            if (catSelection != null && !"All Categories".equals(catSelection)) {
                if (!catSelection.equalsIgnoreCase(t.getCategory())) {
                    continue;
                }
            }

            // Keyword check
            if (!keyword.isEmpty()) {
                String note = t.getNote() != null ? t.getNote().toLowerCase() : "";
                String payment = t.getPaymentMethod() != null ? t.getPaymentMethod().toLowerCase() : "";
                String cat = t.getCategory() != null ? t.getCategory().toLowerCase() : "";
                if (!note.contains(keyword) && !payment.contains(keyword) && !cat.contains(keyword)) {
                    continue;
                }
            }

            // Amount formatted with +/-
            String formattedAmount = (t.getType() == TransactionType.INCOME ? "+$" : "-$") + currencyFormat.format(t.getAmount());

            tableModel.addRow(new Object[]{
                    t.getDate().format(Transaction.DATE_FORMATTER),
                    t.getType().getDisplayName(),
                    t.getCategory(),
                    formattedAmount,
                    t.getPaymentMethod(),
                    t.getNote()
            });
        }
    }

    private void openAddDialog() {
        AddTransactionDialog dialog = new AddTransactionDialog(this);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            Transaction t = dialog.getTransaction();
            boolean success = dao.insert(t);
            if (success) {
                refreshData();
                JOptionPane.showMessageDialog(this, "Transaction added successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save transaction.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to delete.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Find the matching transaction from filtered list
        int modelRow = transactionTable.convertRowIndexToModel(selectedRow);
        // Look up by matching date, type, category and amount
        String dateStr = (String) tableModel.getValueAt(modelRow, 0);
        String typeStr = (String) tableModel.getValueAt(modelRow, 1);
        String category = (String) tableModel.getValueAt(modelRow, 2);

        Transaction target = null;
        for (Transaction t : currentTransactions) {
            if (t.getDate().format(Transaction.DATE_FORMATTER).equals(dateStr)
                    && t.getType().getDisplayName().equals(typeStr)
                    && t.getCategory().equals(category)) {
                target = t;
                break;
            }
        }

        if (target != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this transaction:\n" + target.getCategory() + " (" + target.getAmount() + ") on " + target.getDate() + "?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean deleted = dao.delete(target.getId());
                if (deleted) {
                    refreshData();
                    JOptionPane.showMessageDialog(this, "Transaction deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete transaction.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void openAtlasConfig() {
        MongoSettingsDialog dialog = new MongoSettingsDialog(this, this::refreshData);
        dialog.setVisible(true);
    }

    private void openAnalytics() {
        if (currentTransactions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No transactions available to analyze. Add some transactions first!", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        AnalyticsDialog dialog = new AnalyticsDialog(this, currentTransactions);
        dialog.setVisible(true);
    }

    private void exportToCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("finance_statement_" + System.currentTimeMillis() + ".csv"));
        int ret = chooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("Date,Type,Category,Amount,Payment Method,Note");
                for (Transaction t : currentTransactions) {
                    pw.println(String.format("%s,%s,\"%s\",%.2f,\"%s\",\"%s\"",
                            t.getDate().format(Transaction.DATE_FORMATTER),
                            t.getType().name(),
                            t.getCategory(),
                            t.getAmount(),
                            t.getPaymentMethod() != null ? t.getPaymentMethod() : "",
                            t.getNote() != null ? t.getNote().replace("\"", "\"\"") : ""
                    ));
                }
                JOptionPane.showMessageDialog(this, "Transactions exported successfully to:\n" + file.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

