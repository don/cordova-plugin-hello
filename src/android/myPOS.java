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
    private static final int REQUEST_CODE_MAKE_PAYMENT = 1;
    private static final int REQUEST_CODE_MAKE_REFUND = 2;

    private static final int INTERVAL = 100;

    private Toast mToast;

    private POSHandler mPOSHandler;

    private CallbackContext callbackContext = null;

    @Override
    public boolean execute(String action, final JSONArray data, final CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        if (action.equals("payment")) {
            final Activity activity = this.cordova.getActivity();
            final Context context = activity.getApplicationContext();
            final ConnectionType connectionType = data.optString(1).equals("USB") ? ConnectionType.USB : ConnectionType.BLUETOOTH;
            
            POSHandler.setConnectionType(connectionType); // BLUETOOTH or USB
            POSHandler.setLanguage(Language.ENGLISH); // DUTCH not supported yet
            POSHandler.setCurrency(Currency.EUR);
            POSHandler.setApplicationContext(context);
            POSHandler.setDefaultReceiptConfig(POSHandler.RECEIPT_PRINT_ONLY_MERCHANT_COPY);

            mPOSHandler = POSHandler.getInstance();

            if( mPOSHandler.isConnected() ) {
                // We are already connected, start the payment
                paymentViaActivityThread(
                    activity,
                    data
                );
            }
            else {
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

                // Needs to run on UI thread, otherwise it won't be closed
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPOSHandler.connectDevice(activity);
                    }
                });
            }

            // Set the callback for the activity result to this class
            cordova.setActivityResultCallback(myPOS.this);

            // Create a result and make sure the onActivityResult callback is available
            // PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);

            // result.setKeepCallback(true);

            // // TODO: implement onPOSTransactionComplete?
            // mPOSHandler.setPOSInfoListener(new POSInfoListener() {
            //     @Override
            //     public void onPOSInfoReceived(final int command, final int status, final String description) {
            //         // Handle the response here
            //     }
            
            //     @Override
            //     public void onTransactionComplete(final TransactionData transactionData) {
            //         this.cordova.getActivity().runOnUiThread(new Runnable() {
            //             public void run() {
            //                 callbackContext.success(transactionData.toString());
            //             }
            //         });
            //     }
            // });

            return true;
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if( requestCode == REQUEST_CODE_MAKE_PAYMENT && resultCode == RESULT_OK) {
                    callbackContext.success();
                }
                else {
                    callbackContext.error(0);
                }
            }
        });
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
        if (ms <= 12000) {
            try {
                if (mPOSHandler.isTerminalBusy()) {
                    TimeUnit.MILLISECONDS.sleep(INTERVAL);

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
