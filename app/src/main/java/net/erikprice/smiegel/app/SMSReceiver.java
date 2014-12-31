package net.erikprice.smiegel.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsMessage;

import net.erikprice.smiegel.api.APIClient;

import java.io.IOException;

public class SMSReceiver extends BroadcastReceiver {
    private static final String TAG = SMSForwarderTask.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        APIClient client = APIClient.fromContext(context);
        SMSForwarderTask task = new SMSForwarderTask(client);

        if (client == null) {
            return;
        }

        if (extras != null) {
            for (byte[] smsExtra : (byte[][]) extras.get("pdus")) {
                task.execute(SmsMessage.createFromPdu(smsExtra));
            }
        }
    }

    private class SMSForwarderTask extends AsyncTask<SmsMessage, Void, Void> {
        private final APIClient client;

        public SMSForwarderTask(APIClient client) {
            this.client = client;
        }

        @Override
        protected Void doInBackground(SmsMessage... messages) {
            // TODO: need retry with backoff.
            try {
                for (SmsMessage msg : messages) {
                    client.addMessage(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
