package net.erikprice.smiegel.app;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import net.erikprice.smiegel.app.SMSReceiver;

public class ServiceCommunicator extends Service {
    private SMSReceiver mSMSReceiver;
    private IntentFilter mIntentFilter;

    @Override
    public void onCreate() {
        super.onCreate();

        //SMS event receiver
        mSMSReceiver = new SMSReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(mSMSReceiver, mIntentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the SMS receiver
        unregisterReceiver(mSMSReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}