package com.procollegia;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PtCreateCompetitionActivity extends AppCompatActivity {

    private EditText etName, etVenue, etRules;
    private TextView tvDate, tvTeamSize, tvMaxParticipants;
    private Spinner spinnerSport;
    private SwitchCompat switchAiAuto;
    private View loadingOverlay;

    private int teamSize = 11;
    private int maxParticipants = 44;
    private Calendar selectedDate = Calendar.getInstance();

    private FirebaseFirestore db;

    private static final String[] SPORTS = {"Cricket", "Football", "Basketball", "Volleyball", "Badminton"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pt_create_competition);

        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.et_name);
        etVenue = findViewById(R.id.et_venue);
        etRules = findViewById(R.id.et_rules);
        tvDate = findViewById(R.id.tv_date);
        tvTeamSize = findViewById(R.id.tv_team_size);
        tvMaxParticipants = findViewById(R.id.tv_max_participants);
        spinnerSport = findViewById(R.id.spinner_sport);
        switchAiAuto = findViewById(R.id.switch_ai_auto);
        loadingOverlay = findViewById(R.id.loading_overlay);

        findViewById(R.id.iv_back).setOnClickListener(v -> onBackPressed());

        // Setup Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, SPORTS);
        spinnerSport.setAdapter(adapter);

        // Date Picker setup
        updateDateLabel();
        tvDate.setOnClickListener(v -> showDatePicker());
        // Alternatively whole layout click
        ((View)tvDate.getParent()).setOnClickListener(v -> showDatePicker());

        // Team Size Steppers
        findViewById(R.id.btn_team_minus).setOnClickListener(v -> {
            if (teamSize > 1) { teamSize--; tvTeamSize.setText(String.valueOf(teamSize)); }
        });
        findViewById(R.id.btn_team_plus).setOnClickListener(v -> {
            if (teamSize < 50) { teamSize++; tvTeamSize.setText(String.valueOf(teamSize)); }
        });

        // Max Participants Steppers
        findViewById(R.id.btn_max_minus).setOnClickListener(v -> {
            if (maxParticipants > 2) { maxParticipants -= 2; tvMaxParticipants.setText(String.valueOf(maxParticipants)); }
        });
        findViewById(R.id.btn_max_plus).setOnClickListener(v -> {
            if (maxParticipants < 200) { maxParticipants += 2; tvMaxParticipants.setText(String.valueOf(maxParticipants)); }
        });

        findViewById(R.id.btn_create_competition).setOnClickListener(v -> initiateCreation());
    }

    private void showDatePicker() {
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateLabel();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateLabel() {
        String format = "MMMM dd, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        tvDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void initiateCreation() {
        String name = etName.getText().toString().trim();
        String venue = etVenue.getText().toString().trim();
        String rules = etRules.getText().toString().trim();
        String sport = spinnerSport.getSelectedItem().toString();

        if (name.isEmpty() || venue.isEmpty()) {
            Toast.makeText(this, "Name and Venue are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
        findViewById(R.id.btn_create_competition).setEnabled(false);

        Map<String, Object> tournament = new HashMap<>();
        tournament.put("name", name);
        tournament.put("sport", sport);
        tournament.put("date", tvDate.getText().toString());
        tournament.put("venue", venue);
        tournament.put("teamSize", teamSize);
        tournament.put("slotsAvailable", maxParticipants); // assuming slots roughly equals max participants initally
        tournament.put("rules", rules);
        tournament.put("aiTeamFormation", switchAiAuto.isChecked());
        tournament.put("status", "open"); // Important for visibility

        db.collection("tournaments").add(tournament)
                .addOnSuccessListener(docRef -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(this, "Competition Created Successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Return to tournament list
                })
                .addOnFailureListener(e -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    findViewById(R.id.btn_create_competition).setEnabled(true);
                    Toast.makeText(this, "Failed to create competition", Toast.LENGTH_SHORT).show();
                });
    }
}
