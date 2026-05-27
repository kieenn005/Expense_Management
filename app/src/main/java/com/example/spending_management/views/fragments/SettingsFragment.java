package com.example.spending_management.views.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.spending_management.R;
import com.example.spending_management.models.Account;
import com.example.spending_management.models.Category;
import com.example.spending_management.models.Transaction;
import com.example.spending_management.utils.Constants;

import java.util.ArrayList;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

public class SettingsFragment extends Fragment {
    private TextView tvLanguage;
    private String checkerLanguage;

    public SettingsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_settings, container, false);

        tvLanguage = view.findViewById(R.id.tv_current_language);

        SharedPreferences preferences = requireActivity().getSharedPreferences("settings", getContext().MODE_PRIVATE);
        String currentLanguage = preferences.getString("language", "Tiếng Việt");
        tvLanguage.setText(currentLanguage);

        checkerLanguage = currentLanguage;

        tvLanguage.setOnClickListener(v -> showLanguageDialog(preferences));
        view.findViewById(R.id.layout_category).setOnClickListener(v -> showCategoryManager());
        view.findViewById(R.id.layout_account).setOnClickListener(v -> showAccountManager());

        return view;
    }

    private void showLanguageDialog(SharedPreferences preferences) {
        final String[] displayLanguages = {"Tiếng Việt", "English"};
        final String[] displayLanguageCodes = {"vi", "en"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn ngôn ngữ")
                .setItems(displayLanguages, (dialog, which) -> {
                    String selectedLanguage = displayLanguages[which];
                    String selectedLanguageCode = displayLanguageCodes[which];
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("language", selectedLanguage);
                    editor.putString("language_code", selectedLanguageCode);
                    editor.apply();
                    setLocale(selectedLanguageCode);
                    tvLanguage.setText(selectedLanguage);
                    if (!checkerLanguage.equals(selectedLanguage)) {
                        editor.putBoolean("checker", true);
                        editor.apply();
                    }
                    Toast.makeText(getContext(), "Đã đổi ngôn ngữ", Toast.LENGTH_SHORT).show();
                    requireActivity().recreate();
                })
                .show();
    }

    private void showCategoryManager() {
        Constants.setCategories(requireContext());

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(12), dp(16), dp(8));
        root.setBackgroundColor(requireContext().getColor(R.color.screenBackground));

        LinearLayout tabs = new LinearLayout(requireContext());
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setBackground(requireContext().getDrawable(R.drawable.summary_panel_bg));
        tabs.setPadding(dp(2), dp(2), dp(2), dp(2));

        TextView expenseTab = createManagerTab("Chi tiêu");
        TextView incomeTab = createManagerTab("Thu nhập");
        tabs.addView(expenseTab);
        tabs.addView(incomeTab);

        ScrollView scrollView = new ScrollView(requireContext());
        LinearLayout list = new LinearLayout(requireContext());
        list.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(list);

        TextView addButton = new TextView(requireContext());
        addButton.setText("+ Thêm danh mục");
        addButton.setGravity(android.view.Gravity.CENTER);
        addButton.setTextColor(requireContext().getColor(R.color.white));
        addButton.setTextSize(17);
        addButton.setTypeface(null, android.graphics.Typeface.BOLD);
        addButton.setBackground(requireContext().getDrawable(R.drawable.account_bg));
        addButton.setBackgroundTintList(requireContext().getColorStateList(R.color.orange));
        addButton.setPadding(0, dp(14), 0, dp(14));

        root.addView(tabs);
        root.addView(scrollView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(420)));
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addParams.setMargins(0, dp(10), 0, 0);
        root.addView(addButton, addParams);

        final String[] selectedType = {Constants.EXPENSE};
        Runnable[] render = new Runnable[1];
        render[0] = () -> {
            boolean incomeSelected = Constants.INCOME.equals(selectedType[0]);
            incomeTab.setBackground(incomeSelected ? requireContext().getDrawable(R.drawable.income_selector) : null);
            expenseTab.setBackground(!incomeSelected ? requireContext().getDrawable(R.drawable.expense_selector) : null);
            incomeTab.setTextColor(requireContext().getColor(incomeSelected ? R.color.greenColor : R.color.textSecondary));
            expenseTab.setTextColor(requireContext().getColor(!incomeSelected ? R.color.redColor : R.color.textSecondary));
            renderCategoryRows(list, selectedType[0], render[0]);
        };

        expenseTab.setOnClickListener(view -> {
            selectedType[0] = Constants.EXPENSE;
            render[0].run();
        });
        incomeTab.setOnClickListener(view -> {
            selectedType[0] = Constants.INCOME;
            render[0].run();
        });
        addButton.setOnClickListener(view -> showAddCategoryDialog(selectedType[0], render[0]));

        render[0].run();

        new AlertDialog.Builder(requireContext())
                .setTitle("Cài đặt danh mục")
                .setView(root)
                .setPositiveButton("Xong", null)
                .show();
    }

    private TextView createManagerTab(String text) {
        TextView tab = new TextView(requireContext());
        tab.setText(text);
        tab.setGravity(android.view.Gravity.CENTER);
        tab.setTextSize(16);
        tab.setTextColor(requireContext().getColor(R.color.textPrimary));
        tab.setPadding(0, dp(10), 0, dp(10));
        tab.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        return tab;
    }

    private void renderCategoryRows(LinearLayout list, String type, Runnable refresh) {
        list.removeAllViews();
        ArrayList<Category> categories = Constants.getCategories(type);
        for (Category category : categories) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, dp(10), 0, dp(10));

            TextView delete = new TextView(requireContext());
            delete.setText("-");
            delete.setGravity(android.view.Gravity.CENTER);
            delete.setTextSize(22);
            delete.setTextColor(requireContext().getColor(R.color.white));
            delete.setBackground(requireContext().getDrawable(R.drawable.category_bg));
            delete.setBackgroundTintList(requireContext().getColorStateList(R.color.redColor));
            row.addView(delete, new LinearLayout.LayoutParams(dp(34), dp(34)));

            ImageView icon = new ImageView(requireContext());
            icon.setImageResource(category.getCategory_image());
            icon.setColorFilter(requireContext().getColor(R.color.white));
            icon.setPadding(dp(12), dp(12), dp(12), dp(12));
            icon.setBackground(requireContext().getDrawable(R.drawable.category_bg));
            icon.setBackgroundTintList(requireContext().getColorStateList(category.getCategory_color()));
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(52), dp(52));
            iconParams.setMargins(dp(16), 0, dp(14), 0);
            row.addView(icon, iconParams);

            TextView name = new TextView(requireContext());
            name.setText(category.getCategoryName());
            name.setTextColor(requireContext().getColor(R.color.textPrimary));
            name.setTextSize(17);
            row.addView(name, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            delete.setOnClickListener(view -> {
                ArrayList<Category> updated = new ArrayList<>(Constants.getCategories(type));
                if (updated.size() <= 1) {
                    Toast.makeText(requireContext(), "Cần giữ ít nhất 1 danh mục", Toast.LENGTH_SHORT).show();
                    return;
                }
                updated.remove(category);
                Constants.saveCategories(requireContext(), type, updated);
                refresh.run();
            });
            list.addView(row);
        }
    }

    private void showAddCategoryDialog(String type, Runnable refresh) {
        EditText input = new EditText(requireContext());
        input.setHint("Tên danh mục");
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setPadding(dp(16), dp(8), dp(16), dp(8));

        new AlertDialog.Builder(requireContext())
                .setTitle("Thêm danh mục")
                .setView(input)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.length() == 0) return;
                    ArrayList<Category> updated = new ArrayList<>(Constants.getCategories(type));
                    int color = R.color.categoryIconBlue;
                    int insertIndex = updated.size();
                    for (int i = 0; i < updated.size(); i++) {
                        if ("Khác".equalsIgnoreCase(updated.get(i).getCategoryName())) {
                            insertIndex = i;
                            break;
                        }
                    }
                    updated.add(insertIndex, new Category(name, R.drawable.ic_different, color));
                    Constants.saveCategories(requireContext(), type, updated);
                    refresh.run();
                })
                .show();
    }

    private void showAccountManager() {
        Constants.setCategories(requireContext());

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(12), dp(16), dp(8));
        root.setBackgroundColor(requireContext().getColor(R.color.screenBackground));

        ScrollView scrollView = new ScrollView(requireContext());
        LinearLayout list = new LinearLayout(requireContext());
        list.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(list);

        TextView addButton = new TextView(requireContext());
        addButton.setText("+ Thêm tài khoản");
        addButton.setGravity(android.view.Gravity.CENTER);
        addButton.setTextColor(requireContext().getColor(R.color.white));
        addButton.setTextSize(17);
        addButton.setTypeface(null, android.graphics.Typeface.BOLD);
        addButton.setBackground(requireContext().getDrawable(R.drawable.account_bg));
        addButton.setBackgroundTintList(requireContext().getColorStateList(R.color.orange));
        addButton.setPadding(0, dp(14), 0, dp(14));

        root.addView(scrollView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(360)));
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addParams.setMargins(0, dp(10), 0, 0);
        root.addView(addButton, addParams);

        Runnable[] render = new Runnable[1];
        render[0] = () -> renderAccountRows(list, render[0]);
        addButton.setOnClickListener(view -> showAccountNameDialog(null, render[0]));
        render[0].run();

        new AlertDialog.Builder(requireContext())
                .setTitle("Cài đặt tài khoản")
                .setView(root)
                .setPositiveButton("Xong", null)
                .show();
    }

    private void renderAccountRows(LinearLayout list, Runnable refresh) {
        list.removeAllViews();
        ArrayList<Account> accounts = Constants.getAccounts(requireContext());
        for (Account account : accounts) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, dp(10), 0, dp(10));

            TextView delete = new TextView(requireContext());
            delete.setText("-");
            delete.setGravity(android.view.Gravity.CENTER);
            delete.setTextSize(22);
            delete.setTextColor(requireContext().getColor(R.color.white));
            delete.setBackground(requireContext().getDrawable(R.drawable.category_bg));
            delete.setBackgroundTintList(requireContext().getColorStateList(R.color.redColor));
            row.addView(delete, new LinearLayout.LayoutParams(dp(34), dp(34)));

            TextView name = new TextView(requireContext());
            name.setText(Constants.accountDisplayName(account.getAccount_name()));
            name.setTextColor(requireContext().getColor(R.color.textPrimary));
            name.setTextSize(17);
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            nameParams.setMargins(dp(16), 0, dp(8), 0);
            row.addView(name, nameParams);

            TextView edit = new TextView(requireContext());
            edit.setText("Sửa");
            edit.setTextColor(requireContext().getColor(R.color.orange));
            edit.setTextSize(15);
            edit.setTypeface(null, android.graphics.Typeface.BOLD);
            row.addView(edit);

            edit.setOnClickListener(view -> showAccountNameDialog(account, refresh));
            name.setOnClickListener(view -> showAccountNameDialog(account, refresh));
            delete.setOnClickListener(view -> {
                ArrayList<Account> updated = new ArrayList<>(Constants.getAccounts(requireContext()));
                if (updated.size() <= 1) {
                    Toast.makeText(requireContext(), "Cần giữ ít nhất 1 tài khoản", Toast.LENGTH_SHORT).show();
                    return;
                }
                updated.remove(account);
                Constants.saveAccounts(requireContext(), updated);
                refresh.run();
            });
            list.addView(row);
        }
    }

    private void showAccountNameDialog(@Nullable Account account, Runnable refresh) {
        EditText input = new EditText(requireContext());
        input.setHint("Tên tài khoản");
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setPadding(dp(16), dp(8), dp(16), dp(8));
        if (account != null) {
            input.setText(Constants.accountDisplayName(account.getAccount_name()));
            input.setSelection(input.getText().length());
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(account == null ? "Thêm tài khoản" : "Sửa tài khoản")
                .setView(input)
                .setNegativeButton("Hủy", null)
                .setPositiveButton(account == null ? "Thêm" : "Lưu", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.length() == 0) return;

                    ArrayList<Account> updated = new ArrayList<>(Constants.getAccounts(requireContext()));
                    if (account == null) {
                        updated.add(new Account(0, name));
                    } else {
                        for (Account item : updated) {
                            if (item.getAccount_name().equals(account.getAccount_name())) {
                                item.setAccount_name(name);
                                break;
                            }
                        }
                        renameTransactionsAccount(account.getAccount_name(), name);
                    }
                    Constants.saveAccounts(requireContext(), updated);
                    refresh.run();
                })
                .show();
    }

    private void renameTransactionsAccount(String oldName, String newName) {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<Transaction> transactions = realm.where(Transaction.class)
                    .equalTo("account", oldName)
                    .findAll();
            realm.executeTransaction(transactionRealm -> {
                for (Transaction transaction : transactions) {
                    transaction.setAccount(newName);
                }
            });
        } finally {
            realm.close();
        }
    }

    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        requireContext().getResources().updateConfiguration(config, requireContext().getResources().getDisplayMetrics());
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
