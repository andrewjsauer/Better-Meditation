package sauerapps.betterbetterrx.features.meditation.audioSection;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.app.BaseActivity;
import sauerapps.betterbetterrx.features.meditation.audioSection.journal.JournalFragment;
import sauerapps.betterbetterrx.features.soundcloud.SCService;
import sauerapps.betterbetterrx.features.soundcloud.SoundCloud;
import sauerapps.betterbetterrx.features.soundcloud.Track;

public class AudioListFragment extends Fragment implements AudioClickListener {

    private static final String TAG = AudioListFragment.class.getSimpleName();

    public static Track mTrack;
    public static int mTrackPosition;

    protected TrackRxAdapter mAdapter;
    protected List<Track> mListItems;


    @Bind(R.id.recyclerviewRx)
    protected RecyclerView mRecyclerView;
    @Bind(R.id.toolbar)
    protected Toolbar mToolbar;
    @Bind(R.id.journal_button)
    protected FloatingActionButton mJournalButton;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.audio_list_fragment, container, false);
        ButterKnife.bind(this, v);

        BaseActivity activity = (BaseActivity) getActivity();

        activity.setSupportActionBar(mToolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);


        mJournalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JournalFragment journalFragment = new JournalFragment();

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, journalFragment, null)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListItems = new ArrayList<>();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new TrackRxAdapter(mListItems, getContext(), this);

        mRecyclerView.setAdapter(mAdapter);


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
    }

    private void loadTracks(List<Track> tracks) {
        mListItems.clear();
        mListItems.addAll(tracks);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTrackClicked(TrackRxHolder holder, int position) {
        int trackNumber = position;

        Track track = mListItems.get(position);

        mTrack = track;
        mTrackPosition = position;

        AudioDetailsFragment trackDetails = AudioDetailsFragment.newInstance(trackNumber);

        // Note that we need the API version check here because the actual transition classes (e.g. Fade)
        // are not in the support library and are only available in API 21+. The methods we are calling on the Fragment
        // ARE available in the support library (though they don't do anything on API < 21)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            trackDetails.setSharedElementEnterTransition(new AudioDetailsTransition());
            trackDetails.setEnterTransition(new Fade());
            setExitTransition(new Fade());
            trackDetails.setSharedElementReturnTransition(new AudioDetailsTransition());
        }

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addSharedElement(holder.mTrackImageView, "imageView")
                .addSharedElement(holder.mTitleDescription, "descriptionView")
                .addSharedElement(holder.mTitleTextView, "titleView")
                .replace(R.id.container, trackDetails)
                .addToBackStack(null)
                .commit();
    }
}
