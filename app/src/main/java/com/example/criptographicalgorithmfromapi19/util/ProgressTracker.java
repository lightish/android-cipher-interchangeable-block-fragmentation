package com.example.criptographicalgorithmfromapi19.util;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.criptographicalgorithmfromapi19.R;
import com.example.criptographicalgorithmfromapi19.ui.MainActivity;
import java.util.Queue;


public class ProgressTracker implements Runnable {
    public static final int UPDATE_FREQ = 250;
    private Queue<Integer> progressCallbacks;
    private MainActivity mainActivity;
    private String inProcessMessage;
    private String finishedMessage;

    public ProgressTracker(Queue<Integer> progressCallbacks, MainActivity mainActivity,
                           String inProcessMessage, String finishedMessage) {

        this.progressCallbacks = progressCallbacks;
        this.mainActivity = mainActivity;
        this.inProcessMessage = inProcessMessage;
        this.finishedMessage = finishedMessage;
    }

    @Override
    public void run() {

        Thread.currentThread().setPriority(6);
        int progressStatus = 0;
        final Fragment fragment = mainActivity.getProgressTrackingFragment();
        View view = fragment.getView();
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        final TextView progressText = view.findViewById(R.id.progressText);

        final FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                                .show(fragment).commit();
                        progressText.setText(inProcessMessage);
                    }
                });
        waitForUpdates();

        while(true) {
            if (!progressCallbacks.isEmpty()) {
                int progress = progressCallbacks.remove();
                if (progress == -1) {
                    break;
                }
                progressStatus += progress;
                progressBar.setProgress(progressStatus);

            } else {
                waitForUpdates();
            }
        }
        mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressText.setText(finishedMessage);
                        hideFragmentDelayed(fragmentManager, fragment,3000);
                    }
                });
    }
    private void waitForUpdates() {
        try {
            Thread.sleep(UPDATE_FREQ);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void hideFragmentDelayed(final FragmentManager fragmentManager, final Fragment fragment,
                                     long delayMillis) {
        Handler handler = new Handler();
        Runnable hidingRunnable = new Runnable() {
            @Override
            public void run() {
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .hide(fragment).commit();
            }
        };
        handler.postDelayed(hidingRunnable, delayMillis);
    }
}