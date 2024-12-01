package com.example.movieclub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class login extends AppCompatActivity {

    private Button callSignUp, signInButton,forgetButton;
    private TextInputLayout emailInput, passwordInput;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private final int RC_SIGN_IN = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize Google Sign-In options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_webid)) // Ensure this matches the web client ID in Firebase
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        callSignUp = findViewById(R.id.signup);
        signInButton = findViewById(R.id.signin);
        forgetButton=findViewById(R.id.forget);
        callSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(login.this, Register.class);
            startActivity(intent);
        });
        forgetButton.setOnClickListener(view -> {
            Intent intent = new Intent(login.this, forgetpasswordActivity.class);
            startActivity(intent);
        });
        signInButton.setOnClickListener(view -> {
            String email = Objects.requireNonNull(emailInput.getEditText()).getText().toString().trim();
            String password = Objects.requireNonNull(passwordInput.getEditText()).getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(login.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });

        // Google Sign-In button click listener
        findViewById(R.id.google).setOnClickListener(view -> signInWithGoogle());
    }

    private void loginUser(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(login.this, Home.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(login.this, "Login Failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                } else {
                    Log.w("login", "Google sign-in failed, account is null");
                    Toast.makeText(this, "Google sign-in failed.", Toast.LENGTH_SHORT).show();
                }
            } catch (ApiException e) {
                Log.w("login", "Google sign-in failed", e);
                Toast.makeText(this, "Google sign-in failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        Toast.makeText(this, "Welcome " + (user != null ? user.getDisplayName() : "User"), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(login.this, Home.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.w("login", "signInWithCredential:failure", task.getException());
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
