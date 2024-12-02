package com.example.movieclub;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TrailerFetcher {
    private final Context context;

    public TrailerFetcher(Context context) {
        this.context = context;
    }

    public void fetchTrailer(String type, int id, final TrailerCallback callback) {
        GetURL urlHelper = new GetURL();
        String trailerURL = urlHelper.fetchTrailerURL(type, id);

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, trailerURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.getJSONArray("results");
                            if (results.length() > 0) {
                                JSONObject firstResult = results.getJSONObject(0);
                                String site = firstResult.getString("site");
                                String key = firstResult.getString("key");

                                if ("YouTube".equalsIgnoreCase(site)) {
                                    String youtubeURL = "https://www.youtube.com/watch?v=" + key;
                                    callback.onSuccess(youtubeURL);
                                } else {
                                    callback.onFailure("Trailer not found on YouTube");
                                }
                            } else {
                                callback.onFailure("No trailers found");
                            }
                        } catch (JSONException e) {
                            callback.onFailure("Error parsing response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onFailure("Network error");
                    }
                });

        queue.add(request);
    }

    public interface TrailerCallback {
        void onSuccess(String trailerURL);

        void onFailure(String errorMessage);
    }
}
