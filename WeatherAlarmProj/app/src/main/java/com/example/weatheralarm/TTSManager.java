package com.example.weatheralarm;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class TTSManager {

    private TextToSpeech tts;
    static private TTSManager ttsManager;

    private TTSManager(Context c)
    {
        this.tts = new TextToSpeech(c, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                    //tts.speak("epic bruh moment", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }

    static public TTSManager getInstance(Context c)
    {
        if(ttsManager == null)
        {
            ttsManager = new TTSManager(c);
        }
        return ttsManager;
    }

    public void saySomething(String s)
    {
        tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
    }
}
