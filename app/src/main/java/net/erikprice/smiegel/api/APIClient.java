package net.erikprice.smiegel.api;

import android.content.SharedPreferences;
import android.telephony.SmsMessage;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class APIClient {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Gson gson = new Gson();

    private final OkHttpClient client = new OkHttpClient();

    private final Crypt crypter;
    private URL apiURL;

    public APIClient(String apiHost, int apiPort, String authToken, String sharedKey) {
        crypter = new Crypt(authToken, sharedKey);

        try {
            this.apiURL = new URL("https", apiHost, apiPort, "/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static APIClient fromQR(String qrContents) {
        try {
            HashMap<String, Object> map = gson.fromJson(qrContents, HashMap.class);

            String apiHost = (String) map.get("host");
            Integer apiPort = ((Double) map.get("port")).intValue();
            String sharedKey = (String) map.get("shared_key");
            String authToken = (String) map.get("auth_token");

            return new APIClient(apiHost, apiPort, authToken, sharedKey);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();

            return null;
        }
    }

    public static APIClient fromPreferences(SharedPreferences prefs) {
        String apiHost = prefs.getString("host", null);
        int apiPort = prefs.getInt("port", -1);
        String sharedKey = prefs.getString("shared_key", null);
        String authToken = prefs.getString("auth_token", null);

        return new APIClient(apiHost, apiPort, authToken, sharedKey);
    }

    private Response post(String path, String body) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("signature", crypter.authenticate(body));
        map.put("body", body);

        Request request = new Request.Builder()
                .url(new URL(apiURL, path))
                .post(RequestBody.create(JSON, gson.toJson(map)))
                .build();

        return client.newCall(request).execute();
    }

    private boolean validateResponse(Response response) {
        if (!response.isSuccessful()) {
            return false;
        }

        try {
            HashMap<String, String> map = gson.fromJson(response.body().string(), HashMap.class);

            String signature = map.get("signature");
            String body = map.get("body");

            return crypter.authenticate(signature, body);
        } catch (JsonSyntaxException | IOException e) {
            return false;
        }
    }

    public boolean addSms(SmsMessage message) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("sender", message.getDisplayOriginatingAddress());
        map.put("body", message.getMessageBody());
        map.put("timestamp", Long.toString(message.getTimestampMillis()));

        String[] tuple = crypter.encryptShared(gson.toJson(map));
        Response response = post("sms", gson.toJson(tuple));

        return validateResponse(response);
    }

    public Crypt getCrypt() {
        return crypter;
    }

    public void serializeToPreferences(SharedPreferences.Editor editor) {
        editor.putString("host", apiURL.getHost());
        editor.putInt("port", apiURL.getPort());
        editor.putString("shared_key", Base64.encodeToString(crypter.getSharedKey(), Base64.DEFAULT));
        editor.putString("auth_token", Base64.encodeToString(crypter.getAuthToken(), Base64.DEFAULT));
    }
}