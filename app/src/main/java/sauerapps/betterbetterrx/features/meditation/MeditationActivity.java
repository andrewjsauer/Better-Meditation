package sauerapps.betterbetterrx.features.meditation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.app.BaseActivity;
import sauerapps.betterbetterrx.features.SoundCloud.Config;
import sauerapps.betterbetterrx.features.SoundCloud.SCService;
import sauerapps.betterbetterrx.features.SoundCloud.SoundCloud;
import sauerapps.betterbetterrx.features.SoundCloud.Track;


public class MeditationActivity extends BaseActivity {

    private static String TAG = MeditationActivity.class.getSimpleName();

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

    @Bind(R.id.toolbar)
    protected Toolbar mToolbar;
    @Bind(R.id.playback_controls)
    protected RelativeLayout mPlaybackControls;


    // media
    protected MediaPlayer mMediaPlayer;
    @Bind(R.id.play_pause)
    protected ImageView mPlayerControl;
    @Bind(R.id.title)
    protected TextView mSelectedTrackTitle;
    @Bind(R.id.album_art)
    protected ImageView mSelectedTrackImage;
    @Bind(R.id.artist)
    protected TextView mSelectedTrackArtist;

    private boolean mAudioIsPlaying = false;
    private boolean mAudioFocusGranted = false;
    private BroadcastReceiver mIntentReceiver;
    private boolean mReceiverRegistered = false;
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;


    // view
    @Bind(R.id.track_list_view)
    protected RecyclerView mRecyclerView;
    protected TrackAdapter mAdapter;
    protected List<Track> mListItems;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meditation);
        ButterKnife.bind(this);

        initializeScreen();
    }

    private void initializeScreen() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initializeData();
        initializeAdapter();

        SCService scService = SoundCloud.getService();
        Call<List<Track>> call = scService.getRecentTracks();
        call.enqueue(new Callback<List<Track>>() {
            @Override
            public void onResponse(Call<List<Track>> call, Response<List<Track>> response) {
                if (response.isSuccess()) {
                    loadTracks(response.body());
                    Log.d(TAG, "Something worked");
                } else {
                    int statusCode = response.code();
                    Log.d(TAG, statusCode + "");
                }
            }

            @Override
            public void onFailure(Call<List<Track>> call, Throwable t) {
                Log.d(TAG, "Error: " + t);
            }
        });

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


        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                togglePlayPause();
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mPlayerControl.setImageResource(R.drawable.uamp_ic_play_arrow_white_24dp);
            }
        });

        mPlayerControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayPause();
            }
        });

        mAdapter.setOnItemClickListener(new TrackAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {

//                mPlaybackControls.setVisibility(View.VISIBLE);

                Track track = mListItems.get(position);

                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                }

                mSelectedTrackArtist.setText(track.getDescription());
                mSelectedTrackTitle.setText(track.getTitle());
                Picasso.with(MeditationActivity.this)
                        .load(track.getArtworkURL())
                        .error(R.drawable.ic_default_art)
                        .placeholder(R.drawable.ic_default_art)
                        .into(mSelectedTrackImage);

                try {
                    if (!mAudioIsPlaying) {
                        if (!mAudioFocusGranted && requestAudioFocus()) {
                            // 2. Kill off any other play back sources
                            forceMusicStop();
                            // 3. Register broadcast receiver for player intents
                            setupBroadcastReceiver();
                        }
                        // 4. Play music
                        mMediaPlayer.setDataSource(track.getStreamURL() + "?client_id=" + Config.CLIENT_ID);
                        mMediaPlayer.prepareAsync();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(audioBecomingNoisy, new IntentFilter(
                AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        registerReceiver(headsetDisconnected, new IntentFilter(
                Intent.ACTION_HEADSET_PLUG));
        registerReceiver(mediaButtons, new IntentFilter(
                Intent.ACTION_MEDIA_BUTTON));

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }

            if (mAudioFocusGranted && mAudioIsPlaying) {
                mAudioIsPlaying = false;
                // 2. Give up audio focus
                abandonAudioFocus();
            }

            mMediaPlayer.release();
            mMediaPlayer = null;

            unregisterReceiver(mediaButtons);
            unregisterReceiver(headsetDisconnected);
            unregisterReceiver(audioBecomingNoisy);
            unregisterReceiver(mIntentReceiver);
        }
    }


    private void loadTracks(List<Track> tracks) {
        mListItems.clear();
        mListItems.addAll(tracks);
        mAdapter.notifyDataSetChanged();
    }

    private void initializeData() {
        mListItems = new ArrayList<Track>();
    }


    private void initializeAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new TrackAdapter(mListItems, this);

        mRecyclerView.setAdapter(mAdapter);

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

            mMediaPlayer.start();
            mPlayerControl.setImageResource(R.drawable.pause);
            mAudioIsPlaying = true;
        }
    }

    private void pause() {
        if (mAudioFocusGranted && mAudioIsPlaying) {
            mAudioIsPlaying = false;
            mMediaPlayer.pause();
            mPlayerControl.setImageResource(R.drawable.play);
        }
    }

    private void togglePlayPause() {
        if (mMediaPlayer.isPlaying()) {
            pause();
        } else {
            play();
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


    private boolean requestAudioFocus() {
        if (!mAudioFocusGranted) {
            AudioManager am = (AudioManager)
                    this.getSystemService(Context.AUDIO_SERVICE);
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
            registerReceiver(mIntentReceiver, commandFilter);
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


    private final BroadcastReceiver mediaButtons = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
                KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                    pause();
                }
//                if (!mAudioIsPlaying && KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
//                    pauseMeditationActions();
//                }
            }
        }
    };


}
