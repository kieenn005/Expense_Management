package com.example.spending_management.views.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.spending_management.models.Transaction;
import com.example.spending_management.adapters.TransactionAdapter;
import com.example.spending_management.databinding.FragmentTransactionsBinding;
import com.example.spending_management.utils.Constants;
import com.example.spending_management.utils.Helper;
import com.example.spending_management.viewmodels.MainViewModel;
import com.google.android.material.tabs.TabLayout;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.util.Calendar;

import io.realm.RealmResults;

public class TransactionsFragment extends Fragment {

    private static final Logger log = LogManager.getLogger(TransactionsFragment.class);
    FragmentTransactionsBinding binding;
    Calendar calendar;
    public MainViewModel viewModel;

    public int beforClickCalendar = 0;

    public TransactionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTransactionsBinding.inflate(inflater);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        calendar = Calendar.getInstance();
        updateDate();

        binding.nextDateBtn.setOnClickListener(c-> {
            if (Constants.SELECTED_TAB == Constants.CALENDAR)
            {
                if (beforClickCalendar == 0 || beforClickCalendar == 2) {
                    calendar.add(Calendar.DATE, 1);
                }
                else {
                    calendar.add(Calendar.MONTH, 1);
                    binding.currentDate.setText(Helper.formatDateByMonth(calendar.getTime()));
                    viewModel.LoadMonthly(calendar);
                    return;
                }
                binding.currentDate.setText(Helper.formatDate(calendar.getTime()));
                viewModel.getTransactions(calendar);
                return;
            }
            if(Constants.SELECTED_TAB == Constants.DAILY) {
                calendar.add(Calendar.DATE, 1);
            } else if(Constants.SELECTED_TAB == Constants.MONTHLY) {
                calendar.add(Calendar.MONTH, 1);
            }
            updateDate();
        });

        binding.previousDateBtn.setOnClickListener(c-> {
            if (Constants.SELECTED_TAB == Constants.CALENDAR)
            {
                if (beforClickCalendar == 0 || beforClickCalendar == 2) {
                    calendar.add(Calendar.DATE, -1);
                }
                else {
                    calendar.add(Calendar.MONTH, -1);
                    binding.currentDate.setText(Helper.formatDateByMonth(calendar.getTime()));
                    viewModel.LoadMonthly(calendar);
                    return;
                }
                binding.currentDate.setText(Helper.formatDate(calendar.getTime()));
                viewModel.getTransactions(calendar);
                return;
            }
            if(Constants.SELECTED_TAB == Constants.DAILY) {
                calendar.add(Calendar.DATE, -1);
            } else if(Constants.SELECTED_TAB == Constants.MONTHLY) {
                calendar.add(Calendar.MONTH, -1);
            }
            updateDate();
        });


        binding.floatingActionButton.setOnClickListener(c -> {
            new AddTransactionFragment().show(getParentFragmentManager(), null);
        });


        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getText().equals("Monthly") || tab.getText().equals("Tháng")) {
                    beforClickCalendar = 1;
                    Constants.SELECTED_TAB = 1;
                    updateDate();
                } else if(tab.getText().equals("Daily") || tab.getText().equals("Ngày")) {
                    beforClickCalendar = 0;
                    Constants.SELECTED_TAB = 0;
                    updateDate();
                } else if (tab.getText().equals("Calendar") || tab.getText().equals("Chọn ngày")) {
                    Constants.SELECTED_TAB = 2;
                    updateDate();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getText().equals("Calendar") || tab.getText().equals("Chọn ngày"))
                {
                    updateDate();
                }
            }
        });
        binding.transactionList.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel.transactions.observe(getViewLifecycleOwner(), new Observer<RealmResults<Transaction>>() {
            @Override
            public void onChanged(RealmResults<Transaction> transactions) {
                TransactionAdapter transactionsAdapter = new TransactionAdapter(getActivity(), transactions);

                transactionsAdapter.setOnTransactionClickListener(new TransactionAdapter.OnTransactionClickListener() {
                    @Override
                    public void onTransactionClick(Transaction transaction) {
//                        showTransactionDetails(transaction);
                        // Code Tuan Anh
                        long id = transaction.getId();
                        new ClickInfor(id).show(getActivity().getSupportFragmentManager(), null);
                    }
                });
                binding.transactionList.setAdapter(transactionsAdapter);
                if(transactions.size() > 0) {
//                    binding.emptyState.setVisibility(View.GONE);
                } else {
//                    binding.emptyState.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.totalIncome.observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                DecimalFormat df = new DecimalFormat("#");
                binding.incomeLbl.setText(String.valueOf(df.format(aDouble)));
            }
        });

        viewModel.totalExpense.observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                DecimalFormat df = new DecimalFormat("#");
                binding.expenseLbl.setText(String.valueOf(df.format(aDouble)));
            }
        });

        viewModel.totalAmount.observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                DecimalFormat df = new DecimalFormat("#");
                binding.totalLbl.setText(String.valueOf(df.format(aDouble)));
            }
        });
        viewModel.getTransactions(calendar);

        return binding.getRoot();
    }



    public void updateDate() {
        if(Constants.SELECTED_TAB == Constants.DAILY) {
            binding.currentDate.setText(Helper.formatDate(calendar.getTime()));
        } else if(Constants.SELECTED_TAB == Constants.MONTHLY) {
            binding.currentDate.setText(Helper.formatDateByMonth(calendar.getTime()));
        } else if (Constants.SELECTED_TAB == Constants.CALENDAR) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext());
            datePickerDialog.setOnDateSetListener((datePicker, i, i1, i2) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                calendar.set(Calendar.MONTH, datePicker.getMonth());
                calendar.set(Calendar.YEAR, datePicker.getYear());
                this.calendar = calendar;
                binding.currentDate.setText(Helper.formatDate(calendar.getTime()));
                viewModel.getTransactions(calendar);
                beforClickCalendar = 2;
            });
            datePickerDialog.show();
            return;
        }
        viewModel.getTransactions(calendar);
    }

}