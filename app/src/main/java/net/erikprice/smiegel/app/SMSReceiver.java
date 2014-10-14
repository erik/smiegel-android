package net.erikprice.smiegel.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import net.erikprice.smiegel.api.APIClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SMSReceiver extends BroadcastReceiver {
    private static final String TAG = SMSForwarderTask.class.getCanonicalName();

    APIClient client;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        SMSForwarderTask task = new SMSForwarderTask();

        if (extras != null) {
            Object[] smsextras = (Object[]) extras.get("pdus");
            List<SmsMessage> messages = new ArrayList<>(smsextras.length);

            for (Object smsextra : smsextras) {
                task.execute(SmsMessage.createFromPdu((byte[]) smsextra));
            }
        }
    }

    private class SMSForwarderTask extends AsyncTask<SmsMessage, Void, Void> {
        @Override
        protected Void doInBackground(SmsMessage... messages) {
            for (SmsMessage msg : messages) {

                // TODO: need retry with backoff.
                try {
                    if (!client.addSms(msg)) {
                        Log.e(TAG, "Failed to forward sms...");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
