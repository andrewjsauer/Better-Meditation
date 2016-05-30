package sauerapps.betterbetterrx.features.main_newsfeed;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseRecyclerViewAdapter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.features.meditation.audioSection.AudioList;
import sauerapps.betterbetterrx.utils.Constants;

/**
 * Created by andrewsauer on 5/29/16.
 */
public class UserDetailsFragmentDialog extends DialogFragment {

    protected String mUserEmail;
    protected String mCurrentUserEmail;
    protected String mUserName;

    protected Firebase userAudioDetailsRef;
    protected FirebaseRecyclerViewAdapter<AudioList, UserDetailsFragmentDialogListHolder> mRecycleViewAdapter;

    private HashMap<String, Object> mFriendDate;
    private String mFriendDateFormatted;
    private String mFriendAudioTimeFormatted;
    private double mFriendAudioTime;

    @Bind(R.id.user_details_fragment_dialog_recyclerview)
    protected RecyclerView mRecyclerView;

    @Bind(R.id.dialog_recent_activity_title)
    protected TextView mTitleDialog;


    public static UserDetailsFragmentDialog newInstance(String userEmail, String currentUserEmail, String userName) {
        UserDetailsFragmentDialog userDetailsFragmentDialog = new UserDetailsFragmentDialog();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_ENCODED_EMAIL, userEmail);
        bundle.putString(Constants.KEY_USERS_EMAIL, currentUserEmail);
        bundle.putString(Constants.KEY_NAME, userName);
        userDetailsFragmentDialog.setArguments(bundle);
        return userDetailsFragmentDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserEmail = getArguments().getString(Constants.KEY_ENCODED_EMAIL);
        mCurrentUserEmail = getArguments().getString(Constants.KEY_USERS_EMAIL);
        mUserName = getArguments().getString(Constants.KEY_NAME);

        userAudioDetailsRef = new Firebase(Constants.FIREBASE_URL_USER_AUDIO_DETAILS).child(mUserEmail);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_details_fragment_dialog, container, false);
        ButterKnife.bind(this, view);

        Query queryRef = userAudioDetailsRef.limitToFirst(10);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setReverseLayout(false);

        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(manager);

        mRecycleViewAdapter =
                new FirebaseRecyclerViewAdapter<AudioList,
                UserDetailsFragmentDialogListHolder>(AudioList.class,
                R.layout.user_details_dialog_list_items,
                UserDetailsFragmentDialogListHolder.class, queryRef) {
            @Override
            protected void populateViewHolder(UserDetailsFragmentDialogListHolder viewHolder, AudioList model, int position) {
                if (model != null) {
                    mFriendDate = model.getTimestampCreated();
                    mFriendAudioTime = model.getAudioTime();

                    getFormattedTime();
                    getFormattedDate();
                    getUserName();

                    viewHolder.userDate.setText(mFriendDateFormatted);
                    viewHolder.userTrackTitle.setText(model.getTrackTitle());
                    viewHolder.userTrackDesc.setText(model.getTrackDescription());
                    viewHolder.userTrackTime.setText(mFriendAudioTimeFormatted);
                }
            }
        };

        mRecyclerView.setAdapter(mRecycleViewAdapter);

        return view;
    }

    @OnClick(R.id.dialog_exit_button)
    public void setExitButton() {
        dismiss();
    }

    private void getFormattedDate() {

        Object userLastDate = mFriendDate.get(Constants.FIREBASE_PROPERTY_TIMESTAMP);

        long dateTime = ((long)userLastDate);

        DateTime jodaTime = new DateTime(dateTime);

        DateTimeFormatter outputFormatter = DateTimeFormat
                .forPattern("MM/dd/yyyy hh:mm a")
                .withLocale(Locale.US)
                .withZone(DateTimeZone.getDefault());

        mFriendDateFormatted = outputFormatter.print(jodaTime);

    }

    private void getUserName() {
        if (mCurrentUserEmail.equals(mUserEmail)) {
            mTitleDialog.setText(R.string.dialog_summary_title_current_user);
        } else {
            mTitleDialog.setText(mUserName + "'s" + getString(R.string.dialog_summary_friends_title));
        }
    }

    private void getFormattedTime() {
        mFriendAudioTimeFormatted = (String.format(Locale.ENGLISH, "%01d:%02d",
                TimeUnit.MILLISECONDS.toHours((long) mFriendAudioTime),
                TimeUnit.MILLISECONDS.toMinutes((long) mFriendAudioTime)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours((long) mFriendAudioTime))));
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        mRecycleViewAdapter.cleanup();
    }
}
