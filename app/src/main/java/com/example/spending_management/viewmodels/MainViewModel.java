package com.example.spending_management.viewmodels;

import android.app.Application;
import android.nfc.Tag;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.spending_management.models.Transaction;
import com.example.spending_management.utils.Constants;
import com.example.spending_management.views.activities.MainActivity;

import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainViewModel extends AndroidViewModel {


    public MutableLiveData<RealmResults<Transaction>> transactions = new MutableLiveData<>();
    public MutableLiveData<RealmResults<Transaction>> categoriesTransactions = new MutableLiveData<>();

    public MutableLiveData<Double> totalIncome = new MutableLiveData<>();
    public MutableLiveData<Double> totalExpense = new MutableLiveData<>();
    public MutableLiveData<Double> totalAmount = new MutableLiveData<>();

    Realm realm;
    Calendar calendar;

    public MainViewModel(@NonNull Application application) {
        super(application);
        Realm.init(application);
        setupDatabase();
    }

    public Transaction getTransaction(long id, boolean check)
    {
        if (check) return realm.where(Transaction.class)
                .equalTo("id", id)
                .findFirst();
        return realm.copyFromRealm(
                realm.where(Transaction.class)
                        .equalTo("id", id)
                        .findFirst());
    }

    public void getTransactions(Calendar calendar, String type) {
        this.calendar = calendar;
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        RealmResults<Transaction> newTransactions = null;

        if (Constants.SELECTED_TAB_STATS == Constants.DAILY) {

            newTransactions = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", calendar.getTime())
                    .lessThan("date", new Date(calendar.getTime().getTime() + (24 * 60 * 60 * 1000)))
                    .equalTo("type", type)
                    .findAll();
        } else if (Constants.SELECTED_TAB_STATS == Constants.MONTHLY) {
            Calendar startCalendar = (Calendar) calendar.clone();
            startCalendar.set(Calendar.DAY_OF_MONTH, 1);
            Date startTime = startCalendar.getTime();

            startCalendar.add(Calendar.MONTH, 1);
            Date endTime = startCalendar.getTime();

            newTransactions = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", startTime)
                    .lessThan("date", endTime)
                    .equalTo("type", type)
                    .findAll();
        }

//        // In số lượng giao dịch để kiểm tra
//        if (newTransactions != null) {
//            Log.d("Transactions", "Count: " + newTransactions.size());
//        } else {
//            Log.d("Transactions", "No transactions found");
//        }

        // Cập nhật dữ liệu vào LiveData
        categoriesTransactions.setValue(newTransactions);
    }




    public void getTransactions(Calendar calendar) {
        this.calendar = calendar;
        double income = 0;
        double expense = 0;
        double total = 0;
        RealmResults<Transaction> newTransactions = null;
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (Constants.SELECTED_TAB == Constants.CALENDAR)
        {
            newTransactions = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", calendar.getTime())
                    .lessThan("date", new Date(calendar.getTime().getTime() + (24 * 60 * 60 * 1000)))
                    .findAll();

            income = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", calendar.getTime())
                    .lessThan("date", new Date(calendar.getTime().getTime() + (24 * 60 * 60 * 1000)))
                    .equalTo("type", Constants.INCOME)
                    .sum("amount")
                    .doubleValue();

            expense = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", calendar.getTime())
                    .lessThan("date", new Date(calendar.getTime().getTime() + (24 * 60 * 60 * 1000)))
                    .equalTo("type", Constants.EXPENSE)
                    .sum("amount")
                    .doubleValue();

            total = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", calendar.getTime())
                    .lessThan("date", new Date(calendar.getTime().getTime() + (24 * 60 * 60 * 1000)))
                    .sum("amount")
                    .doubleValue();
        }


        if(Constants.SELECTED_TAB == Constants.DAILY) {
            // Select * from transactions
            // Select * from transactions where id = 5
            newTransactions = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", calendar.getTime())
                    .lessThan("date", new Date(calendar.getTime().getTime() + (24 * 60 * 60 * 1000)))
                    .findAll();

            income = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", calendar.getTime())
                    .lessThan("date", new Date(calendar.getTime().getTime() + (24 * 60 * 60 * 1000)))
                    .equalTo("type", Constants.INCOME)
                    .sum("amount")
                    .doubleValue();

            expense = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", calendar.getTime())
                    .lessThan("date", new Date(calendar.getTime().getTime() + (24 * 60 * 60 * 1000)))
                    .equalTo("type", Constants.EXPENSE)
                    .sum("amount")
                    .doubleValue();

            total = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", calendar.getTime())
                    .lessThan("date", new Date(calendar.getTime().getTime() + (24 * 60 * 60 * 1000)))
                    .sum("amount")
                    .doubleValue();
        } else if(Constants.SELECTED_TAB == Constants.MONTHLY) {
            Calendar calendarStart = (Calendar) calendar.clone();
            calendarStart.set(Calendar.DAY_OF_MONTH, 1); // Đặt ngày là 1
            Date startTime = calendarStart.getTime();

            // Tính ngày cuối tháng: lấy ngày 1 của tháng tiếp theo, sau đó trừ đi 1 ngày
            calendarStart.add(Calendar.MONTH, 1);
            calendarStart.set(Calendar.DAY_OF_MONTH, 1); // Đặt ngày là 1 của tháng tiếp theo
            calendarStart.add(Calendar.DATE, -1); // Lùi lại một ngày để có ngày cuối tháng
            Date endTime = calendarStart.getTime();

            newTransactions = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", startTime)
                    .lessThan("date", endTime)
                    .findAll();

            income = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", startTime)
                    .lessThan("date", endTime)
                    .equalTo("type", Constants.INCOME)
                    .sum("amount")
                    .doubleValue();

            expense = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", startTime)
                    .lessThan("date", endTime)
                    .equalTo("type", Constants.EXPENSE)
                    .sum("amount")
                    .doubleValue();

            total = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", startTime)
                    .lessThan("date", endTime)
                    .sum("amount")
                    .doubleValue();
        }

        totalIncome.setValue(income);
        totalExpense.setValue(expense);
        totalAmount.setValue(total);
        transactions.setValue(newTransactions);
        newTransactions = realm.where(Transaction.class)
                .equalTo("date", calendar.getTime())
                .findAll();

    }
    public void LoadMonthly(Calendar calendar)
    {
        this.calendar = calendar;
        double income = 0;
        double expense = 0;
        double total = 0;
        RealmResults<Transaction> newTransactions = null;
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Calendar calendarStart = (Calendar) calendar.clone();
        calendarStart.set(Calendar.DAY_OF_MONTH, 1); // Đặt ngày là 1
        Date startTime = calendarStart.getTime();

        // Tính ngày cuối tháng: lấy ngày 1 của tháng tiếp theo, sau đó trừ đi 1 ngày
        calendarStart.add(Calendar.MONTH, 1);
        calendarStart.set(Calendar.DAY_OF_MONTH, 1); // Đặt ngày là 1 của tháng tiếp theo
        calendarStart.add(Calendar.DATE, -1); // Lùi lại một ngày để có ngày cuối tháng
        Date endTime = calendarStart.getTime();

        newTransactions = realm.where(Transaction.class)
                .greaterThanOrEqualTo("date", startTime)
                .lessThan("date", endTime)
                .findAll();

        income = realm.where(Transaction.class)
                .greaterThanOrEqualTo("date", startTime)
                .lessThan("date", endTime)
                .equalTo("type", Constants.INCOME)
                .sum("amount")
                .doubleValue();

        expense = realm.where(Transaction.class)
                .greaterThanOrEqualTo("date", startTime)
                .lessThan("date", endTime)
                .equalTo("type", Constants.EXPENSE)
                .sum("amount")
                .doubleValue();

        total = realm.where(Transaction.class)
                .greaterThanOrEqualTo("date", startTime)
                .lessThan("date", endTime)
                .sum("amount")
                .doubleValue();

        totalIncome.setValue(income);
        totalExpense.setValue(expense);
        totalAmount.setValue(total);
        transactions.setValue(newTransactions);
        newTransactions = realm.where(Transaction.class)
                .equalTo("date", calendar.getTime())
                .findAll();
    }

    public void addTransaction(Transaction transaction) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(transaction);
        // some code here
        realm.commitTransaction();
    }

    public void deleteTransaction(Transaction transaction) {
        realm.beginTransaction();
        transaction.deleteFromRealm();
        realm.commitTransaction();
        getTransactions(calendar);
    }
    public void updateTransaction(Transaction transaction) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(realmInstance -> {
            realmInstance.insertOrUpdate(transaction);
        }, realm::close, error -> {
            //xu li loi
        });
    }
    @Override
    protected void onCleared() {
        super.onCleared();
        realm.close();
    }

    public void addTransactions() {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(new Transaction(Constants.INCOME, "Business", "Cash", "Some note here", new Date(), 500, new Date().getTime()));
        realm.copyToRealmOrUpdate(new Transaction(Constants.EXPENSE, "Investment", "Bank", "Some note here", new Date(), -900, new Date().getTime()));
        realm.copyToRealmOrUpdate(new Transaction(Constants.INCOME, "Rent", "Other", "Some note here", new Date(), 500, new Date().getTime()));
        realm.copyToRealmOrUpdate(new Transaction(Constants.INCOME, "Business", "Card", "Some note here", new Date(), 500, new Date().getTime()));
        // some code here
        realm.commitTransaction();
    }

    void setupDatabase() {
        realm = Realm.getDefaultInstance();
    }

    public void deleteAllTransactions() {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            RealmResults<Transaction> transactions = realm.where(Transaction.class).findAll();
            transactions.deleteAllFromRealm(); // Xóa tất cả các đối tượng Transaction
            realm.commitTransaction();
            getTransactions(calendar); // Cập nhật lại danh sách các transactions (nếu cần)
        } catch (Exception e) {
            Log.d("Loi", "Lỗi khi xóa tất cả transaction: " + e.toString());
        } finally {
            realm.close(); // Đảm bảo đóng Realm
        }
    }
}
