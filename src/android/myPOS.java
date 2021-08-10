package com.mrwinston.mypos;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import org.json.*;
import org.apache.cordova.*;

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
    private int REQUEST_CODE_MAKE_PAYMENT = 1;
    private int INTERVAL = 100;
    private Toast mToast;
    private Activity activity;
    private POSHandler mPOSHandler;
    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, final JSONArray data, final CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        if (action.equals("payment")) {
            activity = cordova.getActivity();
            
            // Set the callback for the activity result the Cordova activity
            cordova.setActivityResultCallback(myPOS.this);

            final ConnectionType connectionType = data.optString(1).equals("USB") ? ConnectionType.USB : ConnectionType.BLUETOOTH;
            
            POSHandler.setConnectionType(connectionType); // BLUETOOTH or USB
            POSHandler.setLanguage(Language.ENGLISH); // DUTCH not supported yet
            POSHandler.setCurrency(Currency.EUR);
            POSHandler.setApplicationContext(activity.getApplicationContext());
            POSHandler.setDefaultReceiptConfig(POSHandler.RECEIPT_PRINT_ONLY_MERCHANT_COPY);

            mPOSHandler = POSHandler.getInstance();

            if( mPOSHandler.isConnected() ) {
                // We are already connected, start the payment
                paymentViaActivityThread(data);
            }
            else {
                // We are not yet connected, listen for connections and attempt to connect
                mPOSHandler.setConnectionListener(new ConnectionListener() {
                    @Override
                    public void onConnected(final BluetoothDevice device) {
                        toast("Connected to PIN terminal, please (re-)start payment!");
                    }
                });

                // Needs to run on UI thread, otherwise the DevicesDialogue won't be closed
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mPOSHandler.connectDevice(activity);
                        } catch (Exception e) {
                            e.printStackTrace();
                            callbackContext.error("Error with connecting to device: " + e);
                        }
                    }
                });
            }

            return true;
        }

        return false;
    }

    private void paymentViaActivityThread(final JSONArray data) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                paymentViaActivity(data);
            }
        });

        thread.start();
    }

    private void paymentViaActivity(final JSONArray data, int ms) {
        if (ms <= 3000) {
            try {
                if (mPOSHandler.isTerminalBusy()) {
                    TimeUnit.MILLISECONDS.sleep(INTERVAL);

                    paymentViaActivity(
                        data,
                        ms + INTERVAL
                    );
                }
                else {
                    mPOSHandler.openPaymentActivity(
                        activity,
                        REQUEST_CODE_MAKE_PAYMENT,
                        data.getString(0),
                        UUID.randomUUID().toString()
                    );
                }
            }
            catch (InterruptedException e) {
                // Do nothing with InterruptedExceptions, for some reason
            }
            catch (Exception e) {
                // Toast all others
                toast(String.valueOf(e));
            }
        }
        else {
            toast("Terminal timeout occurred");
        }
    }

    private void paymentViaActivity(final JSONArray data) {
        paymentViaActivity(
            data,
            INTERVAL
        );
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        // If we have requested to make a payment (requestCode == 1)
        if( requestCode == REQUEST_CODE_MAKE_PAYMENT) {
            // And the result is OK (resultCode == -1)
            if (resultCode == RESULT_OK) {
                callbackContext.success();
            }
            else {
                // If the result is NOT OK, we remain in the SDK and allow the user to try again (or go back to Mr. Winston, without calling the callback)
                // TODO: consider whether we want to log errors here somehow (if so, we should probably make use of PluginResult.setKeepCallback)
                // callbackContext.error(resultCode);
            }
        }
    }

    private void toast(final String message) {
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
