package sauerapps.betterbetterrx.features.newsfeed;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerViewAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.features.meditation.AudioList;
import sauerapps.betterbetterrx.utils.Constants;

public class SummaryFriendsFragment extends Fragment {

    private String mEncodedEmail;
    private String mUserEmailCheck;
    private String mUsersName;

    private Firebase mRef;

    private FirebaseRecyclerViewAdapter<AudioList, AudioListHolder> mRecycleViewAdapter;

    private HashMap<String, Object> mFriendDate;
    private double mFriendAudioTime;

    private String mFriendDateFormatted;
    private String mFriendAudioTimeFormatted;


    public SummaryFriendsFragment() {
    }

    public static SummaryFriendsFragment newInstance(String encodedEmail) {
        SummaryFriendsFragment fragment = new SummaryFriendsFragment();
        Bundle args = new Bundle();
        args.putString(Constants.KEY_ENCODED_EMAIL, encodedEmail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mEncodedEmail = getArguments().getString(Constants.KEY_ENCODED_EMAIL);
        }

        mRef = new Firebase(Constants.FIREBASE_URL_USER_AUDIO_DETAILS_LIST).child(mEncodedEmail);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_summary, container, false);
        ButterKnife.bind(this, view);

        initializeScreen(view);

        return view;
    }

    private void initializeScreen(View view) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.friends_audio_list);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setReverseLayout(false);

        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(manager);

        mRecycleViewAdapter = new FirebaseRecyclerViewAdapter<AudioList,
                AudioListHolder>(AudioList.class, R.layout.audio_list_item,
                AudioListHolder.class, mRef) {
            @Override
            protected void populateViewHolder(AudioListHolder viewHolder, AudioList model, int position) {
                if (model != null) {
                    mFriendDate = model.getTimestampCreated();
                    mFriendAudioTime = model.getAudioTime();
                    mUserEmailCheck = model.getOwner();
                    mUsersName = model.getUserName();

                    getLastMeditationTime();
                    getLastMinuteDate();
                    getUserName();

                    viewHolder.userFriendName.setText(mUsersName);
                    viewHolder.userFriendDate.setText(mFriendDateFormatted);
                    viewHolder.userFriendTrackTime.setText(mFriendAudioTimeFormatted);
                    viewHolder.userFriendTrackTitle.setText(model.getTrackTitle());
                    viewHolder.userFriendTrackDesc.setText(model.getTrackDescription());
                }
            }
        };

        recyclerView.setAdapter(mRecycleViewAdapter);
    }

    private void getUserName() {
        if (mUserEmailCheck.equals(mEncodedEmail)) {
            mUsersName = "You";
        } else {
            mUsersName.equals(mUsersName);
        }
    }

    private void getLastMinuteDate() {
        Object userLastDate = mFriendDate.get(Constants.FIREBASE_PROPERTY_TIMESTAMP);

        long dateTime = ((long)userLastDate);

        Date date = new Date(dateTime);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd hh:mm a", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("PST"));
        mFriendDateFormatted = sdf.format(date);

    }

    private void getLastMeditationTime() {
        mFriendAudioTimeFormatted = (String.format(Locale.ENGLISH, "%01d:%02d",
                TimeUnit.MILLISECONDS.toHours((long) mFriendAudioTime),
                TimeUnit.MILLISECONDS.toMinutes((long) mFriendAudioTime)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours((long) mFriendAudioTime))));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRecycleViewAdapter.cleanup();
    }
}
