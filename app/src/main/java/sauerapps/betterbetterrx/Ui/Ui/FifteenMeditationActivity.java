package sauerapps.betterbetterrx.Ui.Ui;

import android.content.Context;
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

    @Bind(R.id.toolbar) protected Toolbar mToolbar;
    @Bind(R.id.seekBar) protected SeekBar mSeekBar;
    @Bind(R.id.songDuration) protected TextView mDuration;
    @Bind(R.id.media_play) protected ImageButton mPlayButton;
    @Bind(R.id.media_pause) protected ImageButton mPauseButton;
    @Bind(R.id.reset_recording) protected ImageButton mResetRecording;

    private Handler durationHandler = new Handler();
    private double timeElapsed = 0, finalTime = 0;
    private MediaPlayer mMediaPlayer;




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


    View.OnClickListener playMeditation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            mPlayButton.setVisibility(ImageView.GONE);
            mPauseButton.setVisibility(ImageView.VISIBLE);
            playMeditationActions();
        }
    };

    View.OnClickListener pauseMeditation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mMediaPlayer.pause();
            durationHandler.removeCallbacks(updateDuration);

            mPauseButton.setVisibility(ImageView.GONE);
            mPlayButton.setVisibility(ImageView.VISIBLE);
        }
    };

    View.OnClickListener resetMeditation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mMediaPlayer.seekTo(0);
            playMeditationActions();
        }
    };


    private void playMeditationActions() {
        mMediaPlayer.start();

        timeElapsed = mMediaPlayer.getCurrentPosition();
        mSeekBar.setProgress((int) timeElapsed);
        durationHandler.postDelayed(updateDuration, 100);

        mPlayButton.setVisibility(ImageView.GONE);
        mPauseButton.setVisibility(ImageView.VISIBLE);
    }


//    handler to change duration text
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
}
