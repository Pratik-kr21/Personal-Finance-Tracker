package com.finance.tracker.model;

import java.util.Arrays;
import java.util.List;

public class Category {
    public static final List<String> EXPENSE_CATEGORIES = Arrays.asList(
            "Food & Dining",
            "Groceries",
            "Rent & Housing",
            "Utilities & Bills",
            "Transportation",
            "Entertainment",
            "Shopping",
            "Healthcare & Fitness",
            "Education",
            "Travel",
            "Personal Care",
            "Other Expense"
    );

    public static final List<String> INCOME_CATEGORIES = Arrays.asList(
            "Salary",
            "Freelance / Contract",
            "Business",
            "Investments & Dividends",
            "Gifts & Grants",
            "Rental Income",
            "Refunds",
            "Other Income"
    );

    public static final List<String> PAYMENT_METHODS = Arrays.asList(
            "Cash",
            "Credit Card",
            "Debit Card",
            "Bank Transfer",
            "UPI / Mobile Wallet",
            "Cheque",
            "Other"
    );

    public static List<String> getCategoriesForType(TransactionType type) {
        if (type == TransactionType.INCOME) {
            return INCOME_CATEGORIES;
        } else {
            return EXPENSE_CATEGORIES;
        }
    }
}

