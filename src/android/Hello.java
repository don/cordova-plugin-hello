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
    @Override
    public boolean execute(String action, final JSONArray data, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("greet")) {
            final Activity activity = this.cordova.getActivity();

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final BluetoothDevicesDialog dialog = new BluetoothDevicesDialog(activity);

                    dialog.show();

                    POSHandler.setCurrency(Currency.EUR);

                    final POSHandler mPOSHandler = POSHandler.getInstance();

                    mPOSHandler.setConnectionListener(new ConnectionListener() {
                        @Override
                        public void onConnected(final BluetoothDevice device) {
                            if( POSHandler.getInstance().isConnected()){
                                mPOSHandler.purchase(
                                        "12.12",
                                        UUID.randomUUID().toString() /*transaction reference*/,
                                        POSHandler.RECEIPT_PRINT_AUTOMATICALLY /*receipt configuration*/
                                );
                            }
                            else {
                                final Toast toast = Toast.makeText(activity, "No terminal connected to this device.", Toast.LENGTH_SHORT);

                                toast.show();
                            }
                        }
                    });
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
