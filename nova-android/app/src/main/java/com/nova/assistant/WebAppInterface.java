package com.nova.assistant;

import android.webkit.JavascriptInterface;

public class WebAppInterface {
    private final MainActivity activity;

    public WebAppInterface(MainActivity activity) {
        this.activity = activity;
    }

    @JavascriptInterface
    public void startVoiceInput() {
        activity.runOnUiThread(activity::startSpeechRecognition);
    }

    @JavascriptInterface
    public void speak(String text) {
        activity.runOnUiThread(() -> activity.speakOut(text));
    }

    @JavascriptInterface
    public void processCommand(String text) {
        activity.runOnUiThread(() -> activity.handleUserCommand(text));
    }

    @JavascriptInterface
    public void toggleWakeWord(boolean enable) {
        activity.runOnUiThread(() -> activity.setWakeWordEnabled(enable));
    }

    @JavascriptInterface
    public boolean isWakeWordEnabled() {
        return activity.isWakeWordEnabled();
    }
}
