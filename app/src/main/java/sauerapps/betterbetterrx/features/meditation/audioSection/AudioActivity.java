package sauerapps.betterbetterrx.features.meditation.audioSection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.app.User;
import sauerapps.betterbetterrx.app.events.EventAudioPaused;
import sauerapps.betterbetterrx.app.events.EventAudioSyncFinish;
import sauerapps.betterbetterrx.features.meditation.playlistDetails.PlaylistTracksActivity;
import sauerapps.betterbetterrx.features.meditation.soundcloud.Track;
import sauerapps.betterbetterrx.utils.AudioListUtil;
import sauerapps.betterbetterrx.utils.Constants;

public class AudioActivity extends AppCompatActivity {

    private static final String TAG = AudioActivity.class.getSimpleName();

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

    protected String mTime;

    ValueEventListener mSharedWithListener;
    HashMap<String, User> mSharedWith;

    Firebase userAudioRef, userAudioDetailsRef, userAudioDetailsListRef, mSharedWithRef;

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

        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        ButterKnife.bind(this);

        mTrack = PlaylistTracksActivity.mTrack;

        if (mMusicPlayer == null) {
            mMusicPlayer = AudioMediaPlayer.getInstance(this);
        }

        Intent intent = this.getIntent();
        mUserName = intent.getStringExtra(Constants.KEY_USER_NAME);
        mUserEncodedEmail = intent.getStringExtra(Constants.KEY_ENCODED_EMAIL);

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


        mSharedWithRef = new Firebase(Constants.FIREBASE_URL_AUDIO_DETAILS_SHARED_WITH).child(mUserEncodedEmail);
        userAudioRef = new Firebase(Constants.FIREBASE_URL_USER_AUDIO).child(mUserEncodedEmail);
        userAudioDetailsRef = new Firebase(Constants.FIREBASE_URL_USER_AUDIO_DETAILS).child(mUserEncodedEmail);
        userAudioDetailsListRef = new Firebase(Constants.FIREBASE_URL_USER_AUDIO_DETAILS_LIST)
                .child(mUserEncodedEmail).child(mUserEncodedEmail);

        mSharedWithListener = mSharedWithRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mSharedWith = new HashMap<>();

                for (DataSnapshot currentUser : dataSnapshot.getChildren()) {
                    mSharedWith.put(currentUser.getKey(), currentUser.getValue(User.class));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG,
                        getString(R.string.log_error_the_read_failed) +
                                firebaseError.getMessage());
            }
        });
    }

    @Override
    public void onResume() {
        registerReceiver(headsetDisconnected, new IntentFilter(
                Intent.ACTION_HEADSET_PLUG));
        super.onResume();

        if (mMusicPlayer.mAudioIsPlaying) {
            mDurationHandler.postDelayed(updateDuration, 100);
        }
    }

    @Override
    protected void onPause() {
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        exitAudioDetails();
    }

    @OnClick(R.id.play)
    public void setPlay() {

        setPlayButton();

        mMusicPlayer.play();
    }

    private void setPlayButton() {

        if (mMusicPlayer.mIsLoadingAudioStream) {
            mProgressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Subscribe
    public void onEvent(EventAudioPaused eventAudioPaused) {
        setPauseButton();
    }

    private void exitAudioDetails() {

        mMusicPlayer.exitAudio();

        mDurationHandler.removeCallbacks(updateDuration);

        unregisterReceiver(headsetDisconnected);

        if (mTimeElapsed >= 10000) {

            double time = mTimeElapsed;

            mTime = String.format(Locale.ENGLISH, "%01d hr %02d min",
                    TimeUnit.MILLISECONDS.toHours((long) time),
                    TimeUnit.MILLISECONDS.toMinutes((long) time)
                            - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours((long) time)));

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.audio_save_time_title))
                    .setMessage("Save: " + mTime + "?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            getSaveSession();
                            dialog.dismiss();
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                            overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
                        }
                    })
                    .setIcon(android.R.drawable.ic_menu_save)
                    .show();
        } else {
            finish();
            enterExitAnimation();
        }
    }

    private void enterExitAnimation() {
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }

    private void getSaveSession() {

        userAudioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    double lastAudioTime = (double) dataSnapshot.getValue();

                    double newTime = lastAudioTime + mTimeElapsed;
                    userAudioRef.setValue(newTime);

                } else {
                    userAudioRef.setValue(mTimeElapsed);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG,
                        getString(R.string.log_error_the_read_failed) +
                                firebaseError.getMessage());
            }
        });

        final String ownerEmail = mUserEncodedEmail;

        final Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);

        HashMap<String, Object> updateAudioListData = new HashMap<>();

        HashMap<String, Object> timestampCreated = new HashMap<>();
        timestampCreated.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

        AudioList audioList = new AudioList(mUserEncodedEmail, mUserName, mTimeElapsed, mTrackTitle,
                mTrackDescription, timestampCreated);

        HashMap<String, Object> audioListMap = (HashMap<String, Object>)
                new ObjectMapper().convertValue(audioList, Map.class);

        AudioListUtil.updateMapForAllWithValue(mSharedWith, ownerEmail, mUserEncodedEmail,
                updateAudioListData, "", audioListMap);

        updateAudioListData.put("/" + Constants.FIREBASE_LOCATION_OWNER_MAPPINGS + "/" + ownerEmail,
                mUserEncodedEmail);

        firebaseRef.updateChildren(updateAudioListData, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                AudioListUtil.updateTimestampReversed(firebaseError, "AddList", ownerEmail,
                        null, mUserEncodedEmail);
            }
        });

        userAudioDetailsRef.push().setValue(audioList);

        Toast.makeText(AudioActivity.this, "Session saved.", Toast.LENGTH_SHORT).show();
    }
}
