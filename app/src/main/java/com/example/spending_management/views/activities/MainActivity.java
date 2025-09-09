package com.example.spending_management.views.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.spending_management.adapters.TransactionAdapter;
import com.example.spending_management.models.Transaction;
import com.example.spending_management.utils.Constants;
import com.example.spending_management.utils.Helper;
import com.example.spending_management.viewmodels.MainViewModel;
import com.example.spending_management.views.fragments.AddTransactionFragment;
import com.example.spending_management.R;
import com.example.spending_management.databinding.ActivityMainBinding;
import com.example.spending_management.views.fragments.ClickInfor;
import com.example.spending_management.views.fragments.SettingsFragment;
import com.example.spending_management.views.fragments.StatsFragment;
import com.example.spending_management.views.fragments.TransactionsFragment;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;


import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    Calendar calendar;
    public MainViewModel viewModel;
    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        // Cập nhật cấu hình ngôn ngữ
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Nếu cần thay đổi text trong các view khác sau khi thay đổi ngôn ngữ, bạn có thể gọi lại phương thức cập nhật các view này tại đây.
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setSupportActionBar(binding.toolBar);
        getSupportActionBar().setTitle("Transactions");


        Constants.setCategories();
        calendar = Calendar.getInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content, new TransactionsFragment());
        transaction.commit();

        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);

        int cnt = preferences.getInt("cnt", 1);

        if (cnt > 0)
        {
            cnt -= 1;
            boolean isDarkMode = preferences.getBoolean("dark_mode", false);
            String currentLanguage = preferences.getString("language", "English");
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("dark_mode", isDarkMode);
            editor.apply();
            AppCompatDelegate.setDefaultNightMode(
                    isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            String[] languages = {"English", "Tiếng Việt"};
            String[] languageCodes = {"en", "vi"}; // Mã ngôn ngữ tương ứng
            int which = currentLanguage.equals("English") ? 0 : 1;
            String selectedLanguage = languages[which];
            String selectedLanguageCode = languageCodes[which];
            // Lưu ngôn ngữ đã chọn vào SharedPreferences
            editor.putString("language", selectedLanguage);
            editor.putString("language_code", selectedLanguageCode);
            editor.apply();
            // Cập nhật Locale
            setLocale(selectedLanguageCode);
//            recreate();
            Log.d("Long", "chay bao lan");
        }

        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.d("An vao tab", "Yes" + item.toString());
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                if(item.getItemId() == R.id.transaction){
                    getSupportFragmentManager().popBackStack();

                }else if (item.getItemId() == R.id.stats){
                    transaction.replace(R.id.content, new StatsFragment());
                    transaction.addToBackStack(null);
                } else if (item.getItemId() == R.id.settings){
                    transaction.replace(R.id.content, new SettingsFragment());
                    transaction.addToBackStack(null);
                }
                else if (item.getItemId() == R.id.more)
                {
                    showMoreMenu();
                }
                transaction.commit();
                return true;
            }
        });
        binding.bottomNavigationView.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                Log.d("An lai tab", "Yes" + item.toString());
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                if(item.getItemId() == R.id.transaction){
                    getSupportFragmentManager().popBackStack();

                }else if (item.getItemId() == R.id.stats){
                    transaction.replace(R.id.content, new StatsFragment());
                    transaction.addToBackStack(null);
                } else if (item.getItemId() == R.id.settings){
                    transaction.replace(R.id.content, new SettingsFragment());
                    transaction.addToBackStack(null);
                }
                else if (item.getItemId() == R.id.more)
                {
                    showMoreMenu();
                }
                transaction.commit();
            }
        });
    }

    private void showMoreMenu(){
        PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.more));

        popupMenu.getMenuInflater().inflate(R.menu.more_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.export_excel) {
                exportExcel(MainActivity.this);
                return true;
            }
            else if (menuItem.getItemId() == R.id.clear)
            {
                ClearData();
                return true;
            }
            else if (menuItem.getItemId() == R.id.import_excel)
            {
                ImportData(MainActivity.this);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }
    public void exportExcel(Context context) {
        Realm realm = Realm.getDefaultInstance();
        try {
            // Lấy tất cả các đối tượng Transaction từ Realm
            RealmResults<Transaction> transactions = realm.where(Transaction.class).findAll();

            // Tạo một workbook Excel mới
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Transactions");

            // Tạo header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Type");
            headerRow.createCell(2).setCellValue("Category");
            headerRow.createCell(3).setCellValue("Account");
            headerRow.createCell(4).setCellValue("Note");
            headerRow.createCell(5).setCellValue("Date");
            headerRow.createCell(6).setCellValue("Amount");

            // Duyệt qua tất cả các transaction và tạo các dòng dữ liệu
            int rowNum = 1;
            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(transaction.getId());
                row.createCell(1).setCellValue(transaction.getType());
                row.createCell(2).setCellValue(transaction.getCategory());
                row.createCell(3).setCellValue(transaction.getAccount());
                row.createCell(4).setCellValue(transaction.getNote());
                row.createCell(5).setCellValue(transaction.getDate().toString());
                row.createCell(6).setCellValue(transaction.getAmount());
            }

            // Lưu file trong thư mục ứng dụng
            File file = new File(context.getExternalFilesDir(null), "Transactions.xlsx");
            FileOutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();

            // Thông báo thành công
            Log.d("ExportExcel", "Dữ liệu đã được xuất ra file Excel thành công.");
            Toast.makeText(MainActivity.this, "Dữ liệu đã được xuất ra file Excel thành công.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("ExportExcel", "Lỗi khi xuất dữ liệu ra Excel: " + e.toString());
            Toast.makeText(MainActivity.this, "Lỗi khi xuất dữ liệu ra Excel", Toast.LENGTH_SHORT).show();
        } finally {
            realm.close(); // Đảm bảo đóng Realm sau khi sử dụng
        }
    }
    private void ClearData()
    {
        viewModel.deleteAllTransactions();
        Toast.makeText(MainActivity.this, "Xóa dữ liệu thành công!", Toast.LENGTH_SHORT).show();
    }

    public void ImportData(Context context) {
        try {
            // Đường dẫn đến tệp Excel
            File file = new File(context.getExternalFilesDir(null), "Transactions.xlsx");

            // Kiểm tra nếu tệp tồn tại
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);

                // Tạo workbook từ file
                Workbook workbook = new XSSFWorkbook(fis);

                // Chọn sheet đầu tiên trong workbook
                Sheet sheet = workbook.getSheetAt(0);

                // Bắt đầu giao dịch Realm
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();

                // Tạo một SimpleDateFormat để phân tích ngày tháng
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                // Định dạng ngày tháng
                // Duyệt qua tất cả các dòng trong sheet
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue; // Bỏ qua hàng tiêu đề

                    // Lấy dữ liệu từ các ô trong dòng
                    long id = (long) row.getCell(0).getNumericCellValue();
                    String type = row.getCell(1).getStringCellValue();
                    String category = row.getCell(2).getStringCellValue();
                    String account = row.getCell(3).getStringCellValue();
                    String note = row.getCell(4).getStringCellValue();
                    double amount = row.getCell(6).getNumericCellValue();
                    String dateString = row.getCell(5).getStringCellValue();

                    // Chuyển đổi chuỗi ngày thành đối tượng Date
                    Date date = null;
                    try {
                        date = dateFormat.parse(dateString); // Phân tích chuỗi ngày thành Date
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Tạo đối tượng Transaction mới
                    Transaction transaction = new Transaction(type, category, account, note, date, amount, id);

                    // Lưu vào Realm
                    realm.insertOrUpdate(transaction);
                }

                // Cam kết giao dịch
                realm.commitTransaction();

                // Đóng workbook và file input stream
                workbook.close();
                fis.close();
                realm.close();
                viewModel.getTransactions(calendar);
                Log.d("calendar", calendar.getTime().toString());
                Log.d("Excel Import", "Dữ liệu đã được nhập thành công.");
                Toast.makeText(MainActivity.this, "Dữ liệu đã được nhập thành công.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Tệp không tồn tại.", Toast.LENGTH_SHORT).show();
                Log.d("Excel Import", "Tệp không tồn tại.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Lỗi khi đọc tệp Excel.", Toast.LENGTH_SHORT).show();
            Log.d("Excel Import", "Lỗi khi đọc tệp Excel: " + e.getMessage());
        }
    }
    public void getTransactions() {
        viewModel.getTransactions(calendar);
    }

    public MenuItem searchItem, thongBao;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu,menu);
        searchItem = menu.findItem(R.id.search);
        thongBao = menu.findItem(R.id.thongBao);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item == searchItem)
        {
            Toast.makeText(this, "Vui lòng đăng ký Vip để tìm kiếm!", Toast.LENGTH_SHORT).show();
        }
        else if (item == thongBao)
        {
            Toast.makeText(this, "Không có thông báo!", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean checker = preferences.getBoolean("checker", false);
        Log.d("Checker", String.valueOf(checker));
        if (checker)
        {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("checker", false);
            editor.apply();
            FragmentTransaction transactionss = getSupportFragmentManager().beginTransaction();
            transactionss.replace(R.id.content, new SettingsFragment());
            transactionss.addToBackStack(null);
            transactionss.commit();
            Log.d("Select tab 3", "Done");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("cnt", 1);
        editor.apply();
    }
}