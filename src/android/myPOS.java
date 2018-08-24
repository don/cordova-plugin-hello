package com.mrwinston.mypos;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import eu.leupau.icardpossdk.ConnectionListener;
import eu.leupau.icardpossdk.ConnectionType;
import eu.leupau.icardpossdk.Currency;
import eu.leupau.icardpossdk.Language;
import eu.leupau.icardpossdk.POSHandler;
import eu.leupau.icardpossdk.POSInfoListener;
import eu.leupau.icardpossdk.TransactionData;

import static android.app.Activity.RESULT_OK;

public class myPOS extends CordovaPlugin {
    private static final int REQUEST_CODE_MAKE_PAYMENT = 1;
    private static final int REQUEST_CODE_MAKE_REFUND = 2;

    private static final int PERMISSION_COARSE_LOCATION = 1;

    private Toast mToast;

    private POSHandler mPOSHandler;

    private CallbackContext callbackContext = null;

    @Override
    public boolean execute(String action, final JSONArray data, final CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        if (action.equals("payment")) {
            final Activity activity = this.cordova.getActivity();
            final Context context = activity.getApplicationContext();

            POSHandler.setLanguage(Language.ENGLISH);
            POSHandler.setCurrency(Currency.EUR);
            POSHandler.setApplicationContext(context);
            POSHandler.setDefaultReceiptConfig(POSHandler.RECEIPT_PRINT_ONLY_MERCHANT_COPY);

            // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(context,  Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //     requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_COARSE_LOCATION);
            // }

            cordova.setActivityResultCallback(myPOS.this);

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    POSHandler.setConnectionType(ConnectionType.BLUETOOTH);

                    mPOSHandler = POSHandler.getInstance();

                    mToast.makeText(context, mPOSHandler.isConnected(), Toast.LENGTH_SHORT).show();

                    if( mPOSHandler.isConnected() ) {
                        mPOSHandler.openPaymentActivity(
                            activity,
                            REQUEST_CODE_MAKE_PAYMENT,
                            data.optString(0),
                            UUID.randomUUID().toString()
                        );
                    }
                    else {
                        mPOSHandler.connectDevice(activity);

                        mPOSHandler.setConnectionListener(new ConnectionListener() {
                            @Override
                            public void onConnected(final BluetoothDevice device) {
                                try {
                                    // TimeUnit.MILLISECONDS.sleep(100);

                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mPOSHandler.openPaymentActivity(
                                                    activity,
                                                    REQUEST_CODE_MAKE_PAYMENT,
                                                    data.optString(0),
                                                    UUID.randomUUID().toString()
                                            );
                                        }
                                    });
                                }
                                catch (Exception e) {

                                }
                            }
                        });
                    }
                }
            });

            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);

            result.setKeepCallback(true);

            return true;
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == REQUEST_CODE_MAKE_PAYMENT && resultCode == RESULT_OK) {
            callbackContext.success();
        }
        else {
            callbackContext.error(0);
        }
    }
}
