package com.example.spending_management.utils;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.spending_management.R;
import com.example.spending_management.models.Account;
import com.example.spending_management.models.Category;
import com.example.spending_management.models.Transaction;
import com.example.spending_management.views.activities.MainActivity;
import com.example.spending_management.views.fragments.SettingsFragment;
import com.example.spending_management.views.fragments.TransactionsFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Constants {
    public static String INCOME = "INCOME";
    public static String EXPENSE = "EXPENSE";
    private static final String SETTINGS_PREFS = "settings";
    private static final String EXPENSE_CATEGORIES_KEY = "expense_categories";
    private static final String INCOME_CATEGORIES_KEY = "income_categories";
    private static final String ACCOUNTS_KEY = "accounts";

    public static ArrayList<Category> categories;
    public static ArrayList<Category> expenseCategories;
    public static ArrayList<Category> incomeCategories;
    public static ArrayList<Account> accounts;

    public static int DAILY = 0;
    public static int MONTHLY = 1;
    public static int CALENDAR = 2;
    public static int SELECTED_TAB = 0;
    public static int SELECTED_TAB_STATS = 0;
    public static String SELECTED_STATS_TYPE = Constants.INCOME;
    public static void setCategories() {
        setDefaultCategories();
    }

    public static void setCategories(android.content.Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SETTINGS_PREFS, android.content.Context.MODE_PRIVATE);
        expenseCategories = buildCategories(
                getSavedCategories(preferences, EXPENSE_CATEGORIES_KEY, "Mua sắm|Đồ ăn|Điện thoại|Giải trí|Giáo dục|Sức khỏe|Đi lại|Nhà ở|Khác"),
                true
        );
        incomeCategories = buildCategories(
                getSavedCategories(preferences, INCOME_CATEGORIES_KEY, "Lương|Khoản đầu tư|Làm thêm|Tiền thưởng|Khác"),
                false
        );
        categories = expenseCategories;
        accounts = buildAccounts(getSavedCategories(preferences, ACCOUNTS_KEY, "Cash|Bank|Pay pal|Viettel Money|Other"));
    }

    private static String getSavedCategories(SharedPreferences preferences, String key, String fallback) {
        Set<String> savedSet = null;
        try {
            savedSet = preferences.getStringSet(key, null);
        } catch (ClassCastException ignored) {
            return preferences.getString(key, fallback);
        }
        if (savedSet != null) {
            ArrayList<String> ordered = new ArrayList<>(savedSet);
            ordered.sort((left, right) -> {
                int leftIndex = Integer.parseInt(left.substring(0, left.indexOf(":")));
                int rightIndex = Integer.parseInt(right.substring(0, right.indexOf(":")));
                return Integer.compare(leftIndex, rightIndex);
            });

            StringBuilder builder = new StringBuilder();
            for (String item : ordered) {
                int separator = item.indexOf(":");
                if (separator < 0) continue;
                if (builder.length() > 0) builder.append("|");
                builder.append(item.substring(separator + 1));
            }
            return builder.length() > 0 ? builder.toString() : fallback;
        }
        return preferences.getString(key, fallback);
    }

    private static void setDefaultCategories() {
        expenseCategories = buildCategories("Mua sắm|Đồ ăn|Điện thoại|Giải trí|Giáo dục|Sức khỏe|Đi lại|Nhà ở|Khác", true);
        incomeCategories = buildCategories("Lương|Khoản đầu tư|Làm thêm|Tiền thưởng|Khác", false);
        categories = expenseCategories;
        accounts = buildAccounts("Cash|Bank|Pay pal|Viettel Money|Other");
    }

    private static ArrayList<Account> buildAccounts(String rawAccounts) {
        ArrayList<Account> result = new ArrayList<>();
        String[] names = rawAccounts.split("\\|");
        for (String rawName : names) {
            String name = rawName.trim();
            if (name.length() == 0) continue;
            result.add(new Account(0, name));
        }
        if (result.isEmpty()) {
            result.add(new Account(0, "Cash"));
        }
        return result;
    }

    private static ArrayList<Category> buildCategories(String rawCategories, boolean expense) {
        ArrayList<Category> result = new ArrayList<>();
        String[] names = rawCategories.split("\\|");
        int[] colors = {R.color.categoryIconBlue};
        int[] expenseIcons = {
                R.drawable.ic_shopping,
                R.drawable.ic_food,
                R.drawable.ic_phone,
                R.drawable.ic_entertain,
                R.drawable.ic_educate,
                R.drawable.ic_health,
                R.drawable.ic_transport,
                R.drawable.ic_home,
                R.drawable.ic_different
        };
        int[] incomeIcons = {
                R.drawable.ic_salary,
                R.drawable.ic_investment,
                R.drawable.ic_business,
                R.drawable.ic_star,
                R.drawable.ic_different
        };

        for (int i = 0; i < names.length; i++) {
            String name = names[i].trim();
            if (name.length() == 0) continue;
            int[] icons = expense ? expenseIcons : incomeIcons;
            int icon = isOtherCategory(name) ? R.drawable.ic_different : icons[Math.min(i, icons.length - 1)];
            result.add(new Category(name, icon, colors[i % colors.length]));
        }
        return result;
    }

    private static boolean isOtherCategory(String categoryName) {
        return "Khác".equalsIgnoreCase(categoryName.trim())
                || "Other".equalsIgnoreCase(categoryName.trim());
    }

    public static ArrayList<Category> getCategories(String type) {
        if (expenseCategories == null || incomeCategories == null) {
            setDefaultCategories();
        }
        return INCOME.equals(type) ? incomeCategories : expenseCategories;
    }

    public static ArrayList<Account> getAccounts(android.content.Context context) {
        if (accounts == null) {
            setCategories(context);
        }
        return accounts;
    }

    public static void saveCategories(android.content.Context context, String type, ArrayList<Category> newCategories) {
        Set<String> savedSet = new HashSet<>();
        StringBuilder legacyBuilder = new StringBuilder();
        for (int i = 0; i < newCategories.size(); i++) {
            String categoryName = newCategories.get(i).getCategoryName();
            savedSet.add(i + ":" + categoryName);
            if (i > 0) legacyBuilder.append("|");
            legacyBuilder.append(categoryName);
        }
        context.getSharedPreferences(SETTINGS_PREFS, android.content.Context.MODE_PRIVATE)
                .edit()
                .putStringSet(INCOME.equals(type) ? INCOME_CATEGORIES_KEY : EXPENSE_CATEGORIES_KEY, savedSet)
                .putString((INCOME.equals(type) ? INCOME_CATEGORIES_KEY : EXPENSE_CATEGORIES_KEY) + "_legacy", legacyBuilder.toString())
                .apply();
        setCategories(context);
    }

    public static void saveAccounts(android.content.Context context, ArrayList<Account> newAccounts) {
        Set<String> savedSet = new HashSet<>();
        StringBuilder legacyBuilder = new StringBuilder();
        for (int i = 0; i < newAccounts.size(); i++) {
            String accountName = newAccounts.get(i).getAccount_name();
            savedSet.add(i + ":" + accountName);
            if (i > 0) legacyBuilder.append("|");
            legacyBuilder.append(accountName);
        }
        context.getSharedPreferences(SETTINGS_PREFS, android.content.Context.MODE_PRIVATE)
                .edit()
                .putStringSet(ACCOUNTS_KEY, savedSet)
                .putString(ACCOUNTS_KEY + "_legacy", legacyBuilder.toString())
                .apply();
        setCategories(context);
    }

    public static String accountDisplayName(String accountValue) {
        if ("Cash".equals(accountValue)) return "Tiền mặt";
        if ("Bank".equals(accountValue)) return "Ngân hàng";
        if ("Pay pal".equals(accountValue)) return "PayPal";
        if ("Viettel Money".equals(accountValue)) return "Viettel Money";
        if ("Other".equals(accountValue)) return "Khác";
        return accountValue;
    }

    public static Category getCategoryDetails(String categoryName) {
        if (expenseCategories == null || incomeCategories == null) {
            setDefaultCategories();
        }
        ArrayList<Category> allCategories = new ArrayList<>();
        allCategories.addAll(expenseCategories);
        allCategories.addAll(incomeCategories);
        for (Category cat :
                allCategories) {
            if (cat.getCategoryName().equals(categoryName)) {
                return cat;
            }
        }
        return new Category(categoryName, R.drawable.ic_different, R.color.categoryIconBlue);
    }
    public static int getAccountColor(String accountName){
        switch (accountName){
            case "Bank":
                return R.color.Bank;
            case "Cash":
                return R.color.Cash;
            case "Pay pal":
                return  R.color.MB_Bank;
            case "Viettel Monney":
                return R.color.Viettel_Money;
            case "Other":
                return R.color.Other;
            default:
                return R.color.redColor;
        }
    }


}
