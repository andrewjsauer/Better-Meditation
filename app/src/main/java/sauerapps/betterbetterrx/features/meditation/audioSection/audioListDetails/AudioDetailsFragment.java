package sauerapps.betterbetterrx.features.meditation.audioSection.audioListDetails;

import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.features.meditation.audioSection.audioListDetails.soundcloud.Config;
import sauerapps.betterbetterrx.features.meditation.audioSection.audioListDetails.soundcloud.Track;
import sauerapps.betterbetterrx.model.User;
import sauerapps.betterbetterrx.utils.Constants;

public class AudioDetailsFragment extends Fragment {

    private static final String TAG = AudioDetailsFragment.class.getSimpleName();

    private static final String CMD_NAME = "command";
    private static final String CMD_PAUSE = "pause";
    private static final String CMD_STOP = "pause";
    private static final String CMD_PLAY = "play";


    // Jellybean
    private static String SERVICE_CMD = "sauerapps.betterbetterrx";
    private static String PAUSE_SERVICE_CMD = "sauerapps.betterbetterrx.pause";
    private static String PLAY_SERVICE_CMD = "sauerapps.betterbetterrx.play";
    protected Track mTrack;
    ;
    protected int mTrackPosition;
    protected MediaPlayer mMediaPlayer;
    @Bind(R.id.play)
    protected ImageButton mPlay;
    @Bind(R.id.pause)
    protected ImageButton mPause;
    @Bind(R.id.reset_recording)
    protected ImageButton mResetButton;
    @Bind(R.id.fast_forward)
    protected ImageButton mFastForwardButton;
    @Bind(R.id.audioProgressBar)
    protected ProgressBar mProgressBar;
    @Bind(R.id.track_time)
    protected TextView mTrackTime;
    @Bind(R.id.track_exit_button)
    protected ImageButton mExitButton;
    private Handler durationHandler = new Handler();
    private double timeElapsed = 0;
    private int seekForwardTime = 5000; // 5000 milliseconds
    private String mTrackTitle;
    private String mTrackDescription;
    private boolean mAudioIsPlaying = false;
    private boolean mAudioFocusGranted = false;
    private BroadcastReceiver mIntentReceiver;
    private boolean mReceiverRegistered = false;
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;
    private String mEncodedEmail;
    private String mUserName;
    private HashMap<String, User> mSharedWith;
    //  handler to change duration text
    private Runnable updateDuration = new Runnable() {
        public void run() {
            //get current position
            timeElapsed = mMediaPlayer.getCurrentPosition();

            //set time remaining
            double timeRemaining = timeElapsed;
            mTrackTime.setText(String.format(Locale.ENGLISH, "%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours((long) timeRemaining),
                    TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining),
                    TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining))));

            //repeat that again in 100 miliseconds
            durationHandler.postDelayed(this, 100);
        }
    };
    private final BroadcastReceiver audioBecomingNoisy = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                pause();
            }
        }
    };
    private final BroadcastReceiver headsetDisconnected = new BroadcastReceiver() {
        private static final String TAG = "headsetDisconnected";
        private static final int UNPLUGGED = 0;
        private static final int PLUGGED = 1;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                if (state != -1) {
                    Log.d(TAG, "Headset plug event. State is " + state);
                    if (state == UNPLUGGED) {
                        Log.d(TAG, "Headset was unplugged during playback.");
                        pause();
                    } else if (state == PLUGGED) {
                        Log.d(TAG, "Headset was plugged in during playback.");
                    }
                } else {
                    Log.e(TAG, "Received invalid ACTION_HEADSET_PLUG intent");
                }
            }
        }
    };

    // Honeycomb
    {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            SERVICE_CMD = "sauerapps.betterbetterrx";
            PAUSE_SERVICE_CMD = "sauerapps.betterbetterrx.pause";
            PLAY_SERVICE_CMD = "sauerapps.betterbetterrx.play";
        }
    }

    public static AudioDetailsFragment newInstance(String encodedEmail, String userName,
                                                   HashMap<String, User> sharedWith) {
        Bundle args = new Bundle();
        args.putString(Constants.KEY_ENCODED_EMAIL, encodedEmail);
        args.putString(Constants.KEY_NAME, userName);
        args.putSerializable(Constants.KEY_SHARED_WITH_USERS, sharedWith);
        AudioDetailsFragment fragment = new AudioDetailsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio_details, container, false);
        ButterKnife.bind(this, view);

        mEncodedEmail = getArguments().getString(Constants.KEY_ENCODED_EMAIL);
        mUserName = getArguments().getString(Constants.KEY_NAME);
        mSharedWith = (HashMap) getArguments().getSerializable(Constants.KEY_SHARED_WITH_USERS);

        mTrack = AudioListFragment.mTrack;

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        if (!mAudioFocusGranted && requestAudioFocus()) {
            // 2. Kill off any other play back sources
            forceMusicStop();
            // 3. Register broadcast receiver for player intents
            setupBroadcastReceiver();
        }

        try {
            mProgressBar.setVisibility(View.VISIBLE);
            mMediaPlayer.setDataSource(mTrack.getStreamURL() + "?client_id=" + Config.CLIENT_ID);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }


        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                play();
                mProgressBar.setVisibility(View.GONE);
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                pause();
            }
        });

        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });
        mPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause();
            }
        });
        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResetRecordingButton();
            }
        });
        mFastForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFastForwardButton();
            }
        });
        mExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitAudioDetails();

            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTrackPosition = AudioListFragment.mTrackPosition;

        ImageView track_image = (ImageView) view.findViewById(R.id.track_image);
        TextView title_text = (TextView) view.findViewById(R.id.track_title);
        TextView title_description = (TextView) view.findViewById(R.id.track_artist);

        mTrackDescription = mTrack.getDescription();
        mTrackTitle = mTrack.getTitle();

        title_description.setText(mTrackDescription);
        title_text.setText(mTrackTitle);

        Picasso.with(getActivity())
                .load(mTrack.getArtworkURL())
                .error(R.drawable.ic_default_art)
                .placeholder(R.drawable.ic_default_art)
                .into(track_image);

        // TODO make exit fade work using trackNumber position, tried if() and switch statements, no prevail

        getActivity().registerReceiver(headsetDisconnected, new IntentFilter(
                Intent.ACTION_HEADSET_PLUG));
    }

    @Override
    public void onResume() {
        super.onResume();

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {

                    exitAudioDetails();

                    return true;
                }
                return false;
            }
        });


        getActivity().registerReceiver(audioBecomingNoisy, new IntentFilter(
                AudioManager.ACTION_AUDIO_BECOMING_NOISY));

        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        Log.i(TAG, "AUDIOFOCUS_GAIN");
