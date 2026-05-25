package com.example.spending_management.views.fragments;

import static com.example.spending_management.utils.Constants.SELECTED_STATS_TYPE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.spending_management.R;
import com.example.spending_management.databinding.FragmentStatsBinding;
import com.example.spending_management.models.Category;
import com.example.spending_management.models.Transaction;
import com.example.spending_management.utils.Constants;
import com.example.spending_management.utils.Helper;
import com.example.spending_management.viewmodels.MainViewModel;
import com.example.spending_management.views.StatsChartView;
import com.google.android.material.tabs.TabLayout;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.realm.RealmResults;

public class StatsFragment extends Fragment {

    private FragmentStatsBinding binding;
    private Calendar calendar;
    public MainViewModel viewModel;
    private RealmResults<Transaction> currentTransactions;
    private int chartPage = 0;
    private float downX;

    public StatsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        calendar = Calendar.getInstance();
        setupControls();
        updateTypeButtons();
        updateDate();

        viewModel.categoriesTransactions.observe(getViewLifecycleOwner(), transactions -> {
            currentTransactions = transactions;
            renderStats();
        });

        return binding.getRoot();
    }

    private void setupControls() {
        binding.incomeBtn.setOnClickListener(view -> {
            SELECTED_STATS_TYPE = Constants.INCOME;
            chartPage = 0;
            updateTypeButtons();
            updateDate();
        });

        binding.expenseBtn.setOnClickListener(view -> {
            SELECTED_STATS_TYPE = Constants.EXPENSE;
            chartPage = 0;
            updateTypeButtons();
            updateDate();
        });

        binding.nextDateBtn.setOnClickListener(c -> {
            if (Constants.SELECTED_TAB_STATS == Constants.DAILY) {
                calendar.add(Calendar.DATE, 1);
            } else if (Constants.SELECTED_TAB_STATS == Constants.MONTHLY) {
                calendar.add(Calendar.MONTH, 1);
            }
            updateDate();
        });

        binding.previousDateBtn.setOnClickListener(c -> {
            if (Constants.SELECTED_TAB_STATS == Constants.DAILY) {
                calendar.add(Calendar.DATE, -1);
            } else if (Constants.SELECTED_TAB_STATS == Constants.MONTHLY) {
                calendar.add(Calendar.MONTH, -1);
            }
            updateDate();
        });

        binding.chartCard.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                downX = event.getX();
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                float delta = event.getX() - downX;
                if (Math.abs(delta) > 80) {
                    chartPage = delta < 0 ? Math.min(2, chartPage + 1) : Math.max(0, chartPage - 1);
                    renderStats();
                }
                return true;
            }
            return true;
        });

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText().equals("Monthly") || tab.getText().equals("Tháng")) {
                    Constants.SELECTED_TAB_STATS = Constants.MONTHLY;
                    updateDate();
                } else if (tab.getText().equals("Daily") || tab.getText().equals("Ngày")) {
                    Constants.SELECTED_TAB_STATS = Constants.DAILY;
                    updateDate();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void updateTypeButtons() {
        boolean isIncome = Constants.INCOME.equals(SELECTED_STATS_TYPE);
        binding.incomeBtn.setBackground(getContext().getDrawable(isIncome ? R.drawable.income_selector : R.drawable.type_unselected_selector));
        binding.expenseBtn.setBackground(getContext().getDrawable(!isIncome ? R.drawable.expense_selector : R.drawable.type_unselected_selector));
        binding.incomeBtn.setTextColor(getContext().getColor(isIncome ? R.color.greenColor : R.color.textPrimary));
        binding.expenseBtn.setTextColor(getContext().getColor(!isIncome ? R.color.redColor : R.color.textPrimary));
    }

    private void renderStats() {
        List<StatsChartView.ChartEntry> categoryEntries = buildCategoryEntries();
        List<StatsChartView.ChartEntry> chartEntries;
        if (chartPage == 1) {
            chartEntries = buildDateEntries();
        } else if (chartPage == 2) {
            chartEntries = buildTrendEntries();
        } else {
            chartEntries = categoryEntries;
        }

        double total = totalOf(chartEntries);
        binding.emptyStats.setVisibility(total <= 0 ? View.VISIBLE : View.GONE);
        binding.statsChartView.setChartData(chartEntries, total, chartPage);
        binding.chartDots.setText(chartPage == 0 ? "●  ○  ○" : chartPage == 1 ? "○  ●  ○" : "○  ○  ●");
        renderBreakdown(chartPage == 0 ? categoryEntries : chartEntries, chartPage == 0);
    }

    private List<StatsChartView.ChartEntry> buildCategoryEntries() {
        Map<String, Double> categoryMap = new LinkedHashMap<>();
        if (currentTransactions != null) {
            for (Transaction transaction : currentTransactions) {
                String category = transaction.getCategory();
                double amount = Math.abs(transaction.getAmount());
                categoryMap.put(category, categoryMap.getOrDefault(category, 0.0) + amount);
            }
        }

        List<StatsChartView.ChartEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            Category category = Constants.getCategoryDetails(entry.getKey());
            entries.add(new StatsChartView.ChartEntry(
                    entry.getKey(),
                    entry.getValue(),
                    getContext().getColor(category.getCategory_color())
            ));
        }
        return entries;
    }

    private List<StatsChartView.ChartEntry> buildDateEntries() {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM", Locale.getDefault());
        Map<String, Double> dateMap = new LinkedHashMap<>();
        if (currentTransactions != null) {
            for (Transaction transaction : currentTransactions) {
                String label = format.format(transaction.getDate());
                double amount = Math.abs(transaction.getAmount());
                dateMap.put(label, dateMap.getOrDefault(label, 0.0) + amount);
            }
        }

        int[] colors = {
                getContext().getColor(R.color.primaryBlueDark),
                getContext().getColor(R.color.aquaColor),
                getContext().getColor(R.color.greenColor),
                getContext().getColor(R.color.orange),
                getContext().getColor(R.color.redColor)
        };
        List<StatsChartView.ChartEntry> entries = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Double> entry : dateMap.entrySet()) {
            entries.add(new StatsChartView.ChartEntry(entry.getKey(), entry.getValue(), colors[index % colors.length]));
            index++;
        }
        return entries;
    }

    private List<StatsChartView.ChartEntry> buildTrendEntries() {
        Map<String, Double> dateMap = new LinkedHashMap<>();
        SimpleDateFormat labelFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

        if (Constants.SELECTED_TAB_STATS == Constants.MONTHLY) {
            Calendar cursor = (Calendar) calendar.clone();
            cursor.set(Calendar.DAY_OF_MONTH, 1);
            int maxDay = cursor.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int day = 1; day <= maxDay; day++) {
                cursor.set(Calendar.DAY_OF_MONTH, day);
                dateMap.put(labelFormat.format(cursor.getTime()), 0.0);
            }
        } else {
            Calendar cursor = (Calendar) calendar.clone();
            cursor.add(Calendar.DAY_OF_MONTH, -3);
            for (int i = 0; i < 7; i++) {
                dateMap.put(labelFormat.format(cursor.getTime()), 0.0);
                cursor.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        if (currentTransactions != null) {
            for (Transaction transaction : currentTransactions) {
                String label = labelFormat.format(transaction.getDate());
                double amount = Math.abs(transaction.getAmount());
                dateMap.put(label, dateMap.getOrDefault(label, 0.0) + amount);
            }
        }

        List<StatsChartView.ChartEntry> entries = new ArrayList<>();
        int lineColor = getContext().getColor(R.color.primaryBlueDark);
        for (Map.Entry<String, Double> entry : dateMap.entrySet()) {
            entries.add(new StatsChartView.ChartEntry(entry.getKey(), entry.getValue(), lineColor));
        }
        return entries;
    }

    private void renderBreakdown(List<StatsChartView.ChartEntry> entries, boolean useCategoryIcons) {
        binding.breakdownList.removeAllViews();
        double total = totalOf(entries);
        DecimalFormat percentFormat = new DecimalFormat("0.##");
        DecimalFormat moneyFormat = new DecimalFormat("#,###");

        for (StatsChartView.ChartEntry entry : entries) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(10), 0, dp(12));

            ImageView icon = new ImageView(requireContext());
            icon.setBackground(requireContext().getDrawable(R.drawable.category_bg));
            icon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(entry.color));
            if (useCategoryIcons) {
                Category category = Constants.getCategoryDetails(entry.label);
                icon.setImageResource(category.getCategory_image());
                icon.setColorFilter(requireContext().getColor(R.color.white));
                icon.setPadding(dp(11), dp(11), dp(11), dp(11));
            } else {
                icon.setImageDrawable(null);
                icon.setPadding(0, 0, 0, 0);
            }
            row.addView(icon, new LinearLayout.LayoutParams(dp(52), dp(52)));

            LinearLayout textColumn = new LinearLayout(requireContext());
            textColumn.setOrientation(LinearLayout.VERTICAL);
            LinearLayout titleRow = new LinearLayout(requireContext());
            titleRow.setOrientation(LinearLayout.HORIZONTAL);

            TextView title = new TextView(requireContext());
            title.setText(entry.label);
            title.setTextColor(requireContext().getColor(R.color.textPrimary));
            title.setTextSize(17);
            title.setTypeface(null, android.graphics.Typeface.BOLD);
            titleRow.addView(title);

            TextView percent = new TextView(requireContext());
            percent.setText("  " + percentFormat.format(total == 0 ? 0 : entry.value / total * 100) + "%");
            percent.setTextColor(requireContext().getColor(R.color.textSecondary));
            percent.setTextSize(16);
            titleRow.addView(percent);

            View bar = new View(requireContext());
            bar.setBackgroundColor(entry.color);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                    Math.max(dp(18), (int) (dp(220) * (total == 0 ? 0 : entry.value / total))),
                    dp(8)
            );
            barParams.setMargins(0, dp(8), 0, 0);

            textColumn.addView(titleRow);
            textColumn.addView(bar, barParams);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            textParams.setMargins(dp(14), 0, dp(10), 0);
            row.addView(textColumn, textParams);

            TextView amount = new TextView(requireContext());
            amount.setText(moneyFormat.format(entry.value).replace(",", "."));
            amount.setTextColor(requireContext().getColor(R.color.textPrimary));
            amount.setTextSize(15);
            row.addView(amount);

            binding.breakdownList.addView(row);
        }
    }

    private double totalOf(List<StatsChartView.ChartEntry> entries) {
        double total = 0;
        for (StatsChartView.ChartEntry entry : entries) {
            total += entry.value;
        }
        return total;
    }

    void updateDate() {
        if (Constants.SELECTED_TAB_STATS == Constants.DAILY) {
            binding.currentDate.setText(Helper.formatDate(calendar.getTime()));
        } else if (Constants.SELECTED_TAB_STATS == Constants.MONTHLY) {
            binding.currentDate.setText(Helper.formatDateByMonth(calendar.getTime()));
        }

        viewModel.getTransactions(calendar, SELECTED_STATS_TYPE);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
