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

    private static final int INTERVAL = 50;

    private Toast mToast;

    private POSHandler mPOSHandler;

    private CallbackContext callbackContext = null;

    @Override
    public boolean execute(String action, final JSONArray data, final CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        if (action.equals("payment")) {
            final Activity activity = this.cordova.getActivity();
            final Context context = activity.getApplicationContext();

            POSHandler.setConnectionType(ConnectionType.BLUETOOTH); // BLUETOOTH or USB
            POSHandler.setLanguage(Language.ENGLISH); // DUTCH not supported yet
            POSHandler.setCurrency(Currency.EUR);
            POSHandler.setApplicationContext(context);
            POSHandler.setDefaultReceiptConfig(POSHandler.RECEIPT_PRINT_ONLY_MERCHANT_COPY);

            mPOSHandler = POSHandler.getInstance();

            cordova.setActivityResultCallback(myPOS.this);

            if( mPOSHandler.isConnected() ) {
                // We are already connected, start the payment
                paymentViaActivityThread(
                    activity,
                    data
                );
            }
            else {
                // activity.runOnUiThread(new Runnable() {
                //     @Override
                //     public void run() {
                        mPOSHandler.connectDevice(activity);
                //     }
                // });

                // We are not yet connected, listen for connections and attempt to connect
                mPOSHandler.setConnectionListener(new ConnectionListener() {
                    @Override
                    public void onConnected(final BluetoothDevice device) {
                        paymentViaActivityThread(
                            activity,
                            data
                        );
                    }
                });
            }

            // Create a result and make sure the onActivityResult callback is available
            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);

            result.setKeepCallback(true);

            return true;
        }

        return false;
    }

    // TODO: implement onPOSTransactionComplete?
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == REQUEST_CODE_MAKE_PAYMENT && resultCode == RESULT_OK) {
            callbackContext.success();
        }
        else {
            callbackContext.error(0);
        }
    }

    private void paymentViaActivityThread(final Activity activity, final JSONArray data) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                paymentViaActivity(
                    activity,
                    data
                );
            }
        });

        thread.start();
    }

    private void paymentViaActivity(final Activity activity, final JSONArray data, int ms) {
        if (ms <= 10000) {
            try {
                TimeUnit.MILLISECONDS.sleep(INTERVAL);
            }
            catch (InterruptedException e) {
                // Do nothing, not sure why we throw here anyway
            }
    
            try {
                if (mPOSHandler.isTerminalBusy()) {
                    paymentViaActivity(
                        activity,
                        data,
                        ms + INTERVAL
                    );
                }
                else {
                    mPOSHandler.openPaymentActivity(
                        activity,
                        REQUEST_CODE_MAKE_PAYMENT,
                        data.optString(0),
                        UUID.randomUUID().toString()
                    );
                }
            }
            catch (final Exception e) {
                toast(activity, String.valueOf(e));
            }
        }
        else {
            toast(activity, "Timeout occurred");
        }
    }

    private void paymentViaActivity(final Activity activity, final JSONArray data) {
        paymentViaActivity(
            activity,
            data,
            INTERVAL
        );
    }

    private void toast(final Activity activity, final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mToast.makeText(
                    activity.getApplicationContext(),
                    message,
                    Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}
