package com.example.payment;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.UUID;

import com.example.icardpossdk.BluetoothDevicesDialog;
import com.example.icardpossdk.ConnectionListener;
import com.example.icardpossdk.Currency;
import com.example.icardpossdk.POSHandler;
import com.example.icardpossdk.TransactionData;

public class Hello extends CordovaPlugin {
    private static final int    REQUEST_CODE_MAKE_PAYMENT  = 1;

    private POSHandler mPOSHandler;

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        final CordovaPlugin that = this;

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
                                        that.cordova.getActivity(),
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
