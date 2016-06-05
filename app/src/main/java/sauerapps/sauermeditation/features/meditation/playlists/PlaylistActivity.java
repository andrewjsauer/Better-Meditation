package sauerapps.sauermeditation.features.meditation.playlists;

import android.content.Intent;
import android.os.Bundle;
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
import sauerapps.sauermeditation.features.meditation.playlistDetails.PlaylistTracksActivity;
import sauerapps.sauermeditation.features.meditation.soundcloud.Playlists;
import sauerapps.sauermeditation.features.meditation.soundcloud.SCService;
import sauerapps.sauermeditation.features.meditation.soundcloud.SoundCloud;
import sauerapps.sauermeditation.utils.Constants;

public class PlaylistActivity extends BaseActivity implements PlaylistClickListener {

    private static final String TAG = PlaylistActivity.class.getSimpleName();

    int mPlaylistPosition;

    PlaylistsAdapter mAdapter;

    List<Playlists> mPlaylistsList;

    @Bind(R.id.recyclerview_playlist)
    RecyclerView mRecyclerView;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.progressBar_playlists)
    ProgressBar mProgressBar;

    String mUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_activity);

        ButterKnife.bind(this);

        Intent intent = this.getIntent();
        mUserName = intent.getStringExtra(Constants.KEY_USER_NAME);

        initializeScreen();

    }

    private void initializeScreen() {
        mProgressBar.setVisibility(View.VISIBLE);

        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            setTitle("Meditation List");
        }

        mPlaylistsList = new ArrayList<>();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new PlaylistsAdapter(mPlaylistsList, PlaylistActivity.this, this);

        mRecyclerView.setAdapter(mAdapter);

        SCService scService = SoundCloud.getService();
        Call<List<Playlists>> call = scService.getPlaylists();
        call.enqueue(new Callback<List<Playlists>>() {
            @Override
            public void onResponse(Call<List<Playlists>> call, Response<List<Playlists>> response) {
                if (response.isSuccess()) {
                    loadTracks(response.body());
                    Log.d(TAG, "Something worked");
                } else {
                    int statusCode = response.code();
                    Toast.makeText(PlaylistActivity.this, "Error: " + statusCode, Toast.LENGTH_LONG).show();
                    Log.d(TAG, statusCode + "");
                }
            }

            @Override
            public void onFailure(Call<List<Playlists>> call, Throwable t) {
                Log.d(TAG, "Error: " + t);
                Toast.makeText(PlaylistActivity.this, "Error: Check internet connectivity. " + t, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadTracks(List<Playlists> playlist) {
        mPlaylistsList.clear();
        mPlaylistsList.addAll(playlist);
        mAdapter.notifyDataSetChanged();
        mProgressBar.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onPlaylistClicked(PlaylistsHolder holder, int position) {
        Playlists playlistPosition = mPlaylistsList.get(position);
        mPlaylistPosition = playlistPosition.getID();

        Intent intent = new Intent(PlaylistActivity.this, PlaylistTracksActivity.class);
        intent.putExtra(Constants.KEY_USER_NAME, mUserName);
        intent.putExtra(Constants.KEY_PLAYLIST_POSITION, mPlaylistPosition);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
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