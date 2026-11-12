package com.finance.tracker.model;

public enum TransactionType {
    INCOME("Income"),
    EXPENSE("Expense");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TransactionType fromString(String text) {
        for (TransactionType b : TransactionType.values()) {
            if (b.name().equalsIgnoreCase(text) || b.displayName.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return EXPENSE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

