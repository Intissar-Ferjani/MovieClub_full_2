package com.example.movieclub;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class contentDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_content_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.details), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialButton watchTrailerButton = findViewById(R.id.watchTrailor);

        watchTrailerButton.setOnClickListener(v -> {
            TrailerFetcher trailerFetcher = new TrailerFetcher(this);

            // Pass movie/TV type and ID (replace `movieId` and `tvId` with actual IDs)
            int movieId = 0; //temp just to not get error or push w error lol
            trailerFetcher.fetchTrailer("movie", movieId, new TrailerFetcher.TrailerCallback() {
                @Override
                public void onSuccess(String trailerURL) {
                    playTrailer(contentDetails.this, trailerURL);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(contentDetails.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    public void playTrailer(Context context, String youtubeURL) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeURL));
        intent.putExtra("force_fullscreen", true);
        context.startActivity(intent);
    }



}