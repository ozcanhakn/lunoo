package com.lumoo.Model;

import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.lumoo.CallActivity;

public class InterfaceJava {

    CallActivity callActivity;

    public InterfaceJava(CallActivity callActivity) {
        this.callActivity = callActivity;
    }

    @JavascriptInterface
    public void onPeerConnected() {
        callActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callActivity.onPeerConnected();
            }
        });
    }

    @JavascriptInterface
    public void showToast(String message) {
        callActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(callActivity, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @JavascriptInterface
    public void onAudioStateChanged(boolean isEnabled) {
        callActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callActivity.onAudioStateChanged(isEnabled);
            }
        });
    }

    @JavascriptInterface
    public void onVideoStateChanged(boolean isEnabled) {
        callActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callActivity.onVideoStateChanged(isEnabled);
            }
        });
    }
}