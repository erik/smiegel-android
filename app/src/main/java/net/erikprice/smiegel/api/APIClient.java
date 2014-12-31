package net.erikprice.smiegel.api;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.telephony.SmsMessage;
import android.util.Base64;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.provider.ContactsContract.CommonDataKinds.Phone;
import static android.provider.ContactsContract.Contacts;

public class APIClient {
    public static final String PREFS_NAME = "SmiegelPreferences";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Gson gson = new Gson();
    private final OkHttpClient client = new OkHttpClient();

    private final Crypt crypter;
    private final URL apiURL;
    private final String uid;

    public APIClient(URL serverUrl, String uid, String authToken, String sharedKey) {
        this.crypter = new Crypt(authToken, sharedKey);
        this.apiURL = serverUrl;
        this.uid = uid;
    }

    public static APIClient fromQR(String qrContents) {
        try {
            HashMap<String, Object> map = gson.fromJson(qrContents, HashMap.class);

            URL serverUrl = new URL((String) map.get("server"));
            String sharedKey = (String) map.get("shared_key");
            String authToken = (String) map.get("auth_token");
            String uid = (String) map.get("user_id");

            return new APIClient(serverUrl, uid, authToken, sharedKey);
        } catch (JsonSyntaxException | MalformedURLException e) {
            e.printStackTrace();

            return null;
        }
    }

    public static APIClient fromPreferences(SharedPreferences prefs) {
        String apiServer = prefs.getString("server", null);
        String sharedKey = prefs.getString("shared_key", null);
        String authToken = prefs.getString("auth_token", null);
        String uid = prefs.getString("user_id", null);

        try {
            return new APIClient(new URL(apiServer), uid, authToken, sharedKey);
        } catch (MalformedURLException e) {
            e.printStackTrace();

            return null;
        }
    }

    public static APIClient fromContext(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        boolean registered = settings.getBoolean("registered", false);

        if (registered) {
            return APIClient.fromPreferences(settings);
        }

        return null;
    }

    public void sendContacts(Context context) throws IOException {
        List<Map<String, Object>> contacts = new ArrayList<>();
        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(Contacts.CONTENT_URI, null, null, null, null);

        while (c.moveToNext()) {
            // Just skip anything that doesn't have a phone number
            if (c.getInt(c.getColumnIndex(Contacts.HAS_PHONE_NUMBER)) == 0) {
                continue;
            }

            List<Map<String, String>> numbers = new ArrayList<>();
            int id = c.getInt(c.getColumnIndex(Contacts._ID));
            Cursor pCursor = cr.query(
                    Phone.CONTENT_URI,
                    null,
                    Phone.CONTACT_ID + " = ?", new String[]{Integer.toString(id)},
                    null);

            while (pCursor.moveToNext()) {
                Map<String, String> entry = new HashMap<>();

                int type = pCursor.getInt(pCursor.getColumnIndex(Phone.TYPE));
                String typeStr = Phone.getTypeLabel(context.getResources(), type, "").toString();


                entry.put("number", pCursor.getString(pCursor.getColumnIndex(Phone.NUMBER)));
                entry.put("type", typeStr);

                numbers.add(entry);
            }

            Map<String, Object> contact = new HashMap<>();
            contact.put("name", c.getString(c.getColumnIndex(Contacts.DISPLAY_NAME)));
            contact.put("numbers", numbers);

            contacts.add(contact);
        }

        this.post("/api/contacts", gson.toJson(contacts));
    }

    private Response post(String path, String body) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("signature", crypter.authenticate(body));
        map.put("body", body);
        map.put("user_id", uid);

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

    public boolean addMessage(SmsMessage message) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("author", message.getDisplayOriginatingAddress());
        map.put("number", message.getDisplayOriginatingAddress());
        map.put("text", message.getMessageBody());
        map.put("timestamp", message.getTimestampMillis());

        String[] tuple = crypter.encryptShared(gson.toJson(map));
        Response response = this.post("/api/message/receive", gson.toJson(tuple));

        return validateResponse(response);
    }

    public Crypt getCrypt() {
        return crypter;
    }

    public URL getApiURL() {
        return apiURL;
    }

    public void serializeToPreferences(SharedPreferences.Editor editor) {
        editor.putString("server", apiURL.toString());
        editor.putString("user_id", uid);
        editor.putString("shared_key", Base64.encodeToString(crypter.getSharedKey(), Base64.NO_WRAP));
        editor.putString("auth_token", Base64.encodeToString(crypter.getAuthToken(), Base64.NO_WRAP));
    }
}
