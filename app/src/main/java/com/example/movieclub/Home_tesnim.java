package com.example.movieclub;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderEffectBlur;
import eightbitlab.com.blurview.RenderScriptBlur;

public class Home_tesnim extends AppCompatActivity {

    PopupWindow popupWindow, popupWindow1;
    View v, collectionv;
    private AppDatabase database;
    private DAO dao_object;
    boolean notify = false;
    private TabLayout tabLayout;
    private BlurView bottomBlurView;
    private ViewGroup blurroot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize the Room database
        database = Room.databaseBuilder(
                this,
                AppDatabase.class,
                "AppDatabase"
        ).fallbackToDestructiveMigration().build();
        dao_object = database.dao();

        initView();
        setupBlurView();
        setupTabLayout();

        loadData("Trending", R.id.trendingRVIEW);
        loadData("NetflixOriginals", R.id.netflixoriginalsRVIEW);
        loadData("TopRated", R.id.topratedRVIEW);
        loadData("ActionMovies", R.id.actionmoviesRVIEW);
        loadData("ComedyMovies", R.id.comedymoviesRVIEW);
        loadData("HorrorMovies", R.id.horrormoviesRVIEW);
        loadData("RomanceMovies", R.id.romancemoviesRVIEW);

        ImageButton profile = findViewById(R.id.profile);
        MaterialButton movie = findViewById(R.id.movies);
        MaterialButton tv = findViewById(R.id.tvshows);

