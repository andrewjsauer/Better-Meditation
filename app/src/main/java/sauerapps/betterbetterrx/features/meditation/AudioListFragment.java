package sauerapps.betterbetterrx.features.meditation;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.app.BaseActivity;
import sauerapps.betterbetterrx.features.meditation.soundcloud.SCService;
import sauerapps.betterbetterrx.features.meditation.soundcloud.SoundCloud;
import sauerapps.betterbetterrx.features.meditation.soundcloud.Track;
import sauerapps.betterbetterrx.features.meditation.soundcloud.Tracks;
import sauerapps.betterbetterrx.app.User;
import sauerapps.betterbetterrx.utils.Constants;

public class AudioListFragment extends Fragment implements AudioClickListener {

    private static final String TAG = AudioListFragment.class.getSimpleName();

    public static Track mTrack;

    protected TrackAdapter mAdapter;
    protected List<Track> mListItems;

    @Bind(R.id.progressBar_audio_list)
    protected ProgressBar mProgressBar;
    @Bind(R.id.recyclerviewRx)
    protected RecyclerView mRecyclerView;
    @Bind(R.id.toolbar)
    protected Toolbar mToolbar;

    ValueEventListener mSharedWithListener;
    HashMap<String, User> mSharedWith;
    Firebase mSharedWithRef;

    private String mEncodedEmail;
    private String mUserName;
    private int mPlaylistPosition;


    public AudioListFragment() {
        /* Required empty public constructor */
    }

    public static AudioListFragment newInstance(String encodedEmail, String userName, int playlistPosition) {
        AudioListFragment fragment = new AudioListFragment();
        Bundle args = new Bundle();
        args.putString(Constants.KEY_ENCODED_EMAIL, encodedEmail);
        args.putString(Constants.KEY_NAME, userName);
        args.putInt(Constants.KEY_PLAYLIST_POSITION, playlistPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEncodedEmail = getArguments().getString(Constants.KEY_ENCODED_EMAIL);
            mUserName = getArguments().getString(Constants.KEY_NAME);
            mPlaylistPosition = getArguments().getInt(Constants.KEY_PLAYLIST_POSITION);
        }

        mSharedWithRef = new Firebase(Constants.FIREBASE_URL_AUDIO_DETAILS_SHARED_WITH).child(mEncodedEmail);
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


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_audio_list, container, false);
        ButterKnife.bind(this, v);

        mProgressBar.setVisibility(View.VISIBLE);

        BaseActivity activity = (BaseActivity) getActivity();

        activity.setSupportActionBar(mToolbar);

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            activity.setTitle("Meditations");
        }

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListItems = new ArrayList<>();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new TrackAdapter(mListItems, getContext(), this);

        mRecyclerView.setAdapter(mAdapter);

        SCService scService = SoundCloud.getService();
        Call<Tracks> call = scService.getRecentTracks(mPlaylistPosition);
        call.enqueue(new Callback<Tracks>() {
            @Override
            public void onResponse(Call<Tracks> call, Response<Tracks> response) {
                if (response.isSuccess()) {
                    loadTracks(response.body().mTracks);
                    Log.d(TAG, "Something: " + response.body().mTracks);
                } else {
                    int statusCode = response.code();
                    Toast.makeText(getActivity(), "Error: " + statusCode, Toast.LENGTH_LONG).show();
                    Log.d(TAG, statusCode + "");
                }
            }

            @Override
            public void onFailure(Call<Tracks> call, Throwable t) {
                Log.d(TAG, "Error: " + t);
                Toast.makeText(getActivity(), "Error: Check internet connectivity. " + t, Toast.LENGTH_LONG).show();
            }
        });

    }

    private void loadTracks(List<Track> tracks) {
        mListItems.clear();
        mListItems.addAll(tracks);
        mAdapter.notifyDataSetChanged();
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onTrackClicked(TrackHolder holder, int position) {
        mTrack = mListItems.get(position);

        AudioMediaFragment audioMediaFragment = AudioMediaFragment.newInstance(mEncodedEmail,
                mUserName, mSharedWith);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            audioMediaFragment.setSharedElementEnterTransition(new AudioMediaTransition());
            audioMediaFragment.setEnterTransition(new Fade());
            setExitTransition(new Fade());
            audioMediaFragment.setSharedElementReturnTransition(new AudioMediaTransition());
        }

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addSharedElement(holder.mTrackImageView, "imageView")
                .addSharedElement(holder.mTitleDescription, "descriptionView")
                .addSharedElement(holder.mTitleTextView, "titleView")
                .replace(R.id.container_audio, audioMediaFragment)
                .addToBackStack(null)
                .commit();
    }
}