//                        play();
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT");
//                        play();
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                        Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
//                        play();
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
                        pause();
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

        durationHandler.postDelayed(updateDuration, 100);

    }

    @Override
    public void onPause() {
        super.onPause();
        durationHandler.removeCallbacks(updateDuration);
    }

    private void exitAudioDetails() {
        if (timeElapsed >= 10000) {

            DialogFragment dialog = SaveAudioTimeDialogFragment.newInstance(mEncodedEmail, mUserName, timeElapsed,
                    mTrackDescription, mTrackTitle, mSharedWith);
            dialog.show(getActivity().getFragmentManager(), "SaveAudioTimeDialogFragment");

        }

        getActivity().getSupportFragmentManager().popBackStack();

        if (mMediaPlayer != null) {

            mMediaPlayer.stop();

            if (mAudioFocusGranted && mAudioIsPlaying) {
                mAudioIsPlaying = false;
                // 2. Give up audio focus
                abandonAudioFocus();
            }

            durationHandler.removeCallbacks(updateDuration);
            mMediaPlayer.release();
            mMediaPlayer = null;

            getActivity().unregisterReceiver(headsetDisconnected);
            getActivity().unregisterReceiver(audioBecomingNoisy);
            getActivity().unregisterReceiver(mIntentReceiver);

        }
    }

    private void play() {
        if (!mAudioIsPlaying) {
            if (!mAudioFocusGranted && requestAudioFocus()) {
                // 2. Kill off any other play back sources
                forceMusicStop();
                // 3. Register broadcast receiver for player intents
                setupBroadcastReceiver();
            }
            // 4. Play music
            timeElapsed = mMediaPlayer.getCurrentPosition();
            durationHandler.postDelayed(updateDuration, 100);

            mMediaPlayer.start();



            mPlay.setVisibility(View.INVISIBLE);
            mPause.setVisibility(View.VISIBLE);

            mAudioIsPlaying = true;
        }
    }

    private void pause() {
        if (mAudioFocusGranted && mAudioIsPlaying) {
            mAudioIsPlaying = false;
            durationHandler.removeCallbacks(updateDuration);
            mMediaPlayer.pause();

            mPause.setVisibility(View.INVISIBLE);
            mPlay.setVisibility(View.VISIBLE);
        }
    }

    private void setResetRecordingButton() {
        mMediaPlayer.seekTo(0);
        play();
    }

    private void setFastForwardButton() {
        int currentPosition = mMediaPlayer.getCurrentPosition();

        if(currentPosition + seekForwardTime <= mMediaPlayer.getDuration()){
            mMediaPlayer.seekTo(currentPosition + seekForwardTime);
        }
        else {
            mMediaPlayer.seekTo(mMediaPlayer.getDuration());
        }
    }

    private void forceMusicStop() {
        AudioManager am = (AudioManager) getActivity()
                .getSystemService(Context.AUDIO_SERVICE);
        if (am.isMusicActive()) {
            Intent intentToStop = new Intent(SERVICE_CMD);
            intentToStop.putExtra(CMD_NAME, CMD_STOP);
            getActivity().sendBroadcast(intentToStop);
        }
    }

    private boolean requestAudioFocus() {
        if (!mAudioFocusGranted) {
            AudioManager am = (AudioManager) getActivity()
                            .getSystemService(Context.AUDIO_SERVICE);
            // Request audio focus for play back
            int result = am.requestAudioFocus(mOnAudioFocusChangeListener,
                    // Use the music stream.
                    AudioManager.STREAM_MUSIC,
                    // Request permanent focus.
                    AudioManager.AUDIOFOCUS_GAIN);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.d(TAG, "Audiofocus granted");
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
        AudioManager am = (AudioManager) getActivity()
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
            getActivity().registerReceiver(mIntentReceiver, commandFilter);
            mReceiverRegistered = true;
        }
    }
}
