package com.example.spending_management.views.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.spending_management.R;
import com.example.spending_management.databinding.FragmentAddTransactionBinding;
import com.example.spending_management.models.Account;
import com.example.spending_management.models.Category;
import com.example.spending_management.models.Transaction;
import com.example.spending_management.utils.Constants;
import com.example.spending_management.utils.Helper;
import com.example.spending_management.views.activities.MainActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.text.DecimalFormat;

public class AddTransactionFragment extends BottomSheetDialogFragment {

    private FragmentAddTransactionBinding binding;
    private Transaction transaction;
    private Calendar selectedDate;
    private String amountText = "";
    private String selectedAccountValue = "Cash";
    private Category selectedCategory;

    public AddTransactionFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() instanceof BottomSheetDialog) {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setDraggable(false);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddTransactionBinding.inflate(inflater, container, false);
        transaction = new Transaction();
        selectedDate = Calendar.getInstance();

        transaction.setType(Constants.EXPENSE);
        transaction.setDate(selectedDate.getTime());
        transaction.setId(System.currentTimeMillis());
        transaction.setAccount(selectedAccountValue);
        binding.account.setText(accountDisplayName(selectedAccountValue));

        setupActions();
        setupKeypad();
        renderTypeTabs();
        renderCategories();
        setupCategoryScroll();

        return binding.getRoot();
    }

    private void setupCategoryScroll() {
        binding.categoryScroll.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
            }
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });
    }

    private void setupActions() {
        binding.cancelBtn.setOnClickListener(view -> dismiss());
        binding.expenseBtn.setOnClickListener(view -> {
            transaction.setType(Constants.EXPENSE);
            selectedCategory = null;
            renderTypeTabs();
            renderCategories();
        });
        binding.incomeBtn.setOnClickListener(view -> {
            transaction.setType(Constants.INCOME);
            selectedCategory = null;
            renderTypeTabs();
            renderCategories();
        });
        binding.date.setOnClickListener(view -> showDatePicker());
        binding.account.setOnClickListener(view -> showAccountPicker());
        binding.saveTransactionBtn.setOnClickListener(view -> saveTransaction());
    }

    private void renderTypeTabs() {
        boolean isIncome = Constants.INCOME.equals(transaction.getType());
        binding.incomeBtn.setBackground(isIncome ? requireContext().getDrawable(R.drawable.income_selector) : null);
        binding.expenseBtn.setBackground(!isIncome ? requireContext().getDrawable(R.drawable.expense_selector) : null);
        binding.incomeBtn.setTextColor(requireContext().getColor(isIncome ? R.color.greenColor : R.color.textSecondary));
        binding.expenseBtn.setTextColor(requireContext().getColor(!isIncome ? R.color.redColor : R.color.textSecondary));
    }

    private void renderCategories() {
        Constants.setCategories(requireContext());
        binding.categoryGrid.removeAllViews();

        ArrayList<Category> categories = Constants.getCategories(transaction.getType());
        for (Category category : categories) {
            binding.categoryGrid.addView(createCategoryView(category));
        }
    }

    private View createCategoryView(Category category) {
        LinearLayout item = new LinearLayout(requireContext());
        item.setGravity(android.view.Gravity.CENTER);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(4, 6, 4, 10);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(4, 8, 4, 8);
        item.setLayoutParams(params);

        ImageView icon = new ImageView(requireContext());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(58), dp(58));
        icon.setLayoutParams(iconParams);
        icon.setPadding(dp(14), dp(14), dp(14), dp(14));
        icon.setImageResource(category.getCategory_image());
        icon.setBackground(requireContext().getDrawable(R.drawable.category_bg));
        boolean isSelected = selectedCategory != null
                && selectedCategory.getCategoryName().equals(category.getCategoryName());
        icon.setBackgroundTintList(requireContext().getColorStateList(
                isSelected ? R.color.orange : category.getCategory_color()
        ));
        icon.setColorFilter(requireContext().getColor(R.color.white));

        TextView label = new TextView(requireContext());
        label.setText(category.getCategoryName());
        label.setTextColor(requireContext().getColor(R.color.textPrimary));
        label.setTextSize(13);
        label.setGravity(android.view.Gravity.CENTER);
        label.setMaxLines(2);

        item.addView(icon);
        item.addView(label);
        item.setOnClickListener(view -> {
            selectedCategory = category;
            transaction.setCategory(category.getCategoryName());
            renderCategories();
        });

        return item;
    }

    private void setupKeypad() {
        String[] keys = {"7", "8", "9", "⌫", "4", "5", "6", "+", "1", "2", "3", "-", ",", "0", ".", "✓"};
        for (String key : keys) {
            TextView button = new TextView(requireContext());
            button.setText(key);
            button.setGravity(android.view.Gravity.CENTER);
            button.setTextSize("✓".equals(key) ? 30 : 24);
            button.setTextColor(requireContext().getColor("✓".equals(key) ? R.color.white : R.color.textPrimary));
            button.setBackground(requireContext().getDrawable("✓".equals(key) ? R.drawable.account_bg : R.drawable.default_selector));
            if ("✓".equals(key)) {
                button.setBackgroundTintList(requireContext().getColorStateList(R.color.orange));
            }
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(5, 5, 5, 5);
            button.setLayoutParams(params);
            button.setOnClickListener(view -> handleKey(key));
            binding.keypadGrid.addView(button);
        }
    }

    private void handleKey(String key) {
        if ("✓".equals(key)) {
            saveTransaction();
            return;
        }
        if ("⌫".equals(key)) {
            if (amountText.length() > 0) {
                amountText = amountText.substring(0, amountText.length() - 1);
            }
        } else if ("+".equals(key) || "-".equals(key)) {
            return;
        } else {
            String normalized = ",".equals(key) ? "." : key;
            if (".".equals(normalized) && amountText.contains(".")) return;
            amountText += normalized;
        }
        binding.amount.setText(formatInputAmount());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext());
        datePickerDialog.setOnDateSetListener((datePicker, year, month, day) -> {
            selectedDate.set(Calendar.DAY_OF_MONTH, day);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.YEAR, year);
            transaction.setDate(selectedDate.getTime());
            transaction.setId(selectedDate.getTime().getTime());
            binding.date.setText(Helper.formatDate(selectedDate.getTime()));
        });
        datePickerDialog.show();
    }

    private void showAccountPicker() {
        ArrayList<Account> accounts = new ArrayList<>(Constants.getAccounts(requireContext()));

        String[] accountNames = new String[accounts.size()];
        for (int i = 0; i < accounts.size(); i++) {
            accountNames[i] = accountDisplayName(accounts.get(i).getAccount_name());
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn tài khoản")
                .setItems(accountNames, (dialog, which) -> {
                    selectedAccountValue = accounts.get(which).getAccount_name();
                    transaction.setAccount(selectedAccountValue);
                    binding.account.setText(accountDisplayName(selectedAccountValue));
                })
                .show();
    }

    private void saveTransaction() {
        if (amountText.length() == 0) {
            Toast.makeText(getContext(), "Bạn chưa nhập số tiền!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (transaction.getCategory() == null) {
            Toast.makeText(getContext(), "Bạn chưa chọn danh mục!", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountText);
        transaction.setAmount(Constants.EXPENSE.equals(transaction.getType()) ? amount * -1 : amount);
        transaction.setNote(binding.note.getText().toString());
        transaction.setId(System.currentTimeMillis());

        ((MainActivity) requireActivity()).viewModel.addTransaction(transaction);
        ((MainActivity) requireActivity()).getTransactions();
        Toast.makeText(getContext(), "Lưu thành công!", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private String formatInputAmount() {
        if (amountText.length() == 0) return "0";
        if (amountText.contains(".")) return amountText;
        try {
            DecimalFormat format = new DecimalFormat("#,###");
            return format.format(Double.parseDouble(amountText)).replace(",", ".");
        } catch (NumberFormatException ignored) {
            return amountText;
        }
    }

    private String accountDisplayName(String accountValue) {
        return Constants.accountDisplayName(accountValue);
    }
}
