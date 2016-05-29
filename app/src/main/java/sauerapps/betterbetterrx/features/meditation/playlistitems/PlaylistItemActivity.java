package sauerapps.betterbetterrx.features.meditation.playlistitems;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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
import sauerapps.betterbetterrx.app.User;
import sauerapps.betterbetterrx.features.meditation.AudioMediaFragment;
import sauerapps.betterbetterrx.features.meditation.soundcloud.SCService;
import sauerapps.betterbetterrx.features.meditation.soundcloud.SoundCloud;
import sauerapps.betterbetterrx.features.meditation.soundcloud.Track;
import sauerapps.betterbetterrx.features.meditation.soundcloud.Tracks;
import sauerapps.betterbetterrx.utils.Constants;

public class PlaylistItemActivity extends AppCompatActivity implements AudioClickListener {

    private static final String TAG = PlaylistItemActivity.class.getSimpleName();

    public static Track mTrack;

    protected TrackAdapter mAdapter;
    protected List<Track> mListItems;

    @Bind(R.id.progressBar_audio_list)
    ProgressBar mProgressBar;

    @Bind(R.id.recyclerviewRx)
    RecyclerView mRecyclerView;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    ValueEventListener mSharedWithListener;
    HashMap<String, User> mSharedWith;
    Firebase mSharedWithRef;

    String mUserEncodedEmail;
    String mUserName;
    int mPlaylistPosition;

    public PlaylistItemActivity() {
        /* Required empty public constructor */
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_item);

        ButterKnife.bind(this);

        Intent intent = this.getIntent();
        mUserName = intent.getStringExtra(Constants.KEY_USER_NAME);
        mPlaylistPosition = intent.getIntExtra(Constants.KEY_PLAYLIST_POSITION, 1);
        mUserEncodedEmail = intent.getStringExtra(Constants.KEY_ENCODED_EMAIL);

        initializeScreen();
    }

    private void initializeScreen() {

        mProgressBar.setVisibility(View.VISIBLE);

        setSupportActionBar(mToolbar);

        if (mToolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            setTitle("Meditations");
        }

        mSharedWithRef = new Firebase(Constants.FIREBASE_URL_AUDIO_DETAILS_SHARED_WITH).child(mUserEncodedEmail);
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

        mListItems = new ArrayList<>();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new TrackAdapter(mListItems, this, this);

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
                    Toast.makeText(PlaylistItemActivity.this, "Error: " + statusCode, Toast.LENGTH_LONG).show();
                    Log.d(TAG, statusCode + "");
                }
            }

            @Override
            public void onFailure(Call<Tracks> call, Throwable t) {
                Log.d(TAG, "Error: " + t);
                Toast.makeText(PlaylistItemActivity.this, "Error: Check internet connectivity. " + t, Toast.LENGTH_LONG).show();
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

        AudioMediaFragment audioMediaFragment = AudioMediaFragment.newInstance(mUserEncodedEmail,
                mUserName, mSharedWith);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            audioMediaFragment.setSharedElementEnterTransition(new AudioMediaTransition());
//            audioMediaFragment.setEnterTransition(new Fade());
//            setExitTransition(new Fade());
//            audioMediaFragment.setSharedElementReturnTransition(new AudioMediaTransition());
//        }

//        getActivity().getSupportFragmentManager()
//                .beginTransaction()
//                .addSharedElement(holder.mTrackImageView, "imageView")
//                .addSharedElement(holder.mTitleDescription, "descriptionView")
//                .addSharedElement(holder.mTitleTextView, "titleView")
//                .replace(R.id.container_audio, audioMediaFragment)
//                .addToBackStack(null)
//                .commit();
    }
}
