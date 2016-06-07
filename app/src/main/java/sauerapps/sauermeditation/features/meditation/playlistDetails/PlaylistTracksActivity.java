package sauerapps.sauermeditation.features.meditation.playlistDetails;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sauerapps.sauermeditation.R;
import sauerapps.sauermeditation.app.BaseActivity;
import sauerapps.sauermeditation.features.meditation.audioSection.AudioActivity;
import sauerapps.sauermeditation.features.meditation.soundcloud.SCService;
import sauerapps.sauermeditation.features.meditation.soundcloud.SoundCloud;
import sauerapps.sauermeditation.features.meditation.soundcloud.Track;
import sauerapps.sauermeditation.features.meditation.soundcloud.Tracks;
import sauerapps.sauermeditation.utils.Constants;

public class PlaylistTracksActivity extends BaseActivity implements PlaylistTracksClickListener {

    private static final String TAG = PlaylistTracksActivity.class.getSimpleName();

    public static Track mTrack;

    protected PlaylistTracksAdapter mAdapter;
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

    public PlaylistTracksActivity() {
        /* Required empty public constructor */
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_item);

        ButterKnife.bind(this);

        mUserEncodedEmail = mEncodedEmail;

        Intent intent = this.getIntent();
        mUserName = intent.getStringExtra(Constants.KEY_USER_NAME);
        mPlaylistPosition = intent.getIntExtra(Constants.KEY_PLAYLIST_POSITION, 1);

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
        mAdapter = new PlaylistTracksAdapter(mListItems, this, this);

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
                    Toast.makeText(PlaylistTracksActivity.this, "Error: " + statusCode, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Tracks> call, Throwable t) {
                Log.d(TAG, "Error: " + t);
                Toast.makeText(PlaylistTracksActivity.this, "Error: Check internet connectivity. " + t, Toast.LENGTH_LONG).show();
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
    public void onTrackClicked(PlaylistTracksHolder holder, int position) {
        Track trackPosition = mListItems.get(position);
        mTrackPosition = trackPosition.getID();

        mTrack = mListItems.get(position);

        View viewImage = findViewById(R.id.playlist_track_image);
        View viewTitle = findViewById(R.id.playlist_track_title);
        View viewDescription = findViewById(R.id.playlist_track_description);

        Intent intent = new Intent(PlaylistTracksActivity.this, AudioActivity.class);
        Pair<View, String> p1 = Pair.create(viewImage, "trackImage");
        Pair<View, String> p2 = Pair.create(viewTitle, "trackTitle");
        Pair<View, String> p3 = Pair.create(viewDescription, "trackDescription");
        intent.putExtra(Constants.KEY_USER_NAME, mUserName);
        intent.putExtra(Constants.KEY_ENCODED_EMAIL, mUserEncodedEmail);

        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        ActivityOptionsCompat optionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this, p1, p2, p3);

        ActivityCompat.startActivity(this, intent, optionsCompat.toBundle());

        finish();
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
        finish();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }
}
