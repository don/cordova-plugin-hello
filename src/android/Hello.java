package com.mrwinston.mypos;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.widget.Toast;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.UUID;

import eu.leupau.icardpossdk.BluetoothDevicesDialog;
import eu.leupau.icardpossdk.ConnectionListener;
import eu.leupau.icardpossdk.Currency;
import eu.leupau.icardpossdk.POSHandler;
import eu.leupau.icardpossdk.TransactionData;

public class Hello extends CordovaPlugin {
    private static final int REQUEST_CODE_MAKE_PAYMENT  = 1;
    private static final int REQUEST_CODE_MAKE_REFUND   = 2;

    @Override
    public boolean execute(String action, final JSONArray data, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("greet")) {
            final Activity activity = this.cordova.getActivity();

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    POSHandler.setCurrency(Currency.EUR);

                    final POSHandler mPOSHandler = POSHandler.getInstance();

                    if( POSHandler.getInstance().isConnected()){
                        mPOSHandler.openPaymentActivity(
                                activity,
                                REQUEST_CODE_MAKE_PAYMENT,
                                "0.12",
                                UUID.randomUUID().toString() /*transaction reference*/
                        );
                    }
                    else {
                        final BluetoothDevicesDialog dialog = new BluetoothDevicesDialog(activity);

                        dialog.show();

                        mPOSHandler.setConnectionListener(new ConnectionListener() {
                            @Override
                            public void onConnected(final BluetoothDevice device) {
                                mPOSHandler.openPaymentActivity(
                                        activity,
                                        REQUEST_CODE_MAKE_PAYMENT,
                                        "0.12",
                                        UUID.randomUUID().toString() /*transaction reference*/
                                );
                            }
                        });
                    }
                }
            });

            String name = data.optString(0);
            String message = "Hello, " + name;
            callbackContext.success(message);

            return true;
        } else {
            return false;
        }
    }
}
