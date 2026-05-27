package com.example.spending_management.views.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.spending_management.R;
import com.example.spending_management.databinding.FragmentChatbotBinding;
import com.example.spending_management.models.Category;
import com.example.spending_management.models.Transaction;
import com.example.spending_management.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmResults;

public class ChatbotFragment extends Fragment {
    private static final String AI_PREFS = "ai_chat";
    private static final String AI_HISTORY = "history";

    private FragmentChatbotBinding binding;
    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChatbotBinding.inflate(inflater, container, false);
        Constants.setCategories(requireContext());

        if (!loadHistory()) {
            addBotMessage("Chào bạn, mình có thể giúp hỏi đáp chi tiêu hoặc thêm giao dịch.\nVí dụ: \"Tháng này tôi tiêu bao nhiêu?\" hoặc \"Mua cà phê 35k bằng tiền mặt\".");
        }

        binding.sendButton.setOnClickListener(view -> handleSend());
        return binding.getRoot();
    }

    private void handleSend() {
        String message = binding.messageInput.getText().toString().trim();
        if (message.isEmpty()) return;

        addUserMessage(message);
        binding.messageInput.setText("");
        hideKeyboard();

        addBotMessage(handleMessage(message));
    }

    private String handleMessage(String message) {
        ParsedTransaction parsed = parseTransaction(message);
        if (parsed != null) {
            saveTransaction(parsed);
            return "Đã thêm " + typeDisplay(parsed.type).toLowerCase(Locale.getDefault())
                    + " " + formatMoney(Math.abs(parsed.amount))
                    + " vào danh mục " + parsed.category
                    + ", tài khoản " + accountDisplay(parsed.account)
                    + ", ngày " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(parsed.date) + ".";
        }

        return answerQuestion(message);
    }

    private String answerQuestion(String message) {
        String normalized = normalize(message);
        Calendar[] range = resolveRange(normalized);
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<Transaction> transactions = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", range[0].getTime())
                    .lessThan("date", range[1].getTime())
                    .findAll();

            double income = 0;
            double expense = 0;
            Map<String, Double> expenseByCategory = new HashMap<>();
            for (Transaction transaction : transactions) {
                double amount = transaction.getAmount();
                if (Constants.INCOME.equals(transaction.getType())) {
                    income += amount;
                } else if (Constants.EXPENSE.equals(transaction.getType())) {
                    expense += Math.abs(amount);
                    String category = transaction.getCategory();
                    expenseByCategory.put(category, expenseByCategory.getOrDefault(category, 0.0) + Math.abs(amount));
                }
            }

            if (normalized.contains("nhieu nhat") || normalized.contains("lon nhat") || normalized.contains("danh muc")) {
                String topCategory = null;
                double topAmount = 0;
                for (Map.Entry<String, Double> entry : expenseByCategory.entrySet()) {
                    if (entry.getValue() > topAmount) {
                        topCategory = entry.getKey();
                        topAmount = entry.getValue();
                    }
                }
                if (topCategory == null) return "Khoảng thời gian này chưa có khoản chi nào.";
                return "Bạn chi nhiều nhất cho " + topCategory + ": " + formatMoney(topAmount) + ".";
            }

            if (normalized.contains("con bao nhieu") || normalized.contains("so du") || normalized.contains("tong")) {
                return "Trong khoảng này: thu nhập " + formatMoney(income)
                        + ", chi tiêu " + formatMoney(expense)
                        + ", còn lại " + formatMoney(income - expense) + ".";
            }

            if (normalized.contains("thu")) {
                return "Thu nhập trong khoảng này là " + formatMoney(income) + ".";
            }
            if (normalized.contains("chi") || normalized.contains("tieu")) {
                return "Chi tiêu trong khoảng này là " + formatMoney(expense) + ".";
            }

            return "Mình chưa hiểu rõ câu hỏi. Bạn có thể hỏi: \"Hôm nay tôi tiêu bao nhiêu?\", \"Tháng này còn bao nhiêu?\", hoặc nhập: \"Mua đồ ăn 50k\".";
        } finally {
            realm.close();
        }
    }

    private ParsedTransaction parseTransaction(String message) {
        Double amount = parseAmount(message);
        if (amount == null || amount <= 0) return null;

        String normalized = normalize(message);
        String type = resolveType(normalized);
        String category = resolveCategory(message, type);
        String account = resolveAccount(normalized);
        Date date = resolveDate(normalized);

        double signedAmount = Constants.EXPENSE.equals(type) ? -amount : amount;
        return new ParsedTransaction(type, category, account, message, date, signedAmount);
    }

    private Double parseAmount(String message) {
        String normalized = normalize(message);
        Matcher matcher = Pattern.compile("(\\d[\\d\\.,]*)(\\s*)(trieu|tr|k|nghin|ngan)?").matcher(normalized);
        while (matcher.find()) {
            String raw = matcher.group(1);
            String unit = matcher.group(3);
            try {
                double value;
                if (unit != null) {
                    value = Double.parseDouble(raw.replace(",", "."));
                    if (unit.equals("trieu") || unit.equals("tr")) return value * 1_000_000;
                    if (unit.equals("k") || unit.equals("nghin") || unit.equals("ngan")) return value * 1_000;
                } else {
                    value = Double.parseDouble(raw.replace(".", "").replace(",", ""));
                    if (value >= 1000) return value;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private String resolveType(String normalized) {
        if (normalized.contains("luong") || normalized.contains("thu nhap") || normalized.contains("nhan")
                || normalized.contains("thuong") || normalized.contains("lam them")) {
            return Constants.INCOME;
        }
        return Constants.EXPENSE;
    }

    private String resolveCategory(String message, String type) {
        String normalized = normalize(message);
        for (Category category : Constants.getCategories(type)) {
            if (normalized.contains(normalize(category.getCategoryName()))) {
                return category.getCategoryName();
            }
        }
        if (Constants.INCOME.equals(type)) {
            if (normalized.contains("luong")) return "Lương";
            if (normalized.contains("thuong")) return "Tiền thưởng";
            if (normalized.contains("dau tu")) return "Khoản đầu tư";
            if (normalized.contains("lam them")) return "Làm thêm";
            return "Khác";
        }
        if (normalized.contains("an") || normalized.contains("com") || normalized.contains("cafe") || normalized.contains("ca phe")) return "Đồ ăn";
        if (normalized.contains("mua") || normalized.contains("shopping") || normalized.contains("sieu thi")) return "Mua sắm";
        if (normalized.contains("dien thoai")) return "Điện thoại";
        if (normalized.contains("giai tri")) return "Giải trí";
        if (normalized.contains("di lai") || normalized.contains("xe") || normalized.contains("grab")) return "Đi lại";
        if (normalized.contains("nha")) return "Nhà ở";
        if (normalized.contains("suc khoe") || normalized.contains("thuoc")) return "Sức khỏe";
        return "Khác";
    }

    private String resolveAccount(String normalized) {
        if (normalized.contains("ngan hang") || normalized.contains("bank")) return "Bank";
        if (normalized.contains("paypal")) return "Pay pal";
        if (normalized.contains("viettel")) return "Viettel Money";
        if (normalized.contains("khac")) return "Other";
        return "Cash";
    }

    private Date resolveDate(String normalized) {
        Calendar calendar = Calendar.getInstance();
        if (normalized.contains("hom qua")) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Calendar[] resolveRange(String normalized) {
        Calendar start = Calendar.getInstance();
        Calendar end;
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        if (normalized.contains("thang")) {
            start.set(Calendar.DAY_OF_MONTH, 1);
            end = (Calendar) start.clone();
            end.add(Calendar.MONTH, 1);
        } else if (normalized.contains("tuan")) {
            start.set(Calendar.DAY_OF_WEEK, start.getFirstDayOfWeek());
            end = (Calendar) start.clone();
            end.add(Calendar.DAY_OF_MONTH, 7);
        } else if (normalized.contains("hom qua")) {
            start.add(Calendar.DAY_OF_MONTH, -1);
            end = (Calendar) start.clone();
            end.add(Calendar.DAY_OF_MONTH, 1);
        } else {
            end = (Calendar) start.clone();
            end.add(Calendar.DAY_OF_MONTH, 1);
        }
        return new Calendar[]{start, end};
    }

    private void saveTransaction(ParsedTransaction parsed) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            Transaction transaction = new Transaction(
                    parsed.type,
                    parsed.category,
                    parsed.account,
                    parsed.note,
                    parsed.date,
                    parsed.amount,
                    System.currentTimeMillis()
            );
            realm.copyToRealmOrUpdate(transaction);
            realm.commitTransaction();
        } finally {
            realm.close();
        }
    }

    private void addUserMessage(String message) {
        addMessage(message, true, true);
    }

    private void addBotMessage(String message) {
        addMessage(message, false, true);
    }

    private void addMessage(String message, boolean isUser, boolean shouldSave) {
        TextView bubble = new TextView(requireContext());
        bubble.setText(message);
        bubble.setTextSize(15);
        bubble.setTextColor(requireContext().getColor(isUser ? R.color.white : R.color.textPrimary));
        bubble.setBackground(requireContext().getDrawable(isUser ? R.drawable.chat_user_bg : R.drawable.chat_bot_bg));
        bubble.setPadding(dp(12), dp(10), dp(12), dp(10));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = isUser ? Gravity.END : Gravity.START;
        params.setMargins(isUser ? dp(54) : 0, dp(6), isUser ? 0 : dp(54), dp(6));
        binding.messages.addView(bubble, params);
        binding.chatScroll.post(() -> binding.chatScroll.fullScroll(View.FOCUS_DOWN));
        if (shouldSave) {
            saveMessage(message, isUser);
        }
    }

    private boolean loadHistory() {
        SharedPreferences preferences = requireContext().getSharedPreferences(AI_PREFS, Context.MODE_PRIVATE);
        String rawHistory = preferences.getString(AI_HISTORY, "[]");
        try {
            JSONArray history = new JSONArray(rawHistory);
            for (int i = 0; i < history.length(); i++) {
                JSONObject item = history.getJSONObject(i);
                addMessage(item.optString("text"), item.optBoolean("isUser"), false);
            }
            return history.length() > 0;
        } catch (JSONException e) {
            preferences.edit().remove(AI_HISTORY).apply();
            return false;
        }
    }

    private void saveMessage(String message, boolean isUser) {
        SharedPreferences preferences = requireContext().getSharedPreferences(AI_PREFS, Context.MODE_PRIVATE);
        String rawHistory = preferences.getString(AI_HISTORY, "[]");
        try {
            JSONArray history = new JSONArray(rawHistory);
            JSONObject item = new JSONObject();
            item.put("text", message);
            item.put("isUser", isUser);
            history.put(item);
            preferences.edit().putString(AI_HISTORY, history.toString()).apply();
        } catch (JSONException ignored) {
        }
    }

    private String normalize(String value) {
        String text = Normalizer.normalize(value.toLowerCase(Locale.getDefault()), Normalizer.Form.NFD);
        return text.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replace("đ", "d");
    }

    private String formatMoney(double value) {
        return moneyFormat.format(value).replace(",", ".") + " VND";
    }

    private String typeDisplay(String type) {
        return Constants.INCOME.equals(type) ? "Thu nhập" : "Chi tiêu";
    }

    private String accountDisplay(String account) {
        return Constants.accountDisplayName(account);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.messageInput.getWindowToken(), 0);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private static class ParsedTransaction {
        final String type;
        final String category;
        final String account;
        final String note;
        final Date date;
        final double amount;

        ParsedTransaction(String type, String category, String account, String note, Date date, double amount) {
            this.type = type;
            this.category = category;
            this.account = account;
            this.note = note;
            this.date = date;
            this.amount = amount;
        }
    }
}
