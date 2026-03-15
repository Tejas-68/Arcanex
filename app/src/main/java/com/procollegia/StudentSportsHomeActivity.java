package com.procollegia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ActivityOptions;
import android.util.Pair;

public class StudentSportsHomeActivity extends AppCompatActivity {

    private ImageView ivBack;
    private View cardBorrowEquipment, cardReturnEquipment, cardTournaments, cardSportsAttendance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_sports_home);

        ivBack = findViewById(R.id.iv_back);
        cardBorrowEquipment   = findViewById(R.id.card_borrow_equipment);
        cardReturnEquipment   = findViewById(R.id.card_return_equipment);
        cardTournaments       = findViewById(R.id.card_tournaments);
        cardSportsAttendance  = findViewById(R.id.card_sports_attendance);

        ivBack.setOnClickListener(v -> onBackPressed());

        cardBorrowEquipment.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentBorrowEquipmentActivity.class);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                    Pair.create(cardBorrowEquipment, "shared_card_borrow"));
            startActivity(intent, options.toBundle());
        });

        cardTournaments.setOnClickListener(v -> startActivity(new Intent(this, StudentSportsTournamentsActivity.class)));
        cardSportsAttendance.setOnClickListener(v -> startActivity(new Intent(this, StudentSportsAttendanceActivity.class)));
        BottomNavigationHelper.setup(this, R.id.nav_sports, "student");
    }
}
