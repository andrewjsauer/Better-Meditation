package sauerapps.betterbetterrx.features.meditation.playlistDetails;

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
import sauerapps.betterbetterrx.features.meditation.audioSection.AudioActivity;
import sauerapps.betterbetterrx.features.meditation.soundcloud.SCService;
import sauerapps.betterbetterrx.features.meditation.soundcloud.SoundCloud;
import sauerapps.betterbetterrx.features.meditation.soundcloud.Track;
import sauerapps.betterbetterrx.features.meditation.soundcloud.Tracks;
import sauerapps.betterbetterrx.utils.Constants;

public class PlaylistDetailsActivity extends AppCompatActivity implements PlaylistDetailsClickListener {

    private static final String TAG = PlaylistDetailsActivity.class.getSimpleName();

    public static Track mTrack;

    protected PlaylistDetailsAdapter mAdapter;
    protected List<Track> mListItems;

    @Bind(R.id.progressBar_audio_list)
    ProgressBar mProgressBar;

    @Bind(R.id.recyclerviewRx)
    RecyclerView mRecyclerView;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    String mUserEncodedEmail;
    String mUserName;
    int mPlaylistPosition;
    int mTrackPosition;

    public PlaylistDetailsActivity() {
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

        mListItems = new ArrayList<>();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new PlaylistDetailsAdapter(mListItems, this, this);

        mRecyclerView.setAdapter(mAdapter);

        SCService scService = SoundCloud.getService();
        Call<Tracks> call = scService.getRecentTracks(mPlaylistPosition);
        call.enqueue(new Callback<Tracks>() {
            @Override
            public void onResponse(Call<Tracks> call, Response<Tracks> response) {
                if (response.isSuccess()) {
                    loadTracks(response.body().mTracks);
                } else {
                    int statusCode = response.code();
                    Toast.makeText(PlaylistDetailsActivity.this, "Error: " + statusCode, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Tracks> call, Throwable t) {
                Log.d(TAG, "Error: " + t);
                Toast.makeText(PlaylistDetailsActivity.this, "Error: Check internet connectivity. " + t, Toast.LENGTH_LONG).show();
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
    public void onTrackClicked(PlaylistDetailsHolder holder, int position) {
        Track trackPosition = mListItems.get(position);
        mTrackPosition = trackPosition.getID();

        mTrack = mListItems.get(position);

        Intent intent = new Intent(PlaylistDetailsActivity.this, AudioActivity.class);
        intent.putExtra(Constants.KEY_USER_NAME, mUserName);
        intent.putExtra(Constants.KEY_ENCODED_EMAIL, mUserEncodedEmail);
        startActivity(intent);

        finish();
    }
}
