package com.example.spending_management.views.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.spending_management.adapters.TransactionAdapter;
import com.example.spending_management.databinding.FragmentTransactionsBinding;
import com.example.spending_management.models.Transaction;
import com.example.spending_management.utils.Constants;
import com.example.spending_management.utils.Helper;
import com.example.spending_management.viewmodels.MainViewModel;
import com.google.android.material.tabs.TabLayout;

import java.text.DecimalFormat;
import java.util.Calendar;

public class TransactionsFragment extends Fragment {

    private static Calendar savedCalendar = Calendar.getInstance();

    FragmentTransactionsBinding binding;
    Calendar calendar;
    public MainViewModel viewModel;

    public TransactionsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTransactionsBinding.inflate(inflater);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        calendar = (Calendar) savedCalendar.clone();
        if (Constants.SELECTED_TAB == Constants.CALENDAR) {
            Constants.SELECTED_TAB = Constants.DAILY;
        }

        setupDateControls();
        setupTabs();
        setupTransactionsList();
        setupTotals();
        selectCurrentTab();
        updateDate();

        return binding.getRoot();
    }

    private void setupDateControls() {
        binding.nextDateBtn.setOnClickListener(view -> {
            if (Constants.SELECTED_TAB == Constants.MONTHLY) {
                calendar.add(Calendar.MONTH, 1);
            } else {
                calendar.add(Calendar.DATE, 1);
            }
            updateDate();
        });

        binding.previousDateBtn.setOnClickListener(view -> {
            if (Constants.SELECTED_TAB == Constants.MONTHLY) {
                calendar.add(Calendar.MONTH, -1);
            } else {
                calendar.add(Calendar.DATE, -1);
            }
            updateDate();
        });
    }

    private void setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == Constants.MONTHLY) {
                    Constants.SELECTED_TAB = Constants.MONTHLY;
                    updateDate();
                } else if (tab.getPosition() == Constants.DAILY) {
                    Constants.SELECTED_TAB = Constants.DAILY;
                    updateDate();
                } else if (tab.getPosition() == Constants.CALENDAR) {
                    showDatePicker();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == Constants.CALENDAR) {
                    showDatePicker();
                }
            }
        });
    }

    private void setupTransactionsList() {
        binding.transactionList.setLayoutManager(new LinearLayoutManager(getContext()));
        viewModel.transactions.observe(getViewLifecycleOwner(), transactions -> {
            TransactionAdapter transactionsAdapter = new TransactionAdapter(getActivity(), transactions);
            transactionsAdapter.setOnTransactionClickListener(new TransactionAdapter.OnTransactionClickListener() {
                @Override
                public void onTransactionClick(Transaction transaction) {
                    new ClickInfor(transaction.getId()).show(getActivity().getSupportFragmentManager(), null);
                }
            });
            binding.transactionList.setAdapter(transactionsAdapter);
        });
    }

    private void setupTotals() {
        DecimalFormat df = new DecimalFormat("#");
        viewModel.totalIncome.observe(getViewLifecycleOwner(), value ->
                binding.incomeLbl.setText(String.valueOf(df.format(value))));
        viewModel.totalExpense.observe(getViewLifecycleOwner(), value ->
                binding.expenseLbl.setText(String.valueOf(df.format(value))));
        viewModel.totalAmount.observe(getViewLifecycleOwner(), value ->
                binding.totalLbl.setText(String.valueOf(df.format(value))));
    }

    private void selectCurrentTab() {
        int tabIndex = Constants.SELECTED_TAB == Constants.MONTHLY ? Constants.MONTHLY : Constants.DAILY;
        TabLayout.Tab tab = binding.tabLayout.getTabAt(tabIndex);
        if (tab != null && !tab.isSelected()) {
            tab.select();
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (datePicker, year, month, day) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, day);
                    Constants.SELECTED_TAB = Constants.DAILY;
                    selectCurrentTab();
                    updateDate();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.setOnCancelListener(dialog -> {
            Constants.SELECTED_TAB = Constants.DAILY;
            selectCurrentTab();
            updateDate();
        });
        datePickerDialog.show();
    }

    public void updateDate() {
        savedCalendar = (Calendar) calendar.clone();
        if (Constants.SELECTED_TAB == Constants.MONTHLY) {
            binding.currentDate.setText(Helper.formatDateByMonth(calendar.getTime()));
        } else {
            binding.currentDate.setText(Helper.formatDate(calendar.getTime()));
        }
        viewModel.getTransactions(calendar);
    }
}
