package net.erikprice.smiegel.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import net.erikprice.smiegel.R;
import net.erikprice.smiegel.api.APIClient;

import org.apache.commons.lang3.math.NumberUtils;

import java.net.MalformedURLException;
import java.net.URL;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;

public class SetupActivity extends Activity {
    private static final int REQUEST_QR = 0;

    @InjectView(R.id.hostField) TextView hostField;
    @InjectView(R.id.portField) TextView portField;
    @InjectView(R.id.sharedSecretField) TextView sharedSecretField;
    @InjectView(R.id.authTokenField) TextView authTokenField;

    @OnClick(R.id.submit_button)
    @Optional
    public void onSubmitButton() {
        String apiHost = hostField.getText().toString();
        int apiPort = NumberUtils.toInt(portField.getText().toString(), 0);
        String sharedKey = sharedSecretField.getText().toString();
        String authToken = authTokenField.getText().toString();

        if (apiHost.isEmpty()
                || apiPort > Short.MAX_VALUE || apiPort <= 0
                || sharedKey.isEmpty()
                || authToken.isEmpty()) {

            Toast toast = Toast.makeText(getApplicationContext(), "Fill out all fields!", Toast.LENGTH_SHORT);
            toast.show();

            return;
        }

        try {
            URL apiUrl = new URL("http", apiHost, apiPort, "/api");
            setApiClient(new APIClient(apiUrl, "TODO UID", authToken, sharedKey));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.qr_button)
    @Optional
    public void onQrButton() {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");

            startActivityForResult(intent, REQUEST_QR);
        } catch (ActivityNotFoundException e) {
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);

            startActivity(marketIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        ButterKnife.inject(this);

        // Just make sure.
        setResult(Activity.RESULT_CANCELED, new Intent());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_QR) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                Log.d("smiegel", contents);
                try {
                    setApiClient(APIClient.fromQR(contents));
                } catch (Exception e) { // TODO: be less broad
                    e.printStackTrace();

                    Toast toast = Toast.makeText(getApplicationContext(), "Gotta scan the right QR, you bum", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }
    }

    private void setApiClient(APIClient apiClient) {
        if (apiClient == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "Something you entered was wrong...", Toast.LENGTH_SHORT);
            toast.show();

            return;
        }

        SharedPreferences settings = getSharedPreferences(APIClient.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean("registered", true);
        apiClient.serializeToPreferences(editor);

        // Commit the edits!
        editor.commit();

        setResult(Activity.RESULT_OK, new Intent());
        finish();
    }
}

