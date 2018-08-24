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
                mToast.makeText(context, "CONNECTED", Toast.LENGTH_SHORT).show();

                // We are already connected, start the payment
                paymentViaActivity(
                    activity,
                    data
                );
            }
            else {
                // We are not yet connected, listen for connections and attempt to connect
                mPOSHandler.setConnectionListener(new ConnectionListener() {
                    @Override
                    public void onConnected(final BluetoothDevice device) {
                        paymentViaActivity(
                            activity,
                            data
                        );
                    }
                });

                mPOSHandler.connectDevice(activity);
            }

            // Create a result and make sure the onActivityResult callback is available
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

    private void paymentViaActivity(final Activity activity, final JSONArray data) { //, int ms
        try { 
            TimeUnit.MILLISECONDS.sleep(50);

            if (mPOSHandler.isTerminalBusy()) {
                paymentViaActivity(
                    activity,
                    data
                    // ms++
                );
            }
            else {
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
        }
        catch (final Exception e) {
            final Context context = activity.getApplicationContext();

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mToast.makeText(context, String.valueOf(e), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    // private void paymentViaActivity(final Activity activity, final JSONArray data) {
    //     paymentViaActivity(
    //         activity,
    //         data,
    //         1
    //     );
    // }
}
