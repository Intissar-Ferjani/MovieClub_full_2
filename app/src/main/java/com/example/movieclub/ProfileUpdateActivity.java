package com.example.movieclub;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileUpdateActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);

        auth = FirebaseAuth.getInstance();

        // Check if user is logged in and direct them to update their data
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            // Show the form to update data
            updateUserData();
        } else {
            // Ask user to re-authenticate
        }
    }

    private void updateUserData() {
        // Add fields for updating user data
    }
}

