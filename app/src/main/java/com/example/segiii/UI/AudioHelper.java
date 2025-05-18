package com.example.segiii.UI;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class AudioHelper {
    private static final String TAG = "AudioHelper";
    private MediaPlayer mediaPlayer;
    private final Context context;

    public AudioHelper(Context context) {
        this.context = context;
    }

    public void playAudio(int resourceId) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            mediaPlayer = MediaPlayer.create(context, resourceId);
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(mp -> release());
                mediaPlayer.start();
                Log.d(TAG, "Playing audio resource: " + resourceId);
            } else {
                Log.e(TAG, "Failed to create MediaPlayer for resource: " + resourceId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing audio: " + e.getMessage());
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d(TAG, "MediaPlayer released");
        }
    }
}