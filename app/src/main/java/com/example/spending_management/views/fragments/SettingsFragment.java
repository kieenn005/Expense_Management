package com.example.spending_management.views.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.spending_management.R;

import java.util.Locale;


public class SettingsFragment extends Fragment {
    private Switch switchDarkMode;
    private TextView tvLanguage;

    public SettingsFragment() {
    }
    public boolean checker;
    public String checkerLanguage;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_settings, container, false);

        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        tvLanguage = view.findViewById(R.id.tv_current_language);

        SharedPreferences preferences = requireActivity().getSharedPreferences("settings", getContext().MODE_PRIVATE);
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);
        String currentLanguage = preferences.getString("language", "English");
        switchDarkMode.setChecked(isDarkMode);
        tvLanguage.setText(currentLanguage);

        checker = isDarkMode;
        checkerLanguage = currentLanguage;

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            Toast.makeText(getContext(), "Dark Mode " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
            if (checker != isChecked)
            {
                editor.putBoolean("checker", true);
                editor.apply();
            }
        });
        tvLanguage.setOnClickListener(v -> {
            String[] languages = {"English", "Tiếng Việt"};
            String[] languageCodes = {"en", "vi"}; // Mã ngôn ngữ tương ứng

            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Select Language")
                    .setItems(languages, (dialog, which) -> {
                        String selectedLanguage = languages[which];
                        String selectedLanguageCode = languageCodes[which];
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("language", selectedLanguage);
                        editor.putString("language_code", selectedLanguageCode);
                        editor.apply();
                        setLocale(selectedLanguageCode);
                        tvLanguage.setText(selectedLanguage);
                        Log.d(checkerLanguage, selectedLanguage);
                        if (checkerLanguage.equals(selectedLanguage) == false)
                        {
                            editor.putBoolean("checker", true);
                            editor.apply();
                        }
                        Toast.makeText(getContext(), "Language set to " + selectedLanguage, Toast.LENGTH_SHORT).show();
                        requireActivity().recreate();
                    })
                    .show();
        });

        return view;
    }

    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        requireContext().getResources().updateConfiguration(config, requireContext().getResources().getDisplayMetrics());

    }
}
