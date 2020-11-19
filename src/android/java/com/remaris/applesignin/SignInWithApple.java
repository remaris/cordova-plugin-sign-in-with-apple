package com.remaris.applesignin;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONException;
import org.json.JSONObject;

public class SignInWithApple extends CordovaPlugin {

    private static final String TAG = "SignInWithApple";
    private static final int REQUEST_CODE = 514;

    private static final String ACTION_SIGNIN = "signin";
    private static final String CANCELLED = "cancelled";

    private CallbackContext mCallbackContext;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        this.mCallbackContext = callbackContext;

        if (ACTION_SIGNIN.equals(action)) {
            Intent signInIntent = new Intent(this.cordova.getContext(), SignInWithAppleActivity.class);
            this.cordova.startActivityForResult(this, signInIntent, REQUEST_CODE);
            return true;

        } else {
            Log.i(TAG, "This action doesn't exist");
            mCallbackContext.error("Unrecognized action: " + action);
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode != REQUEST_CODE) {
            return;
        }

        if (resultCode == Activity.RESULT_OK) {

            JSONObject result = new JSONObject();

            if (data.getExtras().getBoolean("success")) {
                try {
                    result.put("identityToken", data.getExtras().getString("sessionId"));
                } catch (JSONException e) {
                    mCallbackContext.error("Unexpected JSON error");
                }
                mCallbackContext.success(result);
            }

            if (data.getExtras().containsKey("message")) {
                try {
                    result.put("error", data.getExtras().getString("message"));
                } catch (JSONException e) {
                    mCallbackContext.error("Unexpected JSON error");
                }
                mCallbackContext.error(result);
            }

        } else if (resultCode == Activity.RESULT_CANCELED) {
            JSONObject result = new JSONObject();
            try {
                result.put(CANCELLED, true);
            } catch (JSONException e) {
                mCallbackContext.error("Unexpected JSON error");
            }
            mCallbackContext.success(result);

        } else {
            mCallbackContext.error("Unexpected error");
        }
    }

}