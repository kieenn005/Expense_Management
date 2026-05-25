package com.example.spending_management.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.spending_management.R;
import com.example.spending_management.databinding.FragmentMoreBinding;
import com.example.spending_management.views.activities.MainActivity;

public class MoreFragment extends Fragment {
    private FragmentMoreBinding binding;

    public MoreFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMoreBinding.inflate(inflater, container, false);

        binding.aiChat.setOnClickListener(view ->
                ((MainActivity) requireActivity()).openChatbot());
        binding.exportExcel.setOnClickListener(view ->
                ((MainActivity) requireActivity()).exportExcel(requireContext()));
        binding.importExcel.setOnClickListener(view ->
                ((MainActivity) requireActivity()).pickExcelFile());
        binding.clearData.setOnClickListener(view ->
                ((MainActivity) requireActivity()).ClearData());

        return binding.getRoot();
    }
}
