package sauerapps.betterbetterrx.Ui.Ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;
import android.os.Handler;

import butterknife.Bind;
import butterknife.ButterKnife;
import sauerapps.betterbetterrx.BaseActivity;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.utilities.MusicPlayer;

public class FifteenMeditationActivity extends BaseActivity {

    private static final String TAG = FifteenMeditationActivity.class.getSimpleName();

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

    @Bind(R.id.toolbar) protected Toolbar mToolbar;
    @Bind(R.id.seekBar) protected SeekBar mSeekBar;
    @Bind(R.id.songDuration) protected TextView mDuration;
    @Bind(R.id.media_play) protected ImageButton mPlayButton;
    @Bind(R.id.media_pause) protected ImageButton mPauseButton;
    @Bind(R.id.reset_recording) protected ImageButton mResetRecording;
    @Bind(R.id.gratitude_button) protected ImageButton mGratitudeButton;

    private Handler durationHandler = new Handler();
    private double timeElapsed = 0, finalTime = 0;
    private MediaPlayer mMediaPlayer;
    private boolean mAudioIsPlaying = false;
    private boolean mAudioFocusGranted = false;
    private BroadcastReceiver mIntentReceiver;
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;
    private boolean mReceiverRegistered = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fifteen_meditation);
        ButterKnife.bind(this);

        //initialize views
        initializeScreen();

        mPlayButton.setOnClickListener(playMeditation);
        mPauseButton.setOnClickListener(pauseMeditation);
        mResetRecording.setOnClickListener(resetMeditation);
        mGratitudeButton.setOnClickListener(gratitudeButton);

        mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        Log.i(TAG, "AUDIOFOCUS_GAIN");
                        playMeditationActions();
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT");
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                        Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        Log.e(TAG, "AUDIOFOCUS_LOSS");
                        pauseMeditationActions();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                        pauseMeditationActions();
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

    @Override
    protected void onPause () {
        super.onPause();
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mNoisyReceiver, filter);
    }

    public void initializeScreen(){
        setSupportActionBar(mToolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mMediaPlayer = MediaPlayer.create(this, R.raw.fifteen_minute_meditation);

        finalTime = mMediaPlayer.getDuration();

        mSeekBar.setMax((int) finalTime);
        mSeekBar.setClickable(false);
    }

    View.OnClickListener gratitudeButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mAudioFocusGranted && mAudioIsPlaying) {

                mAudioIsPlaying = false;
                // 2. Give up audio focus
                abandonAudioFocus();
            }
            durationHandler.removeCallbacks(updateDuration);

            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;

            unregisterReceiver(mNoisyReceiver);
            unregisterReceiver(mIntentReceiver);

            Intent intent = new Intent(FifteenMeditationActivity.this, GratitudeActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

            finish();
        }
    };

    View.OnClickListener playMeditation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playMeditationActions();
        }
    };

    View.OnClickListener pauseMeditation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            pauseMeditationActions();
        }
    };

    View.OnClickListener resetMeditation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mMediaPlayer.seekTo(0);
            playMeditationActions();
        }
    };

    private void pauseMeditationActions() {
        if (mAudioFocusGranted && mAudioIsPlaying) {
            mMediaPlayer.pause();
            mAudioIsPlaying = false;

            durationHandler.removeCallbacks(updateDuration);

            unregisterReceiver(mNoisyReceiver);

            mPauseButton.setVisibility(ImageView.GONE);
            mPlayButton.setVisibility(ImageView.VISIBLE);
        }
    }

    private void playMeditationActions() {
        if (!mAudioIsPlaying) {
            if (!mAudioFocusGranted && requestAudioFocus()) {
                // 2. Kill off any other play back sources
                forceMusicStop();
                // 3. Register broadcast receiver for player intents
                setupBroadcastReceiver();
            }
            // 4. Play music
            mMediaPlayer.start();
            mAudioIsPlaying = true;
            timeElapsed = mMediaPlayer.getCurrentPosition();
            mSeekBar.setProgress((int) timeElapsed);
            durationHandler.postDelayed(updateDuration, 100);

            IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
            registerReceiver(mNoisyReceiver, filter);

            mPlayButton.setVisibility(ImageView.GONE);
            mPauseButton.setVisibility(ImageView.VISIBLE);
        }
    }


//  handler to change duration text
    private Runnable updateDuration = new Runnable() {
        public void run() {
            //get current position
            timeElapsed = mMediaPlayer.getCurrentPosition();
            //set seekbar progress
            mSeekBar.setProgress((int) timeElapsed);
            //set time remaining
            double timeRemaining = finalTime - timeElapsed;
            mDuration.setText(String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining),
                    TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining))));

            //repeat yourself that again in 100 miliseconds
            durationHandler.postDelayed(this, 100);
        }
    };


    private boolean requestAudioFocus() {
        if (!mAudioFocusGranted) {
            AudioManager am = (AudioManager) this
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

    private void setupBroadcastReceiver() {
        mIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String cmd = intent.getStringExtra(CMD_NAME);
                Log.i(TAG, "mIntentReceiver.onReceive " + action + " / " + cmd);


                if (PAUSE_SERVICE_CMD.equals(action)
                        || (SERVICE_CMD.equals(action) && CMD_PAUSE.equals(cmd))) {
                    playMeditationActions();
                }


                if (PLAY_SERVICE_CMD.equals(action)
                        || (SERVICE_CMD.equals(action) && CMD_PLAY.equals(cmd))) {
                    pauseMeditationActions();
                }
            }
        };

        // Do the right thing when something else tries to play
        if (!mReceiverRegistered) {
            IntentFilter commandFilter = new IntentFilter();
            commandFilter.addAction(SERVICE_CMD);
            commandFilter.addAction(PAUSE_SERVICE_CMD);
            commandFilter.addAction(PLAY_SERVICE_CMD);
            this.registerReceiver(mIntentReceiver, commandFilter);
            mReceiverRegistered = true;
        }
    }

    private void forceMusicStop() {
        AudioManager am = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        if (am.isMusicActive()) {
            Intent intentToStop = new Intent(SERVICE_CMD);
            intentToStop.putExtra(CMD_NAME, CMD_STOP);
            this.sendBroadcast(intentToStop);
        }
    }

    private void abandonAudioFocus() {
        AudioManager am = (AudioManager) this
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

    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                pauseMeditationActions();
            }
        }
    };

}
