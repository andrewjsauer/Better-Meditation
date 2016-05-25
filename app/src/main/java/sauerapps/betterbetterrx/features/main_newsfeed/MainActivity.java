package sauerapps.betterbetterrx.features.main_newsfeed;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.app.BaseActivity;
import sauerapps.betterbetterrx.app.User;
import sauerapps.betterbetterrx.features.journal.JournalActivity;
import sauerapps.betterbetterrx.features.main_newsfeed.menu.ChangePasswordDialog;
import sauerapps.betterbetterrx.features.main_newsfeed.sharing.audioSharing.ShareMainActivity;
import sauerapps.betterbetterrx.features.meditation.AudioActivity;
import sauerapps.betterbetterrx.utils.Constants;

public class MainActivity extends BaseActivity {

    //TODO Share icon does not work without AudioDetails

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

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

    private boolean mUserLearnedDrawer;
    private int mCurrentSelectedPosition;

    protected String mUsersName;

    private Firebase mUserRef;
    private ValueEventListener mUserRefListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsfeed_main);

        ButterKnife.bind(this);

        SummaryUserFragment summaryUserFragment = SummaryUserFragment.newInstance(mEncodedEmail);
        SummaryFriendsFragment summaryFriendsFragment = SummaryFriendsFragment.newInstance(mEncodedEmail);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.summary_fragment, summaryUserFragment)
                    .add(R.id.summary_friends, summaryFriendsFragment)
                    .commit();
        }

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
                    case R.id.navigation_item_1:
                        changePassword();
                        mCurrentSelectedPosition = 0;
                        return true;
                    case R.id.navigation_item_2:
                        logout();
                        mCurrentSelectedPosition = 5;
                        return true;
                    default:
                        return true;
                }
            }
        });

//        mUserRef = new Firebase(Constants.FIREBASE_URL_USERS).child(mEncodedEmail);
//
//        // getting first name for toolbar reference
//        mUserRefListener = mUserRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                User user = snapshot.getValue(User.class);
//                if (user != null) {
//                    mUsersName = user.getName().split("\\s+")[0];
//                    String title = mUsersName + "'s Dashboard";
//
//                    if (getSupportActionBar() != null) {
//                        getSupportActionBar().setTitle(title);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//                Log.e(LOG_TAG,
//                        getString(R.string.log_error_the_read_failed) +
//                                firebaseError.getMessage());
//            }
//        });
    }

    private void changePassword() {
        new ChangePasswordDialog(this, mEncodedEmail)
                .show();
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        mUserRef.removeEventListener(mUserRefListener);
//    }

    private void setUpToolbar() {
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setTitle("andrew's meditation");

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
        intent.putExtra(Constants.KEY_NAME, mUsersName);
        startActivity(intent);
    }

    @OnClick(R.id.journal_button)
    protected void onClickJournal() {
        Intent intent = new Intent(MainActivity.this, JournalActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share_list) {
            Intent intent = new Intent(MainActivity.this, ShareMainActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
