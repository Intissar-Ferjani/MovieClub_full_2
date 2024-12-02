package com.example.movieclub;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private Adapter smallAdapter;
    private ArrayList<DataClass> searchResults;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        searchView = findViewById(R.id.searchbar);
        recyclerView = findViewById(R.id.searchRVIEW);
        searchResults = new ArrayList<>();
        smallAdapter = new Adapter(this, searchResults, R.layout.itemposter_small);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(smallAdapter);

        queue = Volley.newRequestQueue(this);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });
    }

    private void performSearch(String query) {
        String searchUrl = new GetURL().fetch("Search");
        String[] words = query.trim().split("\\s+");
        String joinedQuery = TextUtils.join("+", words);
        String url = searchUrl.replace("+insertquery+", joinedQuery);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseSearchResults(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("SearchActivity", "Error: " + error.toString());
                    }
                });

        queue.add(stringRequest);
    }

    private void parseSearchResults(String response) {
        searchResults.clear();
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            String baseImageUrl = "https://image.tmdb.org/t/p/original";

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject movie = jsonArray.getJSONObject(i);
                int key = movie.getInt("id");
                String imgUrl = baseImageUrl + movie.getString("poster_path");
                String heroUrl = baseImageUrl + movie.getString("backdrop_path");
                String title = movie.has("title") ? movie.getString("title") :
                        movie.has("name") ? movie.getString("name") :
                                movie.getString("original_title");
                String description = movie.getString("overview");

                DataClass data = new DataClass(key, imgUrl, heroUrl, title, description, false);
                searchResults.add(data);
            }
            smallAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}