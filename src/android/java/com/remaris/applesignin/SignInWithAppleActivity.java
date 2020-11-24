package com.remaris.applesignin;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Locale;

public class SignInWithAppleActivity extends Activity {

    public static final String TAG = "SignInWithApple";

    private String APPLE_LOGIN_URL = "https://appleid.apple.com/auth/authorize?client_id=hr.remaris.burgerkingcroatia.service&redirect_uri=https://burgerkingcrm.gm.speedy.remaris.com/Account/AppleRedirectUri&scope=name%20email&response_mode=form_post&response_type=code";

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LinearLayout layout = new LinearLayout(this);
        layout.setId(View.generateViewId());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(layoutParams);

        webView = new WebView(this);
        webView.setId(View.generateViewId());
        webView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        layout.addView(webView);

        setContentView(layout);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Detect redirect with return status
                if (handledRedirect(url)) {
                    return true;

                } else {
                    view.loadUrl(url);
                    return false;
                }
            }
        });

        // Override default if provided in config.xml
        try {
            Resources res = this.getResources();
            XmlResourceParser xml = res.getXml(getResourceID("config", "xml", getApplicationContext()));
            int eventType = -1;
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG) {
                    String strNode = xml.getName();
                    if (strNode.equals("preference")) {
                        String name = xml.getAttributeValue(null, "name").toLowerCase(Locale.getDefault());
                        if (name.equalsIgnoreCase("APPLE_LOGIN_URL")) {
                            APPLE_LOGIN_URL = xml.getAttributeValue(null, "value");
                        }
                    }
                }
                eventType = xml.next();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error reading config.xml: " + e);
        }

        webView.loadUrl(APPLE_LOGIN_URL);
    }

    protected final static int getResourceID(final String resName, final String resType, final Context ctx) {
        final int ResourceID = ctx.getResources().getIdentifier(resName, resType, ctx.getApplicationInfo().packageName);
        if (ResourceID == 0) {
            throw new IllegalArgumentException("No resource string found with name " + resName);
        } else {
            return ResourceID;
        }
    }

    private boolean handledRedirect(String urlStr) {
        Uri uri = Uri.parse(urlStr);

        if (!"AppleLogin".equals(uri.getLastPathSegment())) {
            return false;
        }

        boolean success = uri.getBooleanQueryParameter("success", false);
        if (success) {
            String sessionId = uri.getQueryParameter("sessionId");
            Log.d(TAG, "Success - sessionId: " + sessionId);

            Intent data = new Intent();
            data.putExtra("success", true);
            data.putExtra("sessionId", sessionId);
            setResult(RESULT_OK, data);
            finish();

        } else {
            String message = uri.getQueryParameter("message");
            Log.d(TAG, "Failed - message: " + message);

            Intent data = new Intent();
            data.putExtra("success", false);
            data.putExtra("message",message);
            setResult(RESULT_OK, data);
            finish();
        }

        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && this.webView.canGoBack()) {
            // Go back if I can
            this.webView.goBack();

        } else {
            // Handle as canceled result
            setResult(RESULT_CANCELED);
            finish();
        }
        return true;
    }
}
