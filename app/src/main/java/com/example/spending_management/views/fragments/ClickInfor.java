package com.example.spending_management.views.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.spending_management.R;
import com.example.spending_management.adapters.AccountAdapter;
import com.example.spending_management.adapters.CategoryAdapter;
import com.example.spending_management.databinding.FragmentClickInforBinding;
import com.example.spending_management.databinding.ListDialogBinding;
import com.example.spending_management.models.Account;
import com.example.spending_management.models.Category;
import com.example.spending_management.models.Transaction;
import com.example.spending_management.utils.Constants;
import com.example.spending_management.utils.Helper;
import com.example.spending_management.views.activities.MainActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class ClickInfor extends BottomSheetDialogFragment {

    long id;
    FragmentClickInforBinding binding;
    Transaction transaction, transactionDelete;

    public ClickInfor() {
        // Required empty public constructor
    }

    public ClickInfor(long id)
    {
        this.id = id;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentClickInforBinding.inflate(inflater);
        transaction = ((MainActivity)getActivity()).viewModel.getTransaction(id, false);
        transactionDelete = ((MainActivity)getActivity()).viewModel.getTransaction(id, true);
        //Toast.makeText(getContext(), " " + transaction.getType(), Toast.LENGTH_SHORT).show();
        if (transaction.getType().equals(Constants.INCOME))
        {
            binding.incomeBtn.setBackground(getContext().getDrawable(R.drawable.income_selector));
            binding.expenseBtn.setBackground(getContext().getDrawable(R.drawable.default_selector));
            binding.expenseBtn.setTextColor(getContext().getColor(R.color.textColor));
            binding.incomeBtn.setTextColor(getContext().getColor(R.color.greenColor));
        }
        else
        {
            binding.incomeBtn.setBackground(getContext().getDrawable(R.drawable.default_selector));
            binding.expenseBtn.setBackground(getContext().getDrawable(R.drawable.expense_selector));
            binding.incomeBtn.setTextColor(getContext().getColor(R.color.textColor));
            binding.expenseBtn.setTextColor(getContext().getColor(R.color.redColor));
        }
        binding.incomeBtn.setOnClickListener(view -> {
            binding.incomeBtn.setBackground(getContext().getDrawable(R.drawable.income_selector));
            binding.expenseBtn.setBackground(getContext().getDrawable(R.drawable.default_selector));
            binding.expenseBtn.setTextColor(getContext().getColor(R.color.textColor));
            binding.incomeBtn.setTextColor(getContext().getColor(R.color.greenColor));
            transaction.setType(Constants.INCOME);
        });
        binding.expenseBtn.setOnClickListener(view -> {
            binding.incomeBtn.setBackground(getContext().getDrawable(R.drawable.default_selector));
            binding.expenseBtn.setBackground(getContext().getDrawable(R.drawable.expense_selector));
            binding.incomeBtn.setTextColor(getContext().getColor(R.color.textColor));
            binding.expenseBtn.setTextColor(getContext().getColor(R.color.redColor));
            transaction.setType(Constants.EXPENSE);
        });
        binding.date.setText(Helper.formatDate(transaction.getDate()));
        binding.date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext());
                datePickerDialog.setOnDateSetListener((datePicker, i, i1, i2) -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                    calendar.set(Calendar.MONTH, datePicker.getMonth());
                    calendar.set(Calendar.YEAR, datePicker.getYear());
                    //SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM, yyyy");
                    String dateToShow = Helper.formatDate(calendar.getTime());
                    binding.date.setText(dateToShow);
                    transaction.setDate(calendar.getTime());
                    transaction.setId(calendar.getTime().getTime());
                });
                datePickerDialog.show();
            }
        });
        DecimalFormat df = new DecimalFormat("#");
        double tam = transaction.getAmount();
        if (tam < 0) tam *= -1;
        binding.amount.setText(String.valueOf(df.format(tam)));
        binding.category.setText(transaction.getCategory());
        binding.category.setOnClickListener(c-> {
            ListDialogBinding dialogBinding = ListDialogBinding.inflate(inflater);
            AlertDialog categoryDialog = new AlertDialog.Builder(getContext()).create();
            categoryDialog.setView(dialogBinding.getRoot());
            CategoryAdapter categoryAdapter = new CategoryAdapter(getContext(), Constants.categories, new CategoryAdapter.CategoryClickListener() {
                @Override
                public void onCategoryClicked(Category category) {
                    binding.category.setText(category.getCategoryName());
                    transaction.setCategory(category.getCategoryName());
                    categoryDialog.dismiss();
                }
            });
            dialogBinding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(),3));
            dialogBinding.recyclerView.setAdapter(categoryAdapter);
            categoryDialog.show();
        });
        binding.account.setText(transaction.getAccount());
        binding.account.setOnClickListener(c-> {
            ListDialogBinding dialogBinding = ListDialogBinding.inflate(inflater);
            AlertDialog accountsDialog = new AlertDialog.Builder(getContext()).create();
            accountsDialog.setView(dialogBinding.getRoot());
            ArrayList<Account> accounts = new ArrayList<>();
            accounts.add(new Account(0, "Cash"));
            accounts.add(new Account(0, "Bank"));
            accounts.add(new Account(0, "PayTM"));
            accounts.add(new Account(0, "EasyPaisa"));
            accounts.add(new Account(0, "Other"));
            AccountAdapter adapter = new AccountAdapter(getContext(), accounts, new AccountAdapter.AccountClickListener() {
                @Override
                public void onAccountSelected(Account account) {
                    binding.account.setText(account.getAccount_name());
                    transaction.setAccount(account.getAccount_name());
                    accountsDialog.dismiss();
                }
            });
            dialogBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            //dialogBinding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
            dialogBinding.recyclerView.setAdapter(adapter);
            accountsDialog.show();
        });
        binding.note.setText(transaction.getNote());
        binding.saveTransactionBtn.setOnClickListener(c-> {
            if (transaction.getType() == null)
            {
                Toast.makeText(getContext(), "Bạn chưa chọn Thu nhập hoặc Chi phí!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (transaction.getDate() == null)
            {
                Toast.makeText(getContext(), "Bạn chưa chọn ngày!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (binding.amount.getText().toString().length() == 0)
            {
                Toast.makeText(getContext(), "Bạn chưa nhập số tiền!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (transaction.getCategory() == null)
            {
                Toast.makeText(getContext(), "Bạn chưa chọn loại chi tiêu!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (transaction.getAccount() == null)
            {
                Toast.makeText(getContext(), "Bạn chưa chọn kiểu thanh toán!", Toast.LENGTH_SHORT).show();
                return;
            }
            ((MainActivity)getActivity()).viewModel.deleteTransaction(transactionDelete);
            double amount = Double.parseDouble(binding.amount.getText().toString());
            String note = binding.note.getText().toString();
            if(transaction.getType().equals(Constants.EXPENSE)) {
                transaction.setAmount(amount*-1);
            } else {
                transaction.setAmount(amount);
            }
            transaction.setNote(note);
            ((MainActivity)getActivity()).viewModel.addTransaction(transaction);
            ((MainActivity)getActivity()).getTransactions();
            Toast.makeText(getContext(), "Lưu thành công!", Toast.LENGTH_SHORT).show();
            dismiss();
        });
        binding.deleteTransactionBtn.setOnClickListener(c-> {
            ((MainActivity)getActivity()).viewModel.deleteTransaction(transactionDelete);
            ((MainActivity)getActivity()).getTransactions();
            Toast.makeText(getContext(), "Xóa thành công!", Toast.LENGTH_SHORT).show();
            dismiss();
        });
        return binding.getRoot();
    }
}