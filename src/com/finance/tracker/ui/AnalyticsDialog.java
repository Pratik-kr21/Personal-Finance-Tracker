package com.finance.tracker.ui;

import com.finance.tracker.model.Transaction;
import com.finance.tracker.model.TransactionType;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class AnalyticsDialog extends JDialog {
    private final List<Transaction> transactions;
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private final DecimalFormat percentFormat = new DecimalFormat("0.0%");

    private final Color[] PALETTE = {
            new Color(59, 130, 246),   // Blue
            new Color(16, 185, 129),  // Emerald
            new Color(245, 158, 11),  // Amber
            new Color(239, 68, 68),   // Red
            new Color(139, 92, 246),  // Purple
            new Color(236, 72, 153),  // Pink
            new Color(14, 165, 233),  // Sky
            new Color(249, 115, 22),  // Orange
            new Color(20, 184, 166),  // Teal
            new Color(107, 114, 128)  // Gray
    };

    private JComboBox<String> typeSelector;
    private PieChartPanel chartPanel;
    private DefaultTableModel tableModel;

    public AnalyticsDialog(Frame parent, List<Transaction> transactions) {
        super(parent, "Spending & Income Analytics", true);
        this.transactions = transactions;

        setSize(780, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 41, 59));
        headerPanel.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel("📊 Category Analytics & Visual Breakdown");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(Color.WHITE);

        typeSelector = new JComboBox<>(new String[]{"Expense Breakdown", "Income Breakdown"});
        typeSelector.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        typeSelector.addActionListener(e -> updateData());

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(typeSelector, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Split: Left = Pie Chart, Right = Breakdown Table
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 14, 0));
        centerPanel.setBorder(new EmptyBorder(10, 16, 10, 16));
        centerPanel.setBackground(new Color(245, 247, 250));

        // 1. Chart Panel
        chartPanel = new PieChartPanel();
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        centerPanel.add(chartPanel);

        // 2. Table Panel
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(6, 6, 6, 6)
        ));

        String[] cols = {"Category", "Total (₹)", "Share (%)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(241, 245, 249));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(tableContainer);

        add(centerPanel, BorderLayout.CENTER);

        // Footer Close Button
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);
        add(footer, BorderLayout.SOUTH);

        // Initial render
        updateData();
    }

    private void updateData() {
        TransactionType targetType = typeSelector.getSelectedIndex() == 0 ? TransactionType.EXPENSE : TransactionType.INCOME;

        Map<String, Double> categoryTotals = new LinkedHashMap<>();
        double totalSum = 0;

        for (Transaction t : transactions) {
            if (t.getType() == targetType) {
                categoryTotals.put(t.getCategory(), categoryTotals.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
                totalSum += t.getAmount();
            }
        }

        // Sort descending by amount
        List<Map.Entry<String, Double>> sortedList = new ArrayList<>(categoryTotals.entrySet());
        sortedList.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // Populate Table
        tableModel.setRowCount(0);
        for (Map.Entry<String, Double> entry : sortedList) {
            double share = (totalSum > 0) ? (entry.getValue() / totalSum) : 0;
            tableModel.addRow(new Object[]{
                    entry.getKey(),
                    "₹" + currencyFormat.format(entry.getValue()),
                    percentFormat.format(share)
            });
        }

        // Update Chart
        chartPanel.setData(sortedList, totalSum, targetType);
    }

    private class PieChartPanel extends JPanel {
        private List<Map.Entry<String, Double>> data = new ArrayList<>();
        private double total = 0;
        private TransactionType type = TransactionType.EXPENSE;

        public void setData(List<Map.Entry<String, Double>> data, double total, TransactionType type) {
            this.data = data;
            this.total = total;
            this.type = type;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            if (data.isEmpty() || total <= 0) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2.setColor(Color.GRAY);
                String msg = "No " + (type == TransactionType.EXPENSE ? "expense" : "income") + " transactions found.";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (width - fm.stringWidth(msg)) / 2, height / 2);
                g2.dispose();
                return;
            }

            // Draw Pie
            int diameter = Math.min(width - 40, height - 80);
            int x = (width - diameter) / 2;
            int y = 20;

            double currentAngle = 90;
            int colorIdx = 0;

            for (Map.Entry<String, Double> entry : data) {
                double arcAngle = (entry.getValue() / total) * 360.0;
                Color sliceColor = PALETTE[colorIdx % PALETTE.length];

                g2.setColor(sliceColor);
                g2.fillArc(x, y, diameter, diameter, (int) Math.round(currentAngle), (int) Math.round(arcAngle));

                // Border between slices
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawArc(x, y, diameter, diameter, (int) Math.round(currentAngle), (int) Math.round(arcAngle));

                currentAngle += arcAngle;
                colorIdx++;
            }

            // Draw donut hole for modern aesthetic
            int holeDiameter = (int) (diameter * 0.52);
            int holeX = x + (diameter - holeDiameter) / 2;
            int holeY = y + (diameter - holeDiameter) / 2;
            g2.setColor(Color.WHITE);
            g2.fillOval(holeX, holeY, holeDiameter, holeDiameter);

            // Center text
            g2.setColor(new Color(30, 41, 59));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            String totalTitle = "Total " + (type == TransactionType.EXPENSE ? "Spent" : "Income");
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(totalTitle, holeX + (holeDiameter - fm.stringWidth(totalTitle)) / 2, holeY + holeDiameter / 2 - 4);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            g2.setColor(type == TransactionType.EXPENSE ? new Color(220, 38, 38) : new Color(16, 149, 93));
            String amtStr = "₹" + currencyFormat.format(total);
            fm = g2.getFontMetrics();
            g2.drawString(amtStr, holeX + (holeDiameter - fm.stringWidth(amtStr)) / 2, holeY + holeDiameter / 2 + 16);

            g2.dispose();
        }
    }
}

