
/*import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class forgetpasswordActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button resetButton;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgetpassword);

        emailInput = findViewById(R.id.emailInput);
        resetButton = findViewById(R.id.resetButton);
        firebaseAuth = FirebaseAuth.getInstance();

        resetButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(forgetpasswordActivity.this, "Please enter your email address", Toast.LENGTH_SHORT).show();
            } else {
                resetPassword(email);
            }
        });
    }

    private void resetPassword(String email) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(forgetpasswordActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(forgetpasswordActivity.this, "Failed to send password reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
*/
package com.example.movieclub;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;

public class forgetpasswordActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button resetButton;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgetpassword);

        emailInput = findViewById(R.id.emailInput);
        resetButton = findViewById(R.id.resetButton);
        firebaseAuth = FirebaseAuth.getInstance();

        resetButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(forgetpasswordActivity.this, "Please enter your email address", Toast.LENGTH_SHORT).show();
            } else {
                sendPasswordResetEmailWithDynamicLink(email);
            }
        });
    }

    private void sendPasswordResetEmailWithDynamicLink(String email) {
        // Configure the password reset email with a Dynamic Link that directs the user back to the app
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl("https://movieclub.page.link/updateProfile")
                .setHandleCodeInApp(true)
                .setAndroidPackageName(
                        "com.example.movieclub", // Replace with your app's package name
                        true,  // install if not available
                        "12"   // minimum version, if applicable
                )
                .build();

        firebaseAuth.sendPasswordResetEmail(email, actionCodeSettings)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(forgetpasswordActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(forgetpasswordActivity.this, "Failed to send password reset email: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
