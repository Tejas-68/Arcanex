package com.procollegia.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.procollegia.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PrincipalHomeFragment extends Fragment {

    private TextView tvWelcomeName;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_principal_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvWelcomeName = view.findViewById(R.id.tv_welcome_name);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        String name = doc.getString("name");
                        if (name != null && tvWelcomeName != null) {
                            String hour = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());
                            int h = Integer.parseInt(hour);
                            String greeting = h < 12 ? "Good Morning" : h < 17 ? "Good Afternoon" : "Good Evening";
                            tvWelcomeName.setText(greeting + ", " + name.split(" ")[0]);
                        }
                    });
        }
    }
}
