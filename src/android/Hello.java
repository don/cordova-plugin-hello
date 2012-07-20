package com.example.plugin;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.apache.cordova.api.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Hello extends Plugin {

    @Override
    public PluginResult execute(String action, JSONArray args, String callbackId) {
        
        if (action.equals("greet")) {

            try {
                String name = args.getString(0);
                String message = "Hello, " + name;

                return new PluginResult(Status.OK, message);

            } catch (JSONException e) {

                return new PluginResult(Status.JSON_EXCEPTION);
            }

        } else {
            
            return new PluginResult(Status.INVALID_ACTION);
            
        }
    }
}
