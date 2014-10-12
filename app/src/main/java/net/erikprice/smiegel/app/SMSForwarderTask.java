package net.erikprice.smiegel.app;

import android.os.AsyncTask;
import android.telephony.SmsMessage;

import com.squareup.okhttp.OkHttpClient;

import net.erikprice.smiegel.api.APIClient;

public class SMSForwarderTask extends AsyncTask<SmsMessage, Void, Void> {
    APIClient client;

    protected Void doInBackground(SmsMessage... messages) {
        for (SmsMessage msg : messages) {
            OkHttpClient ok = new OkHttpClient();
            if (isCancelled()) break;
        }

        return null;
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPostExecute(Void v) {
    }
}
