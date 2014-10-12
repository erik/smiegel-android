package net.erikprice.smiegel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

class SMSReceiver extends BroadcastReceiver {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        String strMessage = "";

        if (extras != null) {
            Object[] smsextras = (Object[]) extras.get("pdus");

            for (int i = 0; i < smsextras.length; i++) {
                SmsMessage smsMsg = SmsMessage.createFromPdu((byte[]) smsextras[i]);

                String strMsgBody = smsMsg.getMessageBody().toString();
                String strMsgSrc = smsMsg.getOriginatingAddress();

                strMessage += "SMS from " + strMsgSrc + " : " + strMsgBody;

                Log.i(TAG, strMessage);
            }

        }

    }

}
