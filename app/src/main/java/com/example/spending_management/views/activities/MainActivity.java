package com.example.spending_management.views.activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
import com.example.spending_management.views.fragments.ChatbotFragment;
import com.example.spending_management.views.fragments.MoreFragment;
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
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    Calendar calendar;
    public MainViewModel viewModel;
    private ActivityResultLauncher<String[]> excelPickerLauncher;
    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        String currentLanguageCode = preferences.getString("language_code", "vi");
        setLocale(currentLanguageCode);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        excelPickerLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                importExcelFromUri(uri);
            }
        });

        setSupportActionBar(binding.toolBar);
        getSupportActionBar().setTitle(getString(R.string.app_transaction));


        Constants.setCategories(this);
        calendar = Calendar.getInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content, new TransactionsFragment());
        transaction.commit();

        int cnt = preferences.getInt("cnt", 1);

        if (cnt > 0)
        {
            cnt -= 1;
            boolean isDarkMode = preferences.getBoolean("dark_mode", false);
            String currentLanguage = preferences.getString("language", "Tiếng Việt");
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("dark_mode", isDarkMode);
            editor.apply();
            AppCompatDelegate.setDefaultNightMode(
                    isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            String selectedLanguage = currentLanguage.equals("English") ? "English" : "Tiếng Việt";
            String selectedLanguageCode = currentLanguage.equals("English") ? "en" : "vi";
            // Lưu ngôn ngữ đã chọn vào SharedPreferences
            editor.putString("language", selectedLanguage);
            editor.putString("language_code", selectedLanguageCode);
            editor.apply();
            // Cập nhật Locale
            setLocale(selectedLanguageCode);
//            recreate();
            Log.d("Long", "chay bao lan");
        }

        binding.mainAddButton.setOnClickListener(view -> showAddTransaction());
        binding.aiBubble.setOnClickListener(view -> openChatbot());

        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.d("An vao tab", "Yes" + item.toString());
                return handleBottomNavigation(item);
            }
        });
        binding.bottomNavigationView.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                Log.d("An lai tab", "Yes" + item.toString());
                handleBottomNavigation(item);
            }
        });
    }

    private boolean handleBottomNavigation(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.transaction) {
            switchMainFragment(new TransactionsFragment());
            return true;
        } else if (item.getItemId() == R.id.stats) {
            switchMainFragment(new StatsFragment());
            return true;
        } else if (item.getItemId() == R.id.settings) {
            switchMainFragment(new SettingsFragment());
            return true;
        } else if (item.getItemId() == R.id.add_transaction) {
            showAddTransaction();
            return false;
        } else if (item.getItemId() == R.id.more) {
            switchMainFragment(new MoreFragment());
            return true;
        }
        return false;
    }

    private void switchMainFragment(Fragment fragment) {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
        binding.aiBubble.setVisibility(fragment instanceof ChatbotFragment ? View.GONE : View.VISIBLE);
    }

    private void showAddTransaction() {
        new AddTransactionFragment().show(getSupportFragmentManager(), null);
    }

    public void openChatbot() {
        switchMainFragment(new ChatbotFragment());
    }

    public void exportExcel(Context context) {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<Transaction> transactions = realm.where(Transaction.class).findAll();

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Transactions");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Mã");
            headerRow.createCell(1).setCellValue("Loại");
            headerRow.createCell(2).setCellValue("Danh mục");
            headerRow.createCell(3).setCellValue("Tài khoản");
            headerRow.createCell(4).setCellValue("Ghi chú");
            headerRow.createCell(5).setCellValue("Ngày");
            headerRow.createCell(6).setCellValue("Số tiền");

            int rowNum = 1;
            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(String.valueOf(transaction.getId()));
                row.createCell(1).setCellValue(typeDisplayName(transaction.getType()));
                row.createCell(2).setCellValue(transaction.getCategory());
                row.createCell(3).setCellValue(accountDisplayName(transaction.getAccount()));
                row.createCell(4).setCellValue(transaction.getNote());
                row.createCell(5).setCellValue(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(transaction.getDate()));
                row.createCell(6).setCellValue(transaction.getAmount());
            }

            String savedPath = saveWorkbook(context, workbook, "Transactions.xlsx");
            workbook.close();

            Log.d("ExportExcel", "Dữ liệu đã được xuất ra file Excel thành công: " + savedPath);
            Toast.makeText(MainActivity.this, "Dữ liệu đã được xuất ra file Excel thành công.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("ExportExcel", "Lỗi khi xuất dữ liệu ra Excel: " + e.toString());
            Toast.makeText(MainActivity.this, "Lỗi khi xuất dữ liệu ra Excel", Toast.LENGTH_SHORT).show();
        } finally {
            realm.close();
        }
    }

    private String saveWorkbook(Context context, Workbook workbook, String fileName) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                return saveWorkbookToMediaStore(context, workbook, fileName);
            } catch (Exception e) {
                Log.w("ExportExcel", "Cannot save to Downloads, using app folder instead: " + e.toString());
            }
        }

        File directory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (directory == null) {
            directory = context.getFilesDir();
        }
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Cannot create export directory");
        }

        File file = new File(directory, fileName);
        try (OutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
        }
        return file.getAbsolutePath();
    }

    private String saveWorkbookToMediaStore(Context context, Workbook workbook, String fileName) throws IOException {
        Uri fileUri = createDownloadsFile(context, fileName);
        try (OutputStream fileOut = context.getContentResolver().openOutputStream(fileUri)) {
            if (fileOut == null) {
                throw new IOException("Cannot open output stream");
            }
            workbook.write(fileOut);
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.IS_PENDING, 0);
        context.getContentResolver().update(fileUri, values, null, null);
        return fileUri.toString();
    }

    private Uri createDownloadsFile(Context context, String fileName) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Downloads.IS_PENDING, 1);
        }

        Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            throw new IOException("Cannot create Downloads file");
        }
        return uri;
    }

    public void ClearData()
    {
        viewModel.deleteAllTransactions();
        Toast.makeText(MainActivity.this, "Xóa dữ liệu thành công!", Toast.LENGTH_SHORT).show();
    }

    public void ImportData(Context context) {
        try {
            InputStream fis = openExcelInputStream(context);

            if (fis != null) {
                Workbook workbook = new XSSFWorkbook(fis);

                Sheet sheet = workbook.getSheetAt(0);

                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();

                DataFormatter formatter = new DataFormatter();
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;

                    long id = readLong(row.getCell(0), formatter);
                    String type = typeStorageValue(formatter.formatCellValue(row.getCell(1)));
                    String category = formatter.formatCellValue(row.getCell(2));
                    String account = accountStorageValue(formatter.formatCellValue(row.getCell(3)));
                    String note = formatter.formatCellValue(row.getCell(4));
                    double amount = row.getCell(6).getNumericCellValue();
                    String dateString = formatter.formatCellValue(row.getCell(5));

                    Date date = parseExcelDate(dateString);

                    Transaction transaction = new Transaction(type, category, account, note, date, amount, id);

                    realm.insertOrUpdate(transaction);
                }

                realm.commitTransaction();

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

    public void pickExcelFile() {
        excelPickerLauncher.launch(new String[]{
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-excel",
                "application/octet-stream"
        });
    }

    private void importExcelFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Toast.makeText(this, "Không mở được file Excel.", Toast.LENGTH_SHORT).show();
                return;
            }
            importTransactionsFromExcel(inputStream);
        } catch (IOException e) {
            Log.d("Excel Import", "Cannot read selected file: " + e.getMessage());
            Toast.makeText(this, "Không đọc được file Excel.", Toast.LENGTH_SHORT).show();
        }
    }

    private void importTransactionsFromExcel(InputStream inputStream) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Realm realm = Realm.getDefaultInstance();

        try {
            realm.beginTransaction();
            DataFormatter formatter = new DataFormatter();
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                long id = readLong(row.getCell(0), formatter);
                String type = typeStorageValue(formatter.formatCellValue(row.getCell(1)));
                String category = formatter.formatCellValue(row.getCell(2));
                String account = accountStorageValue(formatter.formatCellValue(row.getCell(3)));
                String note = formatter.formatCellValue(row.getCell(4));
                double amount = row.getCell(6).getNumericCellValue();
                String dateString = formatter.formatCellValue(row.getCell(5));
                Date date = parseExcelDate(dateString);

                Transaction transaction = new Transaction(type, category, account, note, date, amount, id);
                realm.insertOrUpdate(transaction);
            }
            realm.commitTransaction();
            viewModel.getTransactions(calendar);
            Toast.makeText(MainActivity.this, "Đã nhập dữ liệu từ file Excel.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            if (realm.isInTransaction()) {
                realm.cancelTransaction();
            }
            throw e;
        } finally {
            workbook.close();
            inputStream.close();
            realm.close();
        }
    }

    private InputStream openExcelInputStream(Context context) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        Uri downloadsUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Downloads._ID};
        String selection = MediaStore.Downloads.DISPLAY_NAME + "=?";
        String[] selectionArgs = {"Transactions.xlsx"};
        String sortOrder = MediaStore.Downloads.DATE_MODIFIED + " DESC";

        try (Cursor cursor = resolver.query(downloadsUri, projection, selection, selectionArgs, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID));
                Uri fileUri = Uri.withAppendedPath(downloadsUri, String.valueOf(id));
                return resolver.openInputStream(fileUri);
            }
        }

        File appFile = new File(context.getExternalFilesDir(null), "Transactions.xlsx");
        if (appFile.exists()) {
            return new FileInputStream(appFile);
        }
        return null;
    }

    private long readLong(Cell cell, DataFormatter formatter) {
        if (cell == null) return System.currentTimeMillis();
        String text = formatter.formatCellValue(cell).replace(".", "").replace(",", "").trim();
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ignored) {
            try {
                return (long) cell.getNumericCellValue();
            } catch (Exception ignoredAgain) {
                return System.currentTimeMillis();
            }
        }
    }

    private Date parseExcelDate(String dateString) {
        String[] patterns = {"dd/MM/yyyy", "EEE MMM dd HH:mm:ss z yyyy"};
        for (String pattern : patterns) {
            try {
                Locale locale = pattern.startsWith("EEE") ? Locale.ENGLISH : Locale.getDefault();
                return new SimpleDateFormat(pattern, locale).parse(dateString);
            } catch (Exception ignored) {
            }
        }
        return new Date();
    }

    private String typeDisplayName(String type) {
        if (Constants.INCOME.equals(type)) return "Thu nhập";
        if (Constants.EXPENSE.equals(type)) return "Chi tiêu";
        return type;
    }

    private String typeStorageValue(String type) {
        if ("Thu nhập".equalsIgnoreCase(type) || Constants.INCOME.equalsIgnoreCase(type)) return Constants.INCOME;
        if ("Chi tiêu".equalsIgnoreCase(type) || Constants.EXPENSE.equalsIgnoreCase(type)) return Constants.EXPENSE;
        return type;
    }

    private String accountDisplayName(String accountValue) {
        if ("Cash".equals(accountValue)) return "Tiền mặt";
        if ("Bank".equals(accountValue)) return "Ngân hàng";
        if ("Pay pal".equals(accountValue)) return "PayPal";
        if ("Viettel Money".equals(accountValue)) return "Viettel Money";
        if ("Other".equals(accountValue)) return "Khác";
        return accountValue;
    }

    private String accountStorageValue(String accountValue) {
        if ("Tiền mặt".equalsIgnoreCase(accountValue) || "Cash".equalsIgnoreCase(accountValue)) return "Cash";
        if ("Ngân hàng".equalsIgnoreCase(accountValue) || "Bank".equalsIgnoreCase(accountValue)) return "Bank";
        if ("PayPal".equalsIgnoreCase(accountValue) || "Pay pal".equalsIgnoreCase(accountValue)) return "Pay pal";
        if ("Khác".equalsIgnoreCase(accountValue) || "Other".equalsIgnoreCase(accountValue)) return "Other";
        return accountValue;
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
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Tìm giao dịch...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.searchTransactions(calendar, query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.searchTransactions(calendar, newText);
                return true;
            }
        });
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                viewModel.getTransactions(calendar);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item == searchItem)
        {
            return true;
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
            switchMainFragment(new SettingsFragment());
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
