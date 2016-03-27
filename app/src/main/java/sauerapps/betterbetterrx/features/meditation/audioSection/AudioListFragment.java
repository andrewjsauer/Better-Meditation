package sauerapps.betterbetterrx.features.meditation.audioSection;

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
import sauerapps.betterbetterrx.features.soundcloud.SCService;
import sauerapps.betterbetterrx.features.soundcloud.SoundCloud;
import sauerapps.betterbetterrx.features.soundcloud.Track;
import sauerapps.betterbetterrx.utils.Constants;

public class AudioListFragment extends Fragment implements AudioClickListener {

    private static final String TAG = AudioListFragment.class.getSimpleName();

    private String mEncodedEmail;
    private String mUserName;

    public static Track mTrack;
    public static int mTrackPosition;

    protected TrackRxAdapter mAdapter;
    protected List<Track> mListItems;


    @Bind(R.id.recyclerviewRx)
    protected RecyclerView mRecyclerView;
    @Bind(R.id.toolbar)
    protected Toolbar mToolbar;



    public AudioListFragment() {
        /* Required empty public constructor */
    }

    public static AudioListFragment newInstance(String encodedEmail, String userName) {
        AudioListFragment fragment = new AudioListFragment();
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
        View v = inflater.inflate(R.layout.fragment_audio_list, container, false);
        ButterKnife.bind(this, v);

        BaseActivity activity = (BaseActivity) getActivity();

        if (mToolbar != null) {
            activity.setSupportActionBar(mToolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
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
                    Toast.makeText(getActivity(), "Error: " + statusCode, Toast.LENGTH_LONG).show();
                    Log.d(TAG, statusCode + "");
                }
            }

            @Override
            public void onFailure(Call<List<Track>> call, Throwable t) {
                Log.d(TAG, "Error: " + t);
                Toast.makeText(getActivity(), "Error: Check internet connectivity. " + t, Toast.LENGTH_LONG).show();
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
        Track track = mListItems.get(position);

        mTrack = track;
        mTrackPosition = position;

        AudioDetailsFragment audioDetailsFragment = AudioDetailsFragment.newInstance(mEncodedEmail, mUserName);

        // Note that we need the API version check here because the actual transition classes (e.g. Fade)
        // are not in the support library and are only available in API 21+. The methods we are calling on the Fragment
        // ARE available in the support library (though they don't do anything on API < 21)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            audioDetailsFragment.setSharedElementEnterTransition(new AudioDetailsTransition());
            audioDetailsFragment.setEnterTransition(new Fade());
            setExitTransition(new Fade());
            audioDetailsFragment.setSharedElementReturnTransition(new AudioDetailsTransition());
        }

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addSharedElement(holder.mTrackImageView, "imageView")
                .addSharedElement(holder.mTitleDescription, "descriptionView")
                .addSharedElement(holder.mTitleTextView, "titleView")
                .replace(R.id.container_audio, audioDetailsFragment)
                .addToBackStack(null)
                .commit();
    }
}
