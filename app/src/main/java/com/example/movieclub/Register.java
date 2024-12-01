package com.example.movieclub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.material.textfield.TextInputLayout;

public class Register extends AppCompatActivity {
    private Button callSignIn, btn;
    private TextInputLayout regName, regUsername, regEmail, regPassword, regphone;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private final int RC_SIGN_IN = 1001;
    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    String profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_webid))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        callSignIn = findViewById(R.id.signin);
        btn = findViewById(R.id.register);
        regName = findViewById(R.id.fullname);
        regUsername = findViewById(R.id.username);
        regEmail = findViewById(R.id.email);
        regphone = findViewById(R.id.phone);
        regPassword = findViewById(R.id.password);
        profileImage ="";

        callSignIn.setOnClickListener(view -> {
            Intent intent = new Intent(Register.this, login.class);
            startActivity(intent);
        });

        btn.setOnClickListener(view -> {
            String name = regName.getEditText().getText().toString();
            String username = regUsername.getEditText().getText().toString();
            String email = regEmail.getEditText().getText().toString();
            String password = regPassword.getEditText().getText().toString();
            String phone = regphone.getEditText().getText().toString();

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            rootNode = FirebaseDatabase.getInstance();
                            reference = rootNode.getReference("users");

                            UserHelperClass user = new UserHelperClass(name, username, phone, email, password, profileImage);
                            String userId = firebaseAuth.getCurrentUser().getUid();

                            reference.child(userId).setValue(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(Register.this, "User Registered Successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Register.this, login.class);
                                        startActivity(intent);
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(Register.this, "Failed to Register User", Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(Register.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        findViewById(R.id.google).setOnClickListener(v -> signInWithGoogle());
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
                    Log.w("Register", "Google sign-in account is null");
                    Toast.makeText(this, "Google sign-in failed.", Toast.LENGTH_SHORT).show();
                }
            } catch (ApiException e) {
                Log.w("Register", "Google sign-in failed", e);
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
                        if (user != null) {
                            String email = user.getEmail();
                            String name = user.getDisplayName();
                            String userId = user.getUid();

                            rootNode = FirebaseDatabase.getInstance();
                            reference = rootNode.getReference("users");

                            reference.child(userId).get().addOnSuccessListener(dataSnapshot -> {
                                if (!dataSnapshot.exists()) {
                                    UserHelperClass userHelper = new UserHelperClass(name, "", "", email, "","");
                                    reference.child(userId).setValue(userHelper)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(this, "Welcome " + name, Toast.LENGTH_SHORT).show();
                                                updateUI(user);
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to save user data.", Toast.LENGTH_SHORT).show());
                                } else {
                                    updateUI(user);
                                }
                            });
                        }
                    } else {
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            startActivity(new Intent(Register.this, Home.class));
        } else {
            Toast.makeText(this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
