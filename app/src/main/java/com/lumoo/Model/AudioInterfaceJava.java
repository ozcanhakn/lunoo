package com.lumoo.Model;

import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.lumoo.AudioCallActivity;

public class AudioInterfaceJava {

    AudioCallActivity audioCallActivity;

    public AudioInterfaceJava(AudioCallActivity audioCallActivity) {
        this.audioCallActivity = audioCallActivity;
    }

    @JavascriptInterface
    public void onPeerConnected() {
        audioCallActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                audioCallActivity.onPeerConnected();
            }
        });
    }

    @JavascriptInterface
    public void onAudioStateChanged(boolean isEnabled) {
        audioCallActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                audioCallActivity.onAudioStateChanged(isEnabled);
            }
        });
    }

    @JavascriptInterface
    public void showToast(String message) {
        audioCallActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(audioCallActivity, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}