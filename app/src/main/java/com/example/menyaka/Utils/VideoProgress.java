package com.example.menyaka.Utils;

import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

public class VideoProgress extends AsyncTask<Void, Integer, Void> {

    VideoView postVideoView;
    ProgressBar videoSeekbar;
    TextView onGoingDuration;
    public int duration;
    public int current = 0;

    public VideoProgress(VideoView postVideoView, ProgressBar videoSeekbar, TextView onGoingDuration, int duration, int current) {
        this.postVideoView = postVideoView;
        this.videoSeekbar = videoSeekbar;
        this.onGoingDuration = onGoingDuration;
        this.duration = duration;
        this.current = current;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        do{

            current = postVideoView.getCurrentPosition()/1000;
            publishProgress(current);

                /*try{
                    int currentPercent = current * 100/duration;
                    publishProgress(currentPercent);
                }catch (Exception ignored){}*/

        }while (videoSeekbar.getProgress() <= 100);

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        try{
            int currentPercent = values[0] * 100/duration;
            videoSeekbar.setProgress(currentPercent);

            String currentString = String.format("%02d:%02d", values[0] / 60, values[0] % 60);

            onGoingDuration.setText(currentString);

        }catch (Exception ignored){}
    }
}
