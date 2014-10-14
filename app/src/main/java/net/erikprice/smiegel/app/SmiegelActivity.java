package net.erikprice.smiegel.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import net.erikprice.smiegel.R;
import net.erikprice.smiegel.api.APIClient;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class SmiegelActivity extends Activity {
    public static final String PREFS_NAME = "SmiegelPreferences";
    private static final int REQUEST_SETUP = 0;

    @InjectView(R.id.host) TextView hostView;
    @InjectView(R.id.port) TextView portView;
    @InjectView(R.id.auth_token) TextView authTokenView;
    @InjectView(R.id.shared_secret) TextView sharedSecretView;

    private APIClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smiegel);

        ButterKnife.inject(this);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean registered = settings.getBoolean("registered", false);

        // If the device isn't yet registered go ahead and prompt for the set up.
        if (!registered) {
            Toast toast = Toast.makeText(getApplicationContext(), "Scan the QR code in your browser", Toast.LENGTH_SHORT);
            toast.show();

            Intent intent = new Intent(this, SetupActivity.class);
            startActivityForResult(intent, 0);
        } else {
            apiClient = APIClient.fromPreferences(settings);

            updateFields();
        }
    }

    private void updateFields() {
        hostView.setText(apiClient.getApiURL().getHost());
        portView.setText("" + apiClient.getApiURL().getPort());

        authTokenView.setText(Base64.encodeToString(apiClient.getCrypt().getAuthToken(), Base64.DEFAULT));
        sharedSecretView.setText(Base64.encodeToString(apiClient.getCrypt().getSharedKey(), Base64.DEFAULT));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SETUP) {
            if (resultCode == RESULT_OK) {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                apiClient = APIClient.fromPreferences(settings);

                updateFields();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.smiegel, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
