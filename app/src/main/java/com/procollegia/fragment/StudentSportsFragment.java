package com.procollegia.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.procollegia.R;
import com.procollegia.StudentBorrowEquipmentActivity;
import com.procollegia.StudentSportsTournamentsActivity;
import com.procollegia.StudentSportsAttendanceActivity;

public class StudentSportsFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_student_sports_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View cardBorrow = view.findViewById(R.id.card_borrow_equipment);
        View cardReturn = view.findViewById(R.id.card_return_equipment);
        View cardTournaments = view.findViewById(R.id.card_tournaments);
        View cardSportsAtt = view.findViewById(R.id.card_sports_attendance);

        if (cardBorrow != null)
            cardBorrow.setOnClickListener(v -> startActivity(new Intent(requireActivity(), StudentBorrowEquipmentActivity.class)));
        if (cardReturn != null)
            cardReturn.setOnClickListener(v -> startActivity(new Intent(requireActivity(), StudentBorrowEquipmentActivity.class)));
        if (cardTournaments != null)
            cardTournaments.setOnClickListener(v -> startActivity(new Intent(requireActivity(), StudentSportsTournamentsActivity.class)));
        if (cardSportsAtt != null)
            cardSportsAtt.setOnClickListener(v -> startActivity(new Intent(requireActivity(), StudentSportsAttendanceActivity.class)));
    }
}
