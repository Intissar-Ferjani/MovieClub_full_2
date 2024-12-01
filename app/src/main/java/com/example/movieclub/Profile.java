package com.example.movieclub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Profile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText fnameEditText, unameEditText, emailEditText, phoneEditText;
    private Button updateButton;
    private ImageView profileImageView, back, logout;

    private Uri imageUri;
    private DatabaseReference userRef;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Views
        fnameEditText = findViewById(R.id.fullName);
        unameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        phoneEditText = findViewById(R.id.phone);
        updateButton = findViewById(R.id.update);
        profileImageView = findViewById(R.id.profileimg);
        back = findViewById(R.id.back);
        logout = findViewById(R.id.logout);

        // Firebase References
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        storageRef = FirebaseStorage.getInstance().getReference("profile_images").child(userId + ".jpg");

        // Fetch data
        fetchUserData();

        // Image picker
        profileImageView.setOnClickListener(view -> openImagePicker());

        // Update data
        updateButton.setOnClickListener(view -> {
            updateUserData();
            uploadProfileImage();
        });

        back.setOnClickListener(v -> finish());
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(Profile.this, login.class));
            finish();
        });
    }

    private void fetchUserData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    fnameEditText.setText(snapshot.child("name").getValue(String.class));
                    unameEditText.setText(snapshot.child("username").getValue(String.class));
                    emailEditText.setText(snapshot.child("email").getValue(String.class));
                    phoneEditText.setText(snapshot.child("phone").getValue(String.class));

                    // Fetch and display the profile image
                    String profileImageUrl = snapshot.child("profileImage").getValue(String.class);
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(Profile.this).load(profileImageUrl).into(profileImageView);
                    } else {
                        // Set a default image if no profile image is available
                        profileImageView.setImageResource(R.drawable.img);
                    }
                } else {
                    Toast.makeText(Profile.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateUserData() {
        String fullName = fnameEditText.getText().toString().trim();
        String username = unameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.child("name").setValue(fullName);
        userRef.child("username").setValue(username);
        userRef.child("email").setValue(email);
        userRef.child("phone").setValue(phone)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Profile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Profile.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

    private void uploadProfileImage() {
        if (imageUri != null) {
            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        userRef.child("profileImage").setValue(imageUrl) // This line saves the URL to the database
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Profile.this, "Profile image updated!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(Profile.this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }))
                    .addOnFailureListener(e -> Toast.makeText(Profile.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}