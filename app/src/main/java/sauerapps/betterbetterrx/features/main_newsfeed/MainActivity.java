package sauerapps.betterbetterrx.features.main_newsfeed;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseRecyclerViewAdapter;

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
import sauerapps.betterbetterrx.app.BaseActivity;
import sauerapps.betterbetterrx.features.journal.JournalActivity;
import sauerapps.betterbetterrx.features.main_newsfeed.menu.AboutDialog;
import sauerapps.betterbetterrx.features.main_newsfeed.menu.ChangePasswordDialog;
import sauerapps.betterbetterrx.features.main_newsfeed.sharing.audioSharing.ShareMainActivity;
import sauerapps.betterbetterrx.features.meditation.AudioActivity;
import sauerapps.betterbetterrx.features.meditation.AudioList;
import sauerapps.betterbetterrx.utils.Constants;

public class MainActivity extends BaseActivity {

    //TODO Share icon does not work without AudioDetails

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String PREFERENCES_FILE = "mymaterialapp_settings";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    @Bind(R.id.toolbar_main_activity)
    protected Toolbar mToolbar;

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

    private Firebase mRef, mUserAudioTimeTotalRef, mUserEntriesTotalRef, mUserAudioDetailsRef;
    private ValueEventListener mUserAudioTimeTotalListener, mUserEntriesTotalListener, mUserAudioDetailsListener;

    private boolean mUserLearnedDrawer;
    private int mCurrentSelectedPosition;

    protected String mUserEmail;
    private String mUsersName;
    private String mUserEmailCheck;

    private FirebaseRecyclerViewAdapter<AudioList, FriendsDetailListHolder> mRecycleViewAdapter;

    private HashMap<String, Object> mFriendDate;
    private double mFriendAudioTime;

    private String mFriendDateFormatted;
    private String mFriendAudioTimeFormatted;


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
                        mCurrentSelectedPosition = 0;
                        return true;
                    case R.id.navigation_item_change_password:
                        changePassword();
                        mCurrentSelectedPosition = 1;
                        return true;
                    case R.id.navigation_item_about:
                        aboutDialog();
                        mCurrentSelectedPosition = 2;
                        return true;
                    case R.id.navigation_item_logout:
                        logout();
                        mCurrentSelectedPosition = 3;
                        return true;
                    default:
                        return true;
                }
            }
        });

        mRef = new Firebase(Constants.FIREBASE_URL_USER_AUDIO_DETAILS_LIST).child(mEncodedEmail);
        mUserAudioDetailsRef = new Firebase(Constants.FIREBASE_URL_USER_AUDIO_DETAILS).child(mUserEmail);
        mUserAudioTimeTotalRef = new Firebase(Constants.FIREBASE_URL_USER_AUDIO).child(mUserEmail);
        mUserEntriesTotalRef = new Firebase(Constants.FIREBASE_URL_USER_LISTS).child(mUserEmail);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.friends_audio_list);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(false);

        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(manager);

        mRecycleViewAdapter = new FirebaseRecyclerViewAdapter<AudioList,
                FriendsDetailListHolder>(AudioList.class, R.layout.audio_list_item,
                FriendsDetailListHolder.class, mRef) {
            @Override
            protected void populateViewHolder(FriendsDetailListHolder viewHolder, AudioList model, int position) {
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

    @Override
    public void onResume() {
        super.onResume();

        mUserAudioTimeTotalListener = mUserAudioTimeTotalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    double totalTime = (Double) dataSnapshot.getValue();

                    String time = String.format(Locale.ENGLISH, "%01d hr %02d min",
                            TimeUnit.MILLISECONDS.toHours((long) totalTime),
                            TimeUnit.MILLISECONDS.toMinutes((long) totalTime)
                                    - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours((long) totalTime)));

                    mTotalTime.setText(time);
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
    public void onPause() {
        super.onPause();
        mUserAudioDetailsRef.removeEventListener(mUserAudioDetailsListener);
        mUserAudioTimeTotalRef.removeEventListener(mUserAudioTimeTotalListener);
        mUserEntriesTotalRef.removeEventListener(mUserEntriesTotalListener);
        mRecycleViewAdapter.cleanup();
    }


    private void aboutDialog() {
        new AboutDialog(this).show();
    }

    private void changePassword() {
        ChangePasswordDialog dialog = new ChangePasswordDialog(this);
        dialog.setUserEmail(mUserEmail);
        dialog.setTitle("Change Password");
        dialog.show();
    }

    private void setUpToolbar() {
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setTitle("sauer meditation");

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

    @OnClick(R.id.meditation_button)
    protected void onClickMeditation() {
        Intent intent = new Intent(MainActivity.this, AudioActivity.class);
        intent.putExtra(Constants.KEY_NAME, mUserEmail);
        startActivity(intent);
    }

    @OnClick(R.id.journal_button)
    protected void onClickJournal() {
        Intent intent = new Intent(MainActivity.this, JournalActivity.class);
        startActivity(intent);
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
}
