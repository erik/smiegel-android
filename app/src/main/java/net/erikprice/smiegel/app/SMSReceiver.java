package net.erikprice.smiegel.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import net.erikprice.smiegel.api.APIClient;

import java.io.IOException;

public class SMSReceiver extends BroadcastReceiver {
    private static final String TAG = SMSForwarderTask.class.getCanonicalName();

    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        APIClient client = APIClient.fromContext(context);
        SMSForwarderTask task = new SMSForwarderTask(client);

        if (client == null) {
            return;
        }

        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        for (SmsMessage msg : messages) {
            task.execute(msg);
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
