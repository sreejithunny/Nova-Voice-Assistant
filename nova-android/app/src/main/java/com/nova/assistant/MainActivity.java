package com.nova.assistant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private static final String TAG = "NovaMainActivity";
    private static final int PERMISSION_REQUEST_CODE = 200;

    private WebView webView;
    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private AppManager appManager;
    private boolean isWakeWordActive = true;
    private boolean isTTSReady = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appManager = new AppManager(this);
        textToSpeech = new TextToSpeech(this, this);

        initWebView();
        checkAndRequestPermissions();
        initSpeechRecognizer();

        handleWakeUpIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleWakeUpIntent(intent);
    }

    private void handleWakeUpIntent(Intent intent) {
        if (intent != null && "ACTION_WAKE_UP".equals(intent.getAction())) {
            speakOut("Yes, I am listening.");
            runJs("ShowSiri()");
            startSpeechRecognition();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        webView = findViewById(R.id.novaWebView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new WebAppInterface(this), "NovaBridge");
        webView.loadUrl("file:///android_asset/www/index.html");
    }

    private void checkAndRequestPermissions() {
        ArrayList<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            startWakeService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            startWakeService();
            appManager.refreshAppList();
        }
    }

    private void startWakeService() {
        if (isWakeWordActive) {
            Intent serviceIntent = new Intent(this, NovaWakeService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    public void setWakeWordEnabled(boolean enabled) {
        this.isWakeWordActive = enabled;
        Intent serviceIntent = new Intent(this, NovaWakeService.class);
        if (enabled) {
            startWakeService();
            speakOut("Wake word Nova is activated.");
        } else {
            stopService(serviceIntent);
            speakOut("Wake word Nova has been paused.");
        }
    }

    public boolean isWakeWordEnabled() {
        return isWakeWordActive;
    }

    private void initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override public void onReadyForSpeech(Bundle params) {
                    runJs("DisplayMessage('Listening...')");
                    runJs("ShowSiri()");
                }

                @Override public void onBeginningOfSpeech() {}
                @Override public void onRmsChanged(float rmsdB) {}
                @Override public void onBufferReceived(byte[] buffer) {}
                @Override public void onEndOfSpeech() {
                    runJs("DisplayMessage('Processing...')");
                }

                @Override
                public void onError(int error) {
                    runJs("ShowHood()");
                    Log.e(TAG, "SpeechRecognizer error: " + error);
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String query = matches.get(0);
                        runJs("senderText('" + escapeJs(query) + "')");
                        handleUserCommand(query);
                    } else {
                        runJs("ShowHood()");
                    }
                }

                @Override public void onPartialResults(Bundle partialResults) {}
                @Override public void onEvent(int eventType, Bundle params) {}
            });
        }
    }

    public void startSpeechRecognition() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            if (speechRecognizer != null) {
                speechRecognizer.startListening(speechRecognizerIntent);
            }
        } else {
            checkAndRequestPermissions();
        }
    }

    public void handleUserCommand(String rawCommand) {
        if (rawCommand == null || rawCommand.trim().isEmpty()) return;
        String command = rawCommand.toLowerCase(Locale.ROOT).trim();
        runJs("DisplayMessage('" + escapeJs(rawCommand) + "')");

        // 1. OPEN APPLICATION (Any installed app!)
        if (command.startsWith("open") || command.startsWith("launch") || command.startsWith("start") || command.contains("open app")) {
            String appName = command.replace("open app", "")
                                    .replace("open", "")
                                    .replace("launch", "")
                                    .replace("start", "")
                                    .replace("nova", "")
                                    .trim();

            speakOut("Opening " + appName);
            boolean opened = appManager.openApplication(appName);
            if (!opened) {
                speakOut("I couldn't find " + appName + " on your phone. Checking Google Play...");
                try {
                    Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=" + Uri.encode(appName)));
                    playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(playStoreIntent);
                } catch (Exception e) {
                    speakOut("Unable to find the application.");
                }
            }
            runJs("receiverText('Opened " + escapeJs(appName) + "')");
            runJs("ShowHood()");
            return;
        }

        // 2. YOUTUBE PLAYBACK / SEARCH
        if (command.contains("youtube") || command.contains("play")) {
            String query = command.replace("play on youtube", "")
                                  .replace("on youtube", "")
                                  .replace("search youtube for", "")
                                  .replace("play", "")
                                  .replace("nova", "")
                                  .trim();
            speakOut("Playing " + query + " on YouTube");
            Intent ytIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(query)));
            ytIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(ytIntent);
            runJs("receiverText('Playing " + escapeJs(query) + " on YouTube')");
            runJs("ShowHood()");
            return;
        }

        // 3. GOOGLE SEARCH / WEB
        if (command.contains("search google") || command.contains("search for") || command.startsWith("what is") || command.startsWith("who is")) {
            String searchTerm = command.replace("search google for", "")
                                       .replace("search google", "")
                                       .replace("search for", "")
                                       .replace("nova", "")
                                       .trim();
            speakOut("Searching for " + searchTerm);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(searchTerm)));
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(browserIntent);
            runJs("receiverText('Searched Google for: " + escapeJs(searchTerm) + "')");
            runJs("ShowHood()");
            return;
        }

        // 4. PHONE CALL / DIALER
        if (command.startsWith("call") || command.startsWith("dial")) {
            String target = command.replace("call", "").replace("dial", "").trim();
            speakOut("Opening dialer for " + target);
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(target)));
            dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(dialIntent);
            runJs("receiverText('Calling " + escapeJs(target) + "')");
            runJs("ShowHood()");
            return;
        }

        // 5. TIME & DATE
        if (command.contains("time")) {
            String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
            replyAndDisplay("The current time is " + time);
            return;
        }
        if (command.contains("date") || command.contains("day")) {
            String date = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(new Date());
            replyAndDisplay("Today is " + date);
            return;
        }

        // 6. GREETINGS & PERSONALITY
        if (command.contains("who are you") || command.contains("your name")) {
            replyAndDisplay("I am Nova, your personal voice assistant.");
            return;
        }
        if (command.contains("hello") || command.contains("hi nova") || command.contains("hey")) {
            replyAndDisplay("Hello! How can I assist you today?");
            return;
        }
        if (command.contains("how are you")) {
            replyAndDisplay("I am running smoothly and ready for your commands!");
            return;
        }

        // Fallback: Web search
        replyAndDisplay("Here is what I found for " + rawCommand);
        Intent genericSearch = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(rawCommand)));
        genericSearch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(genericSearch);
    }

    private void replyAndDisplay(String response) {
        speakOut(response);
        runJs("receiverText('" + escapeJs(response) + "')");
        runJs("DisplayMessage('" + escapeJs(response) + "')");
        runJs("ShowHood()");
    }

    public void speakOut(String text) {
        if (textToSpeech != null && isTTSReady) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "NovaTTS");
        }
    }

    private void runJs(String script) {
        runOnUiThread(() -> {
            if (webView != null) {
                webView.evaluateJavascript(script, null);
            }
        });
    }

    private String escapeJs(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("'", "\\'")
                   .replace("\n", " ")
                   .replace("\r", "");
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTTSReady = true;
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }
}