        movie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCollectionPopup("Movies", "Movie");
            }
        });

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCollectionPopup("TV Shows", "TV");
            }
        });

        ImageButton search = findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home_tesnim.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home_tesnim.this, Profile.class);
                startActivity(intent);
            }
        });
    }

    private void showCollectionPopup(String title, String dataType) {
        Context context = Home_tesnim.this;
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        collectionv = layoutInflater.inflate(R.layout.collections, null);
        TextView t = collectionv.findViewById(R.id.collection);
        t.setText(title);

        popupWindow1 = new PopupWindow(
                collectionv,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        popupWindow1.setEnterTransition(new Slide());
        popupWindow1.showAtLocation(collectionv, Gravity.BOTTOM, 0, 0);
        popupWindow1.setExitTransition(new Slide());
        int id = R.id.collectionRVIEW;
        loadData(dataType, id);
    }

    private void loadData(String collection, int rviewid) {
        RequestQueue queue = Volley.newRequestQueue(this);
        ArrayList<DataClass> arrayList = new ArrayList<>();
        Adapter adapter = new Adapter(Home_tesnim.this, arrayList, R.layout.itemposter);
        Adapter smalladapter = new Adapter(Home_tesnim.this, arrayList, R.layout.itemposter_small);
        RecyclerView recyclerView;

        GetURL getURL = new GetURL();
        String url = getURL.fetch(collection);

        if (collection.equals("Movie") || collection.equals("TV")) {
            recyclerView = collectionv.findViewById(rviewid);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(Home_tesnim.this, 3);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setAdapter(smalladapter);
        } else {
            recyclerView = findViewById(rviewid);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Home_tesnim.this, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(adapter);
        }

        adapter.setOnItemClickListener(new Adapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                showMovieDetails(arrayList.get(position));
            }
        });

        smalladapter.setOnItemClickListener(new Adapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                showMovieDetails(arrayList.get(position));
            }
        });

        String baseimageurl = "https://image.tmdb.org/t/p/original";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("results");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject movies = jsonArray.getJSONObject(i);
                                int key = movies.getInt("id");
                                String imgurl = baseimageurl + movies.getString("poster_path");
                                String herourl = baseimageurl + movies.getString("backdrop_path");
                                String title = movies.has("title") ? movies.getString("title") :
                                        movies.has("name") ? movies.getString("name") :
                                                movies.getString("original_title");
                                String description = movies.getString("overview");

                                DataClass data = new DataClass(key, imgurl, herourl, title, description, false);
                                checkIfAdded(data);
                                arrayList.add(data);
                            }
                            adapter.notifyDataSetChanged();
                            smalladapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Home_tesnim.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(stringRequest);
    }

    private void checkIfAdded(DataClass data) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                DataClass duplicate = dao_object.checkduplicateById(data.key);
                if (duplicate != null) {
                    data.setAdded(true);
                }
            }
        });
    }

    private void showMovieDetails(DataClass movie) {
        Context context = Home_tesnim.this;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.moviedetails, null);

        ImageView hero = view.findViewById(R.id.heroimage);
        TextView title = view.findViewById(R.id.title);
        TextView description = view.findViewById(R.id.description);
        MaterialButton button = view.findViewById(R.id.watchlist);

        Glide.with(view)
                .load(movie.getHerourl())
                .centerCrop()
                .into(hero);

        title.setText(movie.getTitle());
        description.setText(movie.getDescription());

        updateWatchlistButton(button, movie);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleWatchlistStatus(button, movie);
            }
        });

        popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setEnterTransition(new Slide());
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        popupWindow.setExitTransition(new Slide());
    }

    private void updateWatchlistButton(MaterialButton button, DataClass movie) {
        if (movie.added) {
            setButtonAdded(button);
        } else {
            setButtonNotAdded(button);
        }
    }

    private void setButtonAdded(MaterialButton button) {
        button.setText("Added to Watchlist");
        int color = getResources().getColor(R.color.md_theme_light_background);
        int textcolor = getResources().getColor(R.color.md_theme_dark_errorContainer);
        button.setBackgroundTintList(ColorStateList.valueOf(color));
        button.setTextColor(textcolor);
        button.setIconResource(R.drawable.baseline_check_24);
        button.setIconTint(ColorStateList.valueOf(textcolor));
    }

    private void setButtonNotAdded(MaterialButton button) {
        button.setText("Add to Watchlist");
        int color = getResources().getColor(R.color.md_theme_dark_errorContainer);
        int textcolor = getResources().getColor(R.color.md_theme_light_background);
        button.setBackgroundTintList(ColorStateList.valueOf(color));
        button.setTextColor(textcolor);
        button.setIconResource(R.drawable.baseline_star_24);
        button.setIconTint(ColorStateList.valueOf(textcolor));
    }

    private void toggleWatchlistStatus(MaterialButton button, DataClass movie) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                if (movie.added) {
                    dao_object.deleteDataById(movie.key);
                    movie.added = false;
                } else {
                    movie.added = true;
                    dao_object.insertOrUpdate(movie);
                }
                notify = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateWatchlistButton(button, movie);
                        refreshAllData();
                        notify = false;
                    }
                });
            }
        });
    }

    private void refreshAllData() {
        loadData("Trending", R.id.trendingRVIEW);
        loadData("NetflixOriginals", R.id.netflixoriginalsRVIEW);
        loadData("TopRated", R.id.topratedRVIEW);
        loadData("ActionMovies", R.id.actionmoviesRVIEW);
        loadData("ComedyMovies", R.id.comedymoviesRVIEW);
        loadData("HorrorMovies", R.id.horrormoviesRVIEW);
        loadData("RomanceMovies", R.id.romancemoviesRVIEW);
    }

    private void setupTabLayout() {
        tabLayout.getTabAt(0).select();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    // Watchlist tab selected
                    Intent intent = new Intent(Home_tesnim.this, Watchlist.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }


    private void vibrate() {
        Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vb != null && vb.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vb.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vb.vibrate(30);
            }
        }
    }

    private BlurAlgorithm getBlurAlgorithm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new RenderEffectBlur();
        } else {
            return new RenderScriptBlur(this);
        }
    }

    private void initView() {
        tabLayout = findViewById(R.id.tabLayout);
        bottomBlurView = findViewById(R.id.bottomBlurView);
        blurroot = findViewById(R.id.main);
    }

    private void setupBlurView() {
        final float radius = 25f;
        final Drawable windowBackground = getWindow().getDecorView().getBackground();

        bottomBlurView.setupWith(blurroot, new RenderScriptBlur(this))
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(radius);
    }

    @Override
    public void onBackPressed() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        } else if (popupWindow1 != null && popupWindow1.isShowing()) {
            popupWindow1.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        tabLayout.getTabAt(0).select();
    }
}

