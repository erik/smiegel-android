package net.erikprice.smiegel.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import net.erikprice.smiegel.R;
import net.erikprice.smiegel.api.APIClient;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

public class SetupActivity extends Activity {
    private static final int REQUEST_QR = 0;

    @OnClick(R.id.submit_button)
    @Optional
    public void onSubmitButton() {

    }

    @OnClick(R.id.qr_button)
    @Optional
    public void onQrButton() {
        Log.d("asdf", "qr clicked");

        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");

            startActivityForResult(intent, REQUEST_QR);
        } catch (Exception e) {
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_QR) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                try {
                    APIClient apiClient = APIClient.fromQR(contents);

                    SharedPreferences settings = getSharedPreferences(SmiegelActivity.PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();

                    editor.putBoolean("registered", true);
                    apiClient.serializeToPreferences(editor);

                    // Commit the edits!
                    editor.commit();

                    setResult(Activity.RESULT_OK, new Intent());
                    finish();
                } catch (Exception e) { // TODO: be less broad
                    e.printStackTrace();

                    Toast toast = Toast.makeText(getApplicationContext(), "Gotta scan the right QR, you bum", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }
    }
}

