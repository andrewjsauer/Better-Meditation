package sauerapps.betterbetterrx.features.meditation.audioSection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntRange;
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
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.features.soundcloud.Config;
import sauerapps.betterbetterrx.features.soundcloud.Track;

public class AudioDetailsFragment extends Fragment {

    private static final String ARG_TRACK_NUMBER = "argTrackNumber";
    private static final String TAG = AudioDetailsFragment.class.getSimpleName();

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

    private Handler durationHandler = new Handler();


    protected Track mTrack;
    protected int mTrackPosition;
    protected MediaPlayer mMediaPlayer;
    private double timeElapsed = 0;
    private int seekForwardTime = 5000; // 5000 milliseconds

    private boolean mAudioIsPlaying = false;
    private boolean mAudioFocusGranted = false;
    private BroadcastReceiver mIntentReceiver;
    private boolean mReceiverRegistered = false;
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;

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



    public static AudioDetailsFragment newInstance(@IntRange(from = 1, to = 10) int trackNumber) {
        Bundle args = new Bundle();
        args.putInt(ARG_TRACK_NUMBER, trackNumber);

        AudioDetailsFragment fragment = new AudioDetailsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.audio_details_fragment, container, false);
        ButterKnife.bind(this, view);

        mTrack = AudioListFragment.mTrack;

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

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
                if (mMediaPlayer != null) {
                    getActivity().getSupportFragmentManager().popBackStack();

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

        Bundle args = getArguments();
        int trackNumber = args.containsKey(ARG_TRACK_NUMBER) ? args.getInt(ARG_TRACK_NUMBER) : 1;

        title_description.setText(mTrack.getDescription());
        title_text.setText(mTrack.getTitle());

        Picasso.with(getActivity())
                .load(mTrack.getArtworkURL())
                .error(R.drawable.ic_default_art)
                .placeholder(R.drawable.ic_default_art)
                .into(track_image);
        // TODO make exit fade work using trackNumber position, tried if() and switch statements, no prevail

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
                    return true;
                }
                return false;
            }
        });


        getActivity().registerReceiver(audioBecomingNoisy, new IntentFilter(
                AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        getActivity().registerReceiver(headsetDisconnected, new IntentFilter(
                Intent.ACTION_HEADSET_PLUG));

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

    //  handler to change duration text
    private Runnable updateDuration = new Runnable() {
        public void run() {
            //get current position
            timeElapsed = mMediaPlayer.getCurrentPosition();

            //set time remaining
            double timeRemaining = timeElapsed;
            mTrackTime.setText(String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining),
                    TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining))));

            //repeat yourself that again in 100 miliseconds
            durationHandler.postDelayed(this, 100);
        }
    };

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
}
