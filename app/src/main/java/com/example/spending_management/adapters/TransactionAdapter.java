package com.example.spending_management.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spending_management.R;
import com.example.spending_management.databinding.RowTransactionBinding;
import com.example.spending_management.models.Category;
import com.example.spending_management.models.Transaction;
import com.example.spending_management.utils.Constants;
import com.example.spending_management.utils.Helper;
import com.example.spending_management.views.activities.MainActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;

import io.realm.RealmResults;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {


    Context context;
    RealmResults<Transaction> transactions;
    private OnTransactionLongClickListener longClickListener;
    private OnTransactionClickListener clickListener;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }
    public interface OnTransactionLongClickListener {
        void onTransactionLongClick(Transaction transaction);
    }
    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.clickListener = listener;
    }
    public TransactionAdapter(Context context, RealmResults<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
    }


    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TransactionViewHolder(LayoutInflater.from(context).inflate(R.layout.row_transaction, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction cuttentTransaction = transactions.get(position);
        DecimalFormat df = new DecimalFormat("#");
        holder.binding.transactionAmount.setText(String.valueOf(df.format(cuttentTransaction.getAmount())));
        holder.binding.accountLbl.setText(accountDisplayName(cuttentTransaction.getAccount()));

        holder.binding.transactionDate.setText(Helper.formatDate(cuttentTransaction.getDate()));
        holder.binding.transactionCategory.setText(cuttentTransaction.getCategory());

        Category transactionCategory = Constants.getCategoryDetails(cuttentTransaction.getCategory());

        holder.binding.categoryIcon.setImageResource(transactionCategory.getCategory_image());
        holder.binding.categoryIcon.setBackgroundTintList(context.getColorStateList(transactionCategory.getCategory_color()));
        holder.binding.categoryIcon.setColorFilter(context.getColor(R.color.white));

        holder.binding.accountLbl.setBackgroundTintList(context.getColorStateList(Constants.getAccountColor(cuttentTransaction.getAccount())));

        if(cuttentTransaction.getType().equals(Constants.INCOME)){
            holder.binding.transactionAmount.setTextColor(context.getColor(R.color.greenColor));
        }else if (cuttentTransaction.getType().equals(Constants.EXPENSE)){
            holder.binding.transactionAmount.setTextColor(context.getColor(R.color.redColor));
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTransactionClick(cuttentTransaction);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setTitle("Delete Transaction")
                        .setMessage("Are you sure to delete this transaction?")
                        .setPositiveButton("YES", (dialogInterface, i) -> {
                            ((MainActivity)context).viewModel.deleteTransaction(cuttentTransaction);
                        })
                        .setNegativeButton("NO", (dialog, i) -> {
                            dialog.dismiss();
                        });

                AlertDialog deleteDialog = builder.create();
                deleteDialog.show();

                return true;
            }
        });



//        View editButton = holder.itemView.findViewById(R.id.editTransactionBtn);
//
//        if (editButton != null) {
//            editButton.setOnClickListener(v -> {
//            });
//        } else {
//            Log.e("TransactionAdapter", "Button Edit is null at position " + position);
//        }

        int categoryImage = transactionCategory.getCategory_image();
        if (categoryImage != 0) {
            holder.binding.categoryIcon.setImageResource(categoryImage);
            holder.binding.categoryIcon.setColorFilter(context.getColor(R.color.white));
        } else {
            Log.e("TransactionAdapter", "Invalid category image resource ID.");
        }

        int categoryColor = transactionCategory.getCategory_color();
        if (categoryColor != 0) {
            holder.binding.categoryIcon.setBackgroundTintList(context.getColorStateList(categoryColor));
        } else {
            Log.e("TransactionAdapter", "Invalid category color resource ID.");
        }


    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public class TransactionViewHolder extends RecyclerView.ViewHolder{
        RowTransactionBinding binding;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowTransactionBinding.bind(itemView);
        }
    }
    public void deleteTransaction(Transaction transaction) {
        transactions.remove(transaction);
        notifyDataSetChanged();
    }

    private String accountDisplayName(String accountValue) {
        if ("Cash".equals(accountValue)) return "Tiền mặt";
        if ("Bank".equals(accountValue)) return "Ngân hàng";
        if ("Pay pal".equals(accountValue)) return "PayPal";
        if ("Other".equals(accountValue)) return "Khác";
        return accountValue;
    }

}
