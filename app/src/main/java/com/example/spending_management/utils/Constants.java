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

public class Constants {
    public static String INCOME = "INCOME";
    public static String EXPENSE = "EXPENSE";

    public static ArrayList<Category> categories;

    public static int DAILY = 0;
    public static int MONTHLY = 1;
    public static int CALENDAR = 2;
    public static int SELECTED_TAB = 0;
    public static int SELECTED_TAB_STATS = 0;
    public static String SELECTED_STATS_TYPE = Constants.INCOME;
    public static void setCategories() {
        categories = new ArrayList<>();
        categories.add(new Category("Salary", R.drawable.ic_salary, R.color.category1));
        categories.add(new Category("Business", R.drawable.ic_business, R.color.category2));
        categories.add(new Category("Investment", R.drawable.ic_investment, R.color.category3));
        categories.add(new Category("Loan", R.drawable.ic_loan, R.color.category4));
        categories.add(new Category("Rent", R.drawable.ic_rent, R.color.category5));
        categories.add(new Category("Other", R.drawable.ic_other, R.color.category6));
    }

    public static Category getCategoryDetails(String categoryName) {
        for (Category cat :
                categories) {
            if (cat.getCategoryName().equals(categoryName)) {
                return cat;
            }
        }
        return null;
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
