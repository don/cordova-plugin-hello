package com.example.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

public class Hello extends CordovaPlugin {
    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
            /*if() to select what method of the class should be used.
            *on this class, we have only the greet method
            */
        if (action.equals("greet")) {
            String name = data.getString(0);
            this.greet(name, callbackContext);
            return true;
        } else {
            return false;
        }
        //the method return the String message to the sucessCallback(sucess function added on index.js)
        private void greet(String name, CallbackContext callbackContext){
            String message = "Hello, " + name;
            callbackContext.success(message);
        }
    }
}
