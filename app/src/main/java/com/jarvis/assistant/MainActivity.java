package com.jarvis.assistant;

import android.app.Activity;
import android.os.Bundle;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.res.ColorStateList;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int REQUEST_AUDIO = 100;
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech tts;
    private TextView statusText, answerText;
    private Button listenButton;
    private boolean ttsReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildInterface();

        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                int languageResult = tts.setLanguage(new Locale("ru", "RU"));
                ttsReady = languageResult != TextToSpeech.LANG_MISSING_DATA
                        && languageResult != TextToSpeech.LANG_NOT_SUPPORTED;
                if (!ttsReady) {
                    showAnswer("Русский голос недоступен. Установи русский голос в настройках синтеза речи Android.");
                }
            }
        });

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override public void onReadyForSpeech(Bundle params) {
                    statusText.setText("СЛУШАЮ, КОМАНДИР...");
                }
                @Override public void onBeginningOfSpeech() {
                    statusText.setText("РАСПОЗНАЮ РЕЧЬ");
                }
                @Override public void onRmsChanged(float rmsdB) { }
                @Override public void onBufferReceived(byte[] buffer) { }
                @Override public void onEndOfSpeech() {
                    statusText.setText("ОБРАБАТЫВАЮ КОМАНДУ...");
                }
                @Override public void onError(int error) {
                    statusText.setText("СИСТЕМА ГОТОВА");
                    showAnswer("Не удалось распознать речь. Попробуй ещё раз, командир.");
                }
                @Override public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        handleCommand(matches.get(0));
                    } else {
                        showAnswer("Команда не распознана, командир.");
                    }
                    statusText.setText("СИСТЕМА ГОТОВА");
                }
                @Override public void onPartialResults(Bundle partialResults) { }
                @Override public void onEvent(int eventType, Bundle params) { }
            });
        } else {
            showAnswer("На этом устройстве недоступно распознавание речи Android.");
            listenButton.setEnabled(false);
        }

        if (Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO);
        }
    }

    private void buildInterface() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(28, 35, 28, 35);
        root.setBackgroundColor(Color.rgb(5, 12, 25));

        TextView title = new TextView(this);
        title.setText("J.A.R.V.I.S.");
        title.setTextColor(Color.rgb(80, 205, 255));
        title.setTextSize(32);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = new TextView(this);
        subtitle.setText("PERSONAL AI ASSISTANT");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(12);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 8, 0, 45);
        root.addView(subtitle);

        TextView core = new TextView(this);
        core.setText("◉");
        core.setTextColor(Color.rgb(65, 190, 255));
        core.setTextSize(100);
        core.setGravity(Gravity.CENTER);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(Color.rgb(10, 35, 62));
        circle.setStroke(3, Color.rgb(40, 165, 230));
        core.setBackground(circle);
        LinearLayout.LayoutParams coreParams = new LinearLayout.LayoutParams(230, 230);
        coreParams.gravity = Gravity.CENTER;
        root.addView(core, coreParams);

        statusText = new TextView(this);
        statusText.setText("СИСТЕМА ГОТОВА");
        statusText.setTextColor(Color.rgb(90, 220, 255));
        statusText.setTextSize(14);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 28, 0, 16);
        root.addView(statusText);

        answerText = new TextView(this);
        answerText.setText("Здравствуйте, командир. Ожидаю вашу команду.");
        answerText.setTextColor(Color.WHITE);
        answerText.setTextSize(17);
        answerText.setGravity(Gravity.CENTER);
        answerText.setPadding(8, 8, 8, 30);
        root.addView(answerText);

        listenButton = new Button(this);
        listenButton.setText("🎙  СЛУШАТЬ КОМАНДУ");
        listenButton.setTextSize(14);
        listenButton.setTextColor(Color.WHITE);
        listenButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(0, 115, 175)));
        listenButton.setOnClickListener(v -> startListening());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(-1, -2);
        root.addView(listenButton, buttonParams);

        TextView hint = new TextView(this);
        hint.setText("Нажми кнопку и скажи: «Эй, Джарвис» или назови команду.");
        hint.setTextColor(Color.GRAY);
        hint.setTextSize(12);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, 20, 0, 0);
        root.addView(hint);

        setContentView(root);
    }

    private void startListening() {
        if (Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO);
            return;
        }
        if (speechRecognizer == null) return;

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ru-RU");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Слушаю, командир");
        speechRecognizer.startListening(intent);
    }

    private void handleCommand(String rawCommand) {
        String command = rawCommand.toLowerCase(new Locale("ru", "RU")).trim();
        answerText.setText("Вы: " + rawCommand);

        if (command.contains("эй джарвис") || command.contains("эй jarvis")
                || command.equals("джарвис") || command.equals("jarvis")) {
            speak("Да, слушаю, командир.");
        } else if (command.contains("который час") || command.contains("сколько времени")) {
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            speak("Сейчас " + time + ", командир.");
        } else if (command.contains("какая дата") || command.contains("какое сегодня число")) {
            String date = new SimpleDateFormat("d MMMM yyyy", new Locale("ru")).format(new Date());
            speak("Сегодня " + date + ", командир.");
        } else if (command.contains("как дела") || command.contains("состояние системы")) {
            speak("Все основные системы работают штатно. Ожидаю дальнейших указаний.");
        } else if (command.contains("ютуб") || command.contains("youtube")) {
            openApp("com.google.android.youtube", "YouTube");
        } else if (command.contains("хром") || command.contains("chrome") || command.contains("браузер")) {
            openApp("com.android.chrome", "Chrome");
        } else if (command.contains("майнкрафт") || command.contains("minecraft")) {
            openApp("com.mojang.minecraftpe", "Minecraft");
        } else if (command.contains("роблокс") || command.contains("roblox")) {
            openApp("com.roblox.client", "Roblox");
        } else if (command.contains("телеграм") || command.contains("telegram")) {
            openApp("org.telegram.messenger", "Telegram");
        } else if (command.contains("помощь") || command.contains("что ты умеешь")) {
            speak("Я умею сообщать время и дату, проверять состояние системы и открывать YouTube, Chrome, Minecraft, Roblox и Telegram.");
        } else {
            speak("Пока я не знаю такую команду, командир. Эту функцию можно добавить в следующей версии.");
        }
    }

    private void openApp(String packageName, String appName) {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            speak("Запускаю " + appName + ", командир.");
            startActivity(launchIntent);
        } else {
            speak(appName + " не найден на этом устройстве.");
        }
    }

    private void speak(String message) {
        showAnswer(message);
        if (tts != null && ttsReady) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "JARVIS_REPLY");
        }
    }

    private void showAnswer(String message) {
        answerText.setText(message);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showAnswer("Доступ к микрофону получен. Система готова, командир.");
            } else {
                showAnswer("Для голосовых команд необходимо разрешить доступ к микрофону.");
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
