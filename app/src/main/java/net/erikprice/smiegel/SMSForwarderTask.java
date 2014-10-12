package net.erikprice.smiegel;

import android.os.AsyncTask;
import android.telephony.SmsMessage;

import com.squareup.okhttp.OkHttpClient;

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
