package com.mrwinston.mypos;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.UUID;

import icardpossdk.BluetoothDevicesDialog;
import icardpossdk.ConnectionListener;
import icardpossdk.Currency;
import icardpossdk.POSHandler;
import icardpossdk.TransactionData;

public class Hello extends CordovaPlugin {
    private static final int    REQUEST_CODE_MAKE_PAYMENT  = 1;

    private POSHandler mPOSHandler;

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("greet")) {
            POSHandler.setCurrency(Currency.EUR);
            mPOSHandler = POSHandler.getInstance();

            mPOSHandler.setConnectionListener(new ConnectionListener() {
                @Override
                public void onConnected(final BluetoothDevice device) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mStatus.setText("Connected");

                            mTerminalType.setVisibility(View.VISIBLE);

                            mTerminalType.setText(device.getName().equalsIgnoreCase("") ? device.getAddress() : device.getName());

                            setEnabled(true);

                            if( POSHandler.getInstance().isConnected()){
                                mPOSHandler.openPaymentActivity(
                                        this.cordova.getActivity(),
                                        REQUEST_CODE_MAKE_PAYMENT /*requestCode*/,
                                        "12.12",
                                        UUID.randomUUID().toString()
                                );
                            }
                        }
                    });
                }
            });

            String name = data.getString(0);
            String message = "Hello, " + name;
            callbackContext.success(message);

            return true;

        } else {
            
            return false;

        }
    }
}
