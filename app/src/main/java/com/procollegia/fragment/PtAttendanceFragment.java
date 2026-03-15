package com.procollegia.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.procollegia.BarcodeScannerActivity;
import com.procollegia.R;

public class PtAttendanceFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_pt_attendance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View ivBack = view.findViewById(R.id.iv_back);
        if (ivBack != null) ivBack.setVisibility(View.GONE);

        View btnScan = view.findViewById(R.id.btn_scan_barcode);
        if (btnScan != null)
            btnScan.setOnClickListener(v -> startActivity(new Intent(requireActivity(), BarcodeScannerActivity.class)));
    }
}
