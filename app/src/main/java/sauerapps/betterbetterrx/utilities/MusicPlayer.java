package sauerapps.betterbetterrx.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.Ui.Ui.FifteenMeditationActivity;

/**
 * Created by andrewsauer on 2/18/16.
 */
public class MusicPlayer {

    private static final String TAG = "bettermeditation";
    private static MusicPlayer sInstance;

    public static final String ARG_FROM_MUSIC = "arg";


    private static final String CMD_NAME = "command";
    private static final String CMD_PAUSE = "pause";
    private static final String CMD_STOP = "pause";
    private static final String CMD_PLAY = "play";


    // Jellybean
    private static String SERVICE_CMD = "sauerapps.betterbetterrx";
    private static String PAUSE_SERVICE_CMD = "sauerapps.betterbetterrx.pause";
    private static String PLAY_SERVICE_CMD = "sauerapps.betterbetterrx.play";

    // Honeycomb
    {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            SERVICE_CMD = "sauerapps.betterbetterrx";
            PAUSE_SERVICE_CMD = "sauerapps.betterbetterrx.pause";
            PLAY_SERVICE_CMD = "sauerapps.betterbetterrx.play";
        }
    };


    public static MusicPlayer getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MusicPlayer(context);
        }



        return sInstance;
    }


    private Context mContext;
    private boolean mAudioFocusGranted = false;
    private boolean mAudioIsPlaying = false;
    private MediaPlayer mPlayer;
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;
    private BroadcastReceiver mIntentReceiver;
    private boolean mReceiverRegistered = false;


    public MusicPlayer(Context context) {
        mContext = context;

        mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        Log.i(TAG, "AUDIOFOCUS_GAIN");
                        play();
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT");
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                        Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        Log.e(TAG, "AUDIOFOCUS_LOSS");
                        pause();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                        pause();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                        break;
                    case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                        Log.e(TAG, "AUDIOFOCUS_REQUEST_FAILED");
                        break;
                    default:
                        //
                }
            }
        };
    }


    public void play() {

        if (!mAudioIsPlaying) {
            if (mPlayer == null) {
                mPlayer = MediaPlayer.create(mContext, R.raw.fifteen_minute_meditation);
                mPlayer.setLooping(true);
            }

            // 1. Acquire audio focus
            if (!mAudioFocusGranted && requestAudioFocus()) {
                // 2. Kill off any other play back sources
                forceMusicStop();
                // 3. Register broadcast receiver for player intents
                setupBroadcastReceiver();
            }
            // 4. Play music
            mPlayer.start();
            mAudioIsPlaying = true;
        }
    }


    public void pause() {
        // 1. Suspend play back
        if (mAudioFocusGranted && mAudioIsPlaying) {
            mPlayer.pause();
            mAudioIsPlaying = false;
        }
    }


    public void stop() {
        // 1. Stop play back
        if (mAudioFocusGranted && mAudioIsPlaying) {
            mPlayer.stop();
            mPlayer = null;
            mAudioIsPlaying = false;
            // 2. Give up audio focus
            abandonAudioFocus();
        }
    }

    private boolean requestAudioFocus() {
        if (!mAudioFocusGranted) {
            AudioManager am = (AudioManager) mContext
                    .getSystemService(Context.AUDIO_SERVICE);
            // Request audio focus for play back
            int result = am.requestAudioFocus(mOnAudioFocusChangeListener,
                    // Use the music stream.
                    AudioManager.STREAM_MUSIC,
                    // Request permanent focus.
                    AudioManager.AUDIOFOCUS_GAIN);


            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mAudioFocusGranted = true;
            } else {
                // FAILED
                Log.e(TAG,
                        ">>>>>>>>>>>>> FAILED TO GET AUDIO FOCUS <<<<<<<<<<<<<<<<<<<<<<<<");
            }
        }
        return mAudioFocusGranted;
    }


    private void abandonAudioFocus() {
        AudioManager am = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        int result = am.abandonAudioFocus(mOnAudioFocusChangeListener);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mAudioFocusGranted = false;
        } else {
            // FAILED
            Log.e(TAG,
                    ">>>>>>>>>>>>> FAILED TO ABANDON AUDIO FOCUS <<<<<<<<<<<<<<<<<<<<<<<<");
        }
        mOnAudioFocusChangeListener = null;
    }


    private void setupBroadcastReceiver() {
        mIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String cmd = intent.getStringExtra(CMD_NAME);
                Log.i(TAG, "mIntentReceiver.onReceive " + action + " / " + cmd);


                if (PAUSE_SERVICE_CMD.equals(action)
                        || (SERVICE_CMD.equals(action) && CMD_PAUSE.equals(cmd))) {
                    play();
                }


                if (PLAY_SERVICE_CMD.equals(action)
                        || (SERVICE_CMD.equals(action) && CMD_PLAY.equals(cmd))) {
                    pause();
                }
            }
        };


        // Do the right thing when something else tries to play
        if (!mReceiverRegistered) {
            IntentFilter commandFilter = new IntentFilter();
            commandFilter.addAction(SERVICE_CMD);
            commandFilter.addAction(PAUSE_SERVICE_CMD);
            commandFilter.addAction(PLAY_SERVICE_CMD);
            mContext.registerReceiver(mIntentReceiver, commandFilter);
            mReceiverRegistered = true;
        }
    }


    private void forceMusicStop() {
        AudioManager am = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        if (am.isMusicActive()) {
            Intent intentToStop = new Intent(SERVICE_CMD);
            intentToStop.putExtra(CMD_NAME, CMD_STOP);
            mContext.sendBroadcast(intentToStop);
        }
    }
}