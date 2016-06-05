package sauerapps.sauermeditation.features.main_newsfeed;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseRecyclerViewAdapter;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sauerapps.sauermeditation.R;
import sauerapps.sauermeditation.app.BaseActivity;
import sauerapps.sauermeditation.features.journal.JournalActivity;
import sauerapps.sauermeditation.features.main_newsfeed.menu.AboutDialog;
import sauerapps.sauermeditation.features.main_newsfeed.menu.ChangePasswordDialog;
import sauerapps.sauermeditation.features.main_newsfeed.sharing.audioSharing.ShareMainActivity;
import sauerapps.sauermeditation.features.meditation.playlists.PlaylistActivity;
import sauerapps.sauermeditation.features.meditation.audioSection.AudioList;
import sauerapps.sauermeditation.utils.Constants;

public class MainActivity extends BaseActivity {

    //TODO Share icon does not work without AudioDetails

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String PREFERENCES_FILE = "mymaterialapp_settings";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    @Bind(R.id.toolbar_main_activity)
    protected Toolbar mToolbar;

    @Bind(R.id.friends_audio_list)
    protected RecyclerView mRecyclerView;

    @Bind(R.id.nav_drawer)
    protected DrawerLayout mDrawerLayout;

    @Bind(R.id.nav_view)
    protected NavigationView mNavigationView;

    @Bind(R.id.nav_contentframe)
    protected LinearLayout mFrameLayout;

    @Bind(R.id.summary_total_sessions_text_view)
    protected TextView mTotalSessions;

    @Bind(R.id.summary_total_time_text_view)
    protected TextView mTotalTime;

    @Bind(R.id.summary_total_journal_text_view)
    protected TextView mTotalJournals;

    private Firebase mUserAudioDetailsListRef, mUserAudioTimeTotalRef, mUserEntriesTotalRef, mUserAudioDetailsRef;
    private ValueEventListener mUserAudioTimeTotalListener, mUserEntriesTotalListener, mUserAudioDetailsListener;

    private boolean mUserLearnedDrawer;
    private int mCurrentSelectedPosition;

    protected String mUserEmail;
    private String mUsersName;
    private String mUserEmailCheck;

    private String mTimeSession;

    private FirebaseRecyclerViewAdapter<AudioList, FriendsDetailListHolder> mRecycleViewAdapter;

    private HashMap<String, Object> mFriendDate;
    private double mFriendAudioTime;

    private String mFriendDateFormatted;
    private String mFriendAudioTimeFormatted;

    private CardView mSummaryUserLayout;

    public MainActivity() {
        /* Required empty public constructor */
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsfeed_main);

        ButterKnife.bind(this);

        mUserEmail = mEncodedEmail;

        initializeScreen(savedInstanceState);

    }

    private void initializeScreen(Bundle savedInstanceState) {

        setUpToolbar();

        mUserLearnedDrawer = Boolean.valueOf(readSharedSetting(this, PREF_USER_LEARNED_DRAWER, "false"));

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
        }

        setUpNavDrawer();

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                menuItem.setChecked(true);
                switch (menuItem.getItemId()) {
                    case R.id.navigation_item_share_with_users:
                        Intent intent = new Intent(MainActivity.this, ShareMainActivity.class);
                        startActivity(intent);

                        Bundle shareBundle = new Bundle();
                        shareBundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Share with others clicked");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, shareBundle);

                        mCurrentSelectedPosition = 0;
                        return true;
                    case R.id.navigation_item_change_password:
                        changePassword();

                        Bundle changePasswordBundle = new Bundle();
                        changePasswordBundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Change password clicked");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, changePasswordBundle);

                        mCurrentSelectedPosition = 1;
                        return true;
                    case R.id.navigation_item_about:
                        aboutDialog();

                        Bundle aboutBundle = new Bundle();
                        aboutBundle.putString(FirebaseAnalytics.Param.ITEM_ID, "About section clicked");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, aboutBundle);

                        mCurrentSelectedPosition = 2;
                        return true;
                    case R.id.navigation_item_logout:
                        logout();

                        Bundle logoutBundle = new Bundle();
                        logoutBundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Logout clicked");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, logoutBundle);

                        mCurrentSelectedPosition = 3;
                        return true;
                    default:
                        return true;
                }
            }
        });

        mUserAudioDetailsListRef = new Firebase(Constants.FIREBASE_URL_USER_AUDIO_DETAILS_LIST).child(mEncodedEmail);
        mUserAudioDetailsRef = new Firebase(Constants.FIREBASE_URL_USER_AUDIO_DETAILS).child(mUserEmail);
        mUserAudioTimeTotalRef = new Firebase(Constants.FIREBASE_URL_USER_AUDIO).child(mUserEmail);
        mUserEntriesTotalRef = new Firebase(Constants.FIREBASE_URL_USER_LISTS).child(mUserEmail);

        Query queryAudioDetailsList = mUserAudioDetailsListRef.orderByKey();


        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);

        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(manager);

        mRecycleViewAdapter =
                new FirebaseRecyclerViewAdapter<AudioList,
                FriendsDetailListHolder>(AudioList.class,
                        R.layout.friends_summary_list_item,
                FriendsDetailListHolder.class, queryAudioDetailsList) {
            @Override
            protected void populateViewHolder(FriendsDetailListHolder viewHolder, AudioList model, final int position) {
                if (model != null) {
                    mFriendDate = model.getTimestampCreated();
                    mFriendAudioTime = model.getAudioTime();
                    mUserEmailCheck = model.getOwner();
                    mUsersName = model.getUserName();

                    getLastMeditationTime();
                    getFormattedDate();
                    getUserName();

                    viewHolder.userFriendName.setText(mUsersName);
                    viewHolder.userFriendDate.setText(mFriendDateFormatted);
                    viewHolder.userFriendTrackTime.setText(mFriendAudioTimeFormatted);
                    viewHolder.userFriendTrackTitle.setText(model.getTrackTitle());
                    viewHolder.userFriendTrackDesc.setText(model.getTrackDescription());

                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String usersEmail = getItem(position).getOwner();
                            String userName = getItem(position).getUserName();

                            FragmentManager fragmentManager = getFragmentManager();

                            DialogFragment dialog = UserDetailsFragmentDialog.newInstance(usersEmail, mEncodedEmail, userName);
                            dialog.show(fragmentManager, "UserDetailsFragmentDialog");

                            Bundle friendBundle = new Bundle();
                            friendBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Friends Summary clicked");
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, friendBundle);

                        }
                    });
                }
            }
        };

        mRecyclerView.setAdapter(mRecycleViewAdapter);

        mSummaryUserLayout = (CardView) findViewById(R.id.user_summary_layout);
        mSummaryUserLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(mTimeSession)) {
                    FragmentManager fragmentManager = getFragmentManager();
                    DialogFragment dialog = UserDetailsFragmentDialog.newInstance(mEncodedEmail, mEncodedEmail, "");
                    dialog.show(fragmentManager, "UserDetailsFragmentDialog");

                    Bundle userBundle = new Bundle();
                    userBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Users Summary clicked");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, userBundle);

                } else {
                    Toast.makeText(MainActivity.this, "No recent activity", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        mUserAudioTimeTotalListener = mUserAudioTimeTotalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    double totalTime = (Double) dataSnapshot.getValue();

                    mTimeSession = String.format(Locale.ENGLISH, "%01d hr %02d min",
                            TimeUnit.MILLISECONDS.toHours((long) totalTime),
                            TimeUnit.MILLISECONDS.toMinutes((long) totalTime)
                                    - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours((long) totalTime)));

                    mTotalTime.setText(mTimeSession);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG,
                        getString(R.string.log_error_the_read_failed) +
                                firebaseError.getMessage());
            }
        });

        mUserAudioDetailsListener = mUserAudioDetailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    long totalMeditations = dataSnapshot.getChildrenCount();
                    String sessionTotal = Long.toString(totalMeditations);
                    mTotalSessions.setText(sessionTotal);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG,
                        getString(R.string.log_error_the_read_failed) +
                                firebaseError.getMessage());
            }
        });

        mUserEntriesTotalListener = mUserEntriesTotalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long totalEntries = dataSnapshot.getChildrenCount();
                    String sessionTotal = Long.toString(totalEntries);
                    mTotalJournals.setText(sessionTotal);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUserAudioDetailsRef.removeEventListener(mUserAudioDetailsListener);
        mUserAudioTimeTotalRef.removeEventListener(mUserAudioTimeTotalListener);
        mUserEntriesTotalRef.removeEventListener(mUserEntriesTotalListener);
        mRecycleViewAdapter.cleanup();
    }

    @OnClick(R.id.meditation_button)
    protected void onClickMeditation() {
        Intent intent = new Intent(MainActivity.this, PlaylistActivity.class);
        intent.putExtra(Constants.KEY_USER_NAME, mUsersName);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        Bundle meditationBundle = new Bundle();
        meditationBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Meditation button clicked");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, meditationBundle);

    }

    @OnClick(R.id.journal_button)
    protected void onClickJournal() {
        Intent intent = new Intent(MainActivity.this, JournalActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        Bundle journalBundle = new Bundle();
        journalBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Journal button clicked");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, journalBundle);
    }

    @OnClick(R.id.group_meditation_button)
    protected void onClickGroup() {
        Toast.makeText(MainActivity.this, "Coming soon!", Toast.LENGTH_SHORT).show();

        Bundle groupBundle = new Bundle();
        groupBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Group meditation button clicked");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, groupBundle);
    }

    @OnClick(R.id.custom_meditation_button)
    protected void onClickCustom() {
        Toast.makeText(MainActivity.this, "Coming soon!", Toast.LENGTH_SHORT).show();

        Bundle customBundle = new Bundle();
        customBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Custom meditation button clicked");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, customBundle);
    }


    private void aboutDialog() {
        AboutDialog aboutDialog = new AboutDialog(this);
        aboutDialog.show();
    }

    private void changePassword() {
        ChangePasswordDialog dialog = new ChangePasswordDialog(this);
        dialog.setUserEmail(mUserEmail);
        dialog.show();
    }

    private void setUpToolbar() {
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            setTitle("sauer meditation");
        }
    }

    private void setUpNavDrawer() {
        if (mToolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mToolbar.setNavigationIcon(R.drawable.ic_drawer);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        if (!mUserLearnedDrawer) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            mUserLearnedDrawer = true;
            saveSharedSetting(this, PREF_USER_LEARNED_DRAWER, "true");
        }
    }

    private void getUserName() {
        if (mUserEmailCheck.equals(mEncodedEmail)) {
            mUsersName = "You";
        } else {
            mUsersName.equals(mUsersName);
        }
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

    private void getLastMeditationTime() {
        mFriendAudioTimeFormatted = (String.format(Locale.ENGLISH, "%01d:%02d",
                TimeUnit.MILLISECONDS.toHours((long) mFriendAudioTime),
                TimeUnit.MILLISECONDS.toMinutes((long) mFriendAudioTime)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours((long) mFriendAudioTime))));
    }

    public static void saveSharedSetting(Context ctx, String settingName, String settingValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(settingName, settingValue);
        editor.apply();
    }

    public static String readSharedSetting(Context ctx, String settingName, String defaultValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(settingName, defaultValue);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION, 0);
        Menu menu = mNavigationView.getMenu();
        menu.getItem(mCurrentSelectedPosition).setChecked(true);
    }
}
