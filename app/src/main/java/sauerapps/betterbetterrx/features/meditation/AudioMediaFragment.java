package sauerapps.betterbetterrx.features.meditation;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.app.User;
import sauerapps.betterbetterrx.app.events.EventAudioSyncFinish;
import sauerapps.betterbetterrx.features.meditation.soundcloud.Track;
import sauerapps.betterbetterrx.utils.Constants;

public class AudioMediaFragment extends Fragment {

    private static AudioMediaPlayer mMusicPlayer;

    Track mTrack;

    @Bind(R.id.play)
    protected ImageButton mPlay;
    @Bind(R.id.pause)
    protected ImageButton mPause;
    @Bind(R.id.audioProgressBar)
    protected ProgressBar mProgressBar;
    @Bind(R.id.track_time)
    protected TextView mTrackTime;

    private Handler durationHandler = new Handler();
    private double timeElapsed = 0;
    private String mTrackTitle;
    private String mTrackDescription;

    private String mEncodedEmail;
    private String mUserName;
    private HashMap<String, User> mSharedWith;

    EventBus mEventBus = EventBus.getDefault();

    private Runnable updateDuration = new Runnable() {
        public void run() {
            //get current position
            timeElapsed = mMusicPlayer.getCurrentAudioTime();

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
                        mMusicPlayer.pause();
                    } else if (state == PLUGGED) {
                        Log.d(TAG, "Headset was plugged in during playback.");
                    }
                } else {
                    Log.e(TAG, "Received invalid ACTION_HEADSET_PLUG intent");
                }
            }
        }
    };


    public static AudioMediaFragment newInstance(String encodedEmail, String userName,
                                                 HashMap<String, User> sharedWith) {
        Bundle args = new Bundle();
        args.putString(Constants.KEY_ENCODED_EMAIL, encodedEmail);
        args.putString(Constants.KEY_NAME, userName);
        args.putSerializable(Constants.KEY_SHARED_WITH_USERS, sharedWith);

        AudioMediaFragment fragment = new AudioMediaFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTrack = AudioListFragment.mTrack;

        if (mMusicPlayer == null) {
            mMusicPlayer = AudioMediaPlayer.getInstance(getActivity());
        }

        mEncodedEmail = getArguments().getString(Constants.KEY_ENCODED_EMAIL);
        mUserName = getArguments().getString(Constants.KEY_NAME);
        mSharedWith = (HashMap<String, User>) getArguments().getSerializable(Constants.KEY_SHARED_WITH_USERS);

        getArguments().remove(Constants.KEY_SHARED_WITH_USERS);
        getArguments().remove(Constants.KEY_NAME);
        getArguments().remove(Constants.KEY_ENCODED_EMAIL);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_audio_details, container, false);

        ButterKnife.bind(this, view);

        mMusicPlayer.setAudioIsPlaying();
        setPlayButton();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView track_image = (ImageView) view.findViewById(R.id.track_image);
        TextView title_text = (TextView) view.findViewById(R.id.track_title);
        TextView title_description = (TextView) view.findViewById(R.id.track_description);

        mTrackDescription = mTrack.getDescription();
        mTrackTitle = mTrack.getTitle();

        title_description.setText(mTrackDescription);
        title_text.setText(mTrackTitle);

        Picasso.with(getActivity())
                .load(mTrack.getArtworkURL())
                .error(R.drawable.ic_default_art)
                .placeholder(R.drawable.ic_default_art)
                .into(track_image);

        getActivity().registerReceiver(headsetDisconnected, new IntentFilter(
                Intent.ACTION_HEADSET_PLUG));

    }

    @Override
    public void onResume() {
        super.onResume();

        mEventBus.register(this);

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

        if (mMusicPlayer.mAudioIsPlaying) {
            durationHandler.postDelayed(updateDuration, 100);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        durationHandler.removeCallbacks(updateDuration);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }
    }

    @OnClick (R.id.play)
    public void setPlay() {

        setPlayButton();

        mMusicPlayer.play();

    }

    private void setPlayButton() {

        if (mMusicPlayer.mIsLoadingAudioStream) {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        durationHandler.postDelayed(updateDuration, 100);

        mPlay.setVisibility(View.INVISIBLE);
        mPause.setVisibility(View.VISIBLE);
    }

    @OnClick (R.id.pause)
    public void setPauseButton() {
        mMusicPlayer.pause();

        durationHandler.removeCallbacks(updateDuration);

        mPause.setVisibility(View.INVISIBLE);
        mPlay.setVisibility(View.VISIBLE);
    }

    @OnClick (R.id.reset_recording)
    public void setResetButton() {
        mMusicPlayer.setResetRecordingButton();
    }

    @OnClick (R.id.fast_forward)
    public void setFastForwardButton() {
        mMusicPlayer.setFastForwardButton();
    }

    @OnClick (R.id.track_exit_button)
    public void setExitButton() {
        exitAudioDetails();
    }

    @Subscribe
    public void onEvent(EventAudioSyncFinish eventAudioSyncFinish) {
        mProgressBar.setVisibility(View.GONE);
    }

    private void exitAudioDetails() {

        mMusicPlayer.exitAudio();

        durationHandler.removeCallbacks(updateDuration);

        int backStackCount = getActivity().getSupportFragmentManager().getBackStackEntryCount();

        for (int i = 0; i < backStackCount; i++) {

            int backStackId = getActivity().getSupportFragmentManager().getBackStackEntryAt(i).getId();

            getActivity().getSupportFragmentManager().popBackStack(backStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        }

        getActivity().unregisterReceiver(headsetDisconnected);


        if (timeElapsed >= 10000) {

            DialogFragment dialog = SaveAudioTimeDialogFragment.newInstance(mEncodedEmail, mUserName, timeElapsed,
                    mTrackDescription, mTrackTitle, mSharedWith);
            dialog.show(getActivity().getFragmentManager(), "SaveAudioTimeDialogFragment");

        }
    }
}
