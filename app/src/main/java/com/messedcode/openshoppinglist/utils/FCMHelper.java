package com.messedcode.openshoppinglist.utils;

import android.content.Context;
import android.util.Log;

import com.messedcode.openshoppinglist.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * https://firebase.google.com/docs/cloud-messaging/send-message#send_messages_using_the_legacy_app_server_protocols
 */
public class FCMHelper {

    private static final String TAG = "FCMHelper";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient client;
    private static String serverKey = null;

    public static void init(Context context) {
        client = new OkHttpClient();
        serverKey = context.getString(R.string.fcm_server_key).trim();
    }

    public static boolean isServerKeyValid() {
        return serverKey != null && serverKey.length() > 0;
    }

    public static void sendNotification(String topic, String notificationTitle, String notificationBody) {
        sendNotification(topic, notificationTitle, notificationBody, null);
    }

    public static void sendNotification(String topic, String notificationTitle, String notificationBody, final Callback cb) {
        if (!isServerKeyValid()) {
            Log.i(TAG, "FCM Server Key is invalid, skipping sending of notification");
            return;
        }

        // Build body
        JSONObject json = new JSONObject();
        try {
            json.put("to", "/topics/" + topic);
            JSONObject notification = new JSONObject();
            notification.put("title", notificationTitle);
            notification.put("body", notificationBody);
            json.put("notification", notification);
        } catch (JSONException e) {
            Log.e(TAG, "sendNotification failed to create notification JSONObject: " + e.toString());
        }

        // Setup request
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(JSON, json.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "key=" + serverKey)
                .build();

        // Send request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "sendNotification onFailure : " + e.toString());

                if (cb != null) {
                    cb.onFailure(call, e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.v(TAG, "sendNotification successful: " + response.body().string());
                } else {
                    Log.e(TAG, "sendNotification unsucessful: " + response.body().string());
                }

                if (cb != null) {
                    cb.onResponse(call, response);
                }
            }
        });
    }

}
