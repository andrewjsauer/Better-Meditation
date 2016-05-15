package sauerapps.betterbetterrx.features.meditation.playlists;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.app.BaseActivity;
import sauerapps.betterbetterrx.features.meditation.AudioListFragment;
import sauerapps.betterbetterrx.features.meditation.soundcloud.Playlists;
import sauerapps.betterbetterrx.features.meditation.soundcloud.SCService;
import sauerapps.betterbetterrx.features.meditation.soundcloud.SoundCloud;
import sauerapps.betterbetterrx.utils.Constants;

/**
 * Created by andrewsauer on 4/14/16.
 */
public class PlaylistFragment extends Fragment implements PlaylistClickListener {

    private static final String TAG = PlaylistFragment.class.getSimpleName();

    private int mPlaylistPosition;

    private PlaylistsAdapter mAdapter;
    private List<Playlists> mPlaylistsList;

    @Bind(R.id.recyclerview_playlist)
    protected RecyclerView mRecyclerView;
    @Bind(R.id.toolbar)
    protected Toolbar mToolbar;
    @Bind(R.id.progressBar_playlists)
    protected ProgressBar mProgressBar;

    private String mEncodedEmail;
    private String mUserName;

    public PlaylistFragment() {
        /* Required empty public constructor */
    }

    public static PlaylistFragment newInstance(String encodedEmail, String userName) {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        args.putString(Constants.KEY_ENCODED_EMAIL, encodedEmail);
        args.putString(Constants.KEY_NAME, userName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEncodedEmail = getArguments().getString(Constants.KEY_ENCODED_EMAIL);
            mUserName = getArguments().getString(Constants.KEY_NAME);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_audio_playlist, container, false);
        ButterKnife.bind(this, v);

        mProgressBar.setVisibility(View.VISIBLE);

        BaseActivity activity = (BaseActivity) getActivity();

        activity.setSupportActionBar(mToolbar);

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            activity.setTitle("Meditation List");
        }

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPlaylistsList = new ArrayList<>();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new PlaylistsAdapter(mPlaylistsList, getContext(), this);

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
                    Toast.makeText(getActivity(), "Error: " + statusCode, Toast.LENGTH_LONG).show();
                    Log.d(TAG, statusCode + "");
                }
            }

            @Override
            public void onFailure(Call<List<Playlists>> call, Throwable t) {
                Log.d(TAG, "Error: " + t);
                Toast.makeText(getActivity(), "Error: Check internet connectivity. " + t, Toast.LENGTH_LONG).show();
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

        Log.d("PlaylistFragment", mPlaylistPosition + "");

        AudioListFragment audioListFragment = AudioListFragment.newInstance(mEncodedEmail
                , mUserName, mPlaylistPosition);
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container_audio, audioListFragment)
                .addToBackStack(null)
                .commit();
    }
}
