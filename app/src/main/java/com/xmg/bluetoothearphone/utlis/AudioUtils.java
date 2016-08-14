package com.xmg.bluetoothearphone.utlis;

import android.content.Context;
import android.media.MediaPlayer;

import com.xmg.bluetoothearphone.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @des:单例设计
 * @author: 刘军
 * @version: 1.0.0
 * @date: 2016/6/14 10:45
 * @see {@link }
 */
public enum  AudioUtils {
    INSTANCE;
    private MediaPlayer mediaPlayer;
    private Timer mTimer;
    private TimerTask mTimerTask;

    public void playMedai(Context activity) {
        mediaPlayer= MediaPlayer.create(activity, R.raw.yiqianlengyiye);
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {

            }
        };
        mTimer.schedule(mTimerTask, 0, 10);
        mediaPlayer.start();
    }

    public  void stopPlay (){
        mediaPlayer.stop();
    }
}
