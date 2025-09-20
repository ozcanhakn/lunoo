package com.lumoo.FCM;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FCMNotificationSender {
    private final String postUrl = "https://fcm.googleapis.com/v1/projects/lunoo-78c1d/messages:send";
    private final String userFcmToken;
    private final String title;
    private final String body;
    private final Context context;

    public FCMNotificationSender(String userFcmToken, String title, String body, Context context) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.context = context;
    }

    public void sendNotification() {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject mainObj = new JSONObject();
        try {
            JSONObject messageObj = new JSONObject();
            JSONObject dataObj = new JSONObject(); // **notification yerine data**

            dataObj.put("title", title);
            dataObj.put("body", body);

            messageObj.put("token", userFcmToken);
            messageObj.put("data", dataObj); // Burada notification değil data kullanıyoruz

            mainObj.put("message", messageObj);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj,
                    response -> Log.d("FCMResponse", "Response: " + response.toString()),
                    volleyError -> Log.e("FCMError", "Error: " + volleyError.toString())
            ) {
                @Override
                @NonNull
                public Map<String, String> getHeaders() {
                    AccessToken accessToken = new AccessToken();
                    String accessKey = accessToken.getAccessToken();
                    Map<String, String> header = new HashMap<>();
                    header.put("Content-Type", "application/json");
                    header.put("Authorization", "Bearer " + accessKey);
                    return header;
                }
            };
            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

