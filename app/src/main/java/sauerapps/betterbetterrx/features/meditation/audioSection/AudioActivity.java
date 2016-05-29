package sauerapps.betterbetterrx.features.meditation.audioSection;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.OnClick;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.app.User;
import sauerapps.betterbetterrx.app.events.EventAudioSyncFinish;
import sauerapps.betterbetterrx.features.meditation.soundcloud.Track;
import sauerapps.betterbetterrx.utils.Constants;

public class AudioActivity extends AppCompatActivity {

    static AudioMediaPlayer mMusicPlayer;

    Track mTrack;

    @Bind(R.id.play)
    protected ImageButton mPlay;

    @Bind(R.id.pause)
    protected ImageButton mPause;

    @Bind(R.id.audioProgressBar)
    protected ProgressBar mProgressBar;

    @Bind(R.id.track_time)
    protected TextView mTrackTime;

    private Handler mDurationHandler = new Handler();
    private double mTimeElapsed = 0;
    private String mTrackTitle;
    private String mTrackDescription;

    private boolean mHeadsetConnected = false;

    String mUserEncodedEmail;
    String mUserName;
    HashMap<String, User> mSharedWith;
    int mTrackPositionDetails;

    EventBus mEventBus = EventBus.getDefault();

    private Runnable updateDuration = new Runnable() {
        public void run() {
            //get current position
            mTimeElapsed = mMusicPlayer.getCurrentAudioTime();

            //set time remaining
            double timeRemaining = mTimeElapsed;

            mTrackTime.setText(String.format(Locale.ENGLISH, "%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours((long) timeRemaining),
                    TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining),
                    TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining))));

            //repeat that again in 100 miliseconds
            mDurationHandler.postDelayed(this, 100);
        }
    };

    private final BroadcastReceiver headsetDisconnected = new BroadcastReceiver() {

        private static final String TAG = "headsetDisconnected";

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra("state")) {
                if (mHeadsetConnected && intent.getIntExtra("state", 0) == 0) {
                    mHeadsetConnected = false;
                    if (mMusicPlayer.mAudioIsPlaying) {
                        setPauseButton();
                        Log.d(TAG, "Headset was unplugged during playback.");

                    }
                } else if (!mHeadsetConnected && intent.getIntExtra("state", 0) == 1) {
                    mHeadsetConnected = true;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        Intent intent = this.getIntent();
        mUserName = intent.getStringExtra(Constants.KEY_USER_NAME);
        mUserEncodedEmail = intent.getStringExtra(Constants.KEY_ENCODED_EMAIL);
        mSharedWith = (HashMap<String, User>) intent.getSerializableExtra(Constants.KEY_SHARED_WITH_USERS);
//        mTrackPositionDetails = intent.getIntExtra(Constants.KEY_TRACK_POSITION, 1);

        initializeScreen();
    }

    private void initializeScreen() {
        mMusicPlayer.setAudioIsPlaying();
        setPlayButton();

        mEventBus.register(this);

        ImageView track_image = (ImageView) findViewById(R.id.track_image);
        TextView title_text = (TextView) findViewById(R.id.track_title);
        TextView title_description = (TextView) findViewById(R.id.track_description);

        mTrackDescription = mTrack.getDescription();
        mTrackTitle = mTrack.getTitle();

        title_description.setText(mTrackDescription);
        title_text.setText(mTrackTitle);

        Picasso.with(this)
                .load(mTrack.getArtworkURL())
                .error(R.drawable.ic_default_art)
                .placeholder(R.drawable.ic_default_art)
                .into(track_image);
    }

    @Override
    public void onResume() {
        registerReceiver(headsetDisconnected, new IntentFilter(
                Intent.ACTION_HEADSET_PLUG));

        super.onResume();

//        setFocusableInTouchMode(true);
//        requestFocus();
//        setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//
//                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
//
//                    exitAudioDetails();
//
//                    return true;
//                }
//                return false;
//            }
//        });

        if (mMusicPlayer.mAudioIsPlaying) {
            mDurationHandler.postDelayed(updateDuration, 100);
        }
    }


    @Override
    public void onPause() {
        super.onPause();

        mDurationHandler.removeCallbacks(updateDuration);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }
    }

    @OnClick(R.id.play)
    public void setPlay() {

        setPlayButton();

        mMusicPlayer.play();
    }

    private void setPlayButton() {

        if (mMusicPlayer.mIsLoadingAudioStream) {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        mDurationHandler.postDelayed(updateDuration, 100);

        mPlay.setVisibility(View.INVISIBLE);
        mPause.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.pause)
    public void setPauseButton() {
        mMusicPlayer.pause();

        mDurationHandler.removeCallbacks(updateDuration);

        mPause.setVisibility(View.INVISIBLE);
        mPlay.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.reset_recording)
    public void setResetButton() {
        mMusicPlayer.setResetRecordingButton();
    }

    @OnClick(R.id.fast_forward)
    public void setFastForwardButton() {
        mMusicPlayer.setFastForwardButton();
    }

    @OnClick(R.id.track_exit_button)
    public void setExitButton() {
        exitAudioDetails();
    }

    @Subscribe
    public void onEvent(EventAudioSyncFinish eventAudioSyncFinish) {
        mProgressBar.setVisibility(View.GONE);
    }

    private void exitAudioDetails() {

        mMusicPlayer.exitAudio();

        mDurationHandler.removeCallbacks(updateDuration);

        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();

        for (int i = 0; i < backStackCount; i++) {

            int backStackId = getSupportFragmentManager().getBackStackEntryAt(i).getId();

            getSupportFragmentManager().popBackStack(backStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        }

        unregisterReceiver(headsetDisconnected);

        if (mTimeElapsed >= 10000) {

            DialogFragment dialog = SaveAudioTimeDialogFragment.newInstance(mUserEncodedEmail, mUserName, mTimeElapsed,
                    mTrackDescription, mTrackTitle, mSharedWith);
            dialog.show(getFragmentManager(), "SaveAudioTimeDialogFragment");

        }
    }
}
