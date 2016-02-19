package sauerapps.betterbetterrx;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import sauerapps.betterbetterrx.Model.User;
import sauerapps.betterbetterrx.Model.VersionModel;
import sauerapps.betterbetterrx.Adapter.SimpleRecyclerAdapter;
import sauerapps.betterbetterrx.Ui.Ui.FifteenMeditationActivity;
import sauerapps.betterbetterrx.utilities.Constants;
import sauerapps.betterbetterrx.utilities.MyRecyclerScroll;
import sauerapps.betterbetterrx.utilities.Utils;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String PREFERENCES_FILE = "mymaterialapp_settings";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private ValueEventListener mUserRefListener;
    private Firebase mUserRef;


    @Bind(R.id.toolbar) protected Toolbar mToolbar;
    @Bind(R.id.fabhide_toolbar_container) protected LinearLayout mToolbarContainer;
    @Bind(R.id.nav_drawer) protected DrawerLayout mDrawerLayout;
    @Bind(R.id.nav_view) protected NavigationView mNavigationView;
    @Bind(R.id.content_frame) protected FrameLayout mContentFrame;
    @Bind(R.id.fab) protected FloatingActionButton mFloatingActionButton;
    @Bind(R.id.fab1) protected FloatingActionButton mFloatingActionButton1;
    @Bind(R.id.fab2) protected FloatingActionButton mFloatingActionButton2;

    private Boolean mIsFabOpen = false;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;
    private int mFabMargin;
    private int mToolbarHeight;
    private boolean mFadeToolbar = false;

    private SimpleRecyclerAdapter mSimpleRecyclerAdapter;
    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;
    private int mCurrentSelectedPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mUserRef = new Firebase(Constants.FIREBASE_URL_USERS).child(mEncodedEmail);

        initializeScreen(savedInstanceState);


    }

    private void initializeScreen(Bundle savedInstanceState) {
        // mFloatingActionButton section
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);
        mFloatingActionButton.setOnClickListener(this);
        mFloatingActionButton1.setOnClickListener(this);
        mFloatingActionButton2.setOnClickListener(this);

        setSupportActionBar(mToolbar);


        // nav drawer
        mUserLearnedDrawer = Boolean.valueOf(readSharedSetting(this, PREF_USER_LEARNED_DRAWER, "false"));

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        setUpNavDrawer();

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                // TODO add settings, reset password, people i love (hannah, all code i took), about the meditations
                menuItem.setChecked(true);
                switch (menuItem.getItemId()) {
                    case R.id.navigation_item_1:
                        Snackbar.make(mContentFrame, "Item One", Snackbar.LENGTH_SHORT).show();
                        mCurrentSelectedPosition = 0;
                        return true;
                    case R.id.navigation_item_2:
                        Snackbar.make(mContentFrame, "Item Two", Snackbar.LENGTH_SHORT).show();
                        mCurrentSelectedPosition = 1;
                        return true;
                    default:
                        return true;
                }
            }
        });


        // mFloatingActionButton and recycle section

        mFabMargin = getResources().getDimensionPixelSize(R.dimen.fab_margin);
        mToolbarHeight = Utils.getToolbarHeight(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);

        /* Set top padding= mToolbar height.
         So there is no overlap when the mToolbar hides.
         Avoid using 0 for the other parameters as it resets padding set via XML!*/
        recyclerView.setPadding(recyclerView.getPaddingLeft(), mToolbarHeight,
                recyclerView.getPaddingRight(), recyclerView.getPaddingBottom());

        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Adding list data thrice for a more comfortable scroll.
        List<String> listData = new ArrayList<String>();
        int ct = 0;
        for (int i = 0; i < VersionModel.data.length * 3; i++) {
            listData.add(VersionModel.data[ct]);
            ct++;
            if (ct == VersionModel.data.length) {
                ct = 0;
            }
        }

        if (mSimpleRecyclerAdapter == null) {
            mSimpleRecyclerAdapter = new SimpleRecyclerAdapter(listData);
            recyclerView.setAdapter(mSimpleRecyclerAdapter);
        }

        recyclerView.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void show() {
                mToolbarContainer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                if (mFadeToolbar)
                    mToolbarContainer.animate().alpha(1).setInterpolator(new DecelerateInterpolator(1)).start();
                    makeFabReappear();
            }

            @Override
            public void hide() {
                mToolbarContainer.animate().translationY(-mToolbarHeight).setInterpolator(new AccelerateInterpolator(2)).start();
                if (mFadeToolbar)
                    mToolbarContainer.animate().alpha(0).setInterpolator(new AccelerateInterpolator(1)).start();
                if (mIsFabOpen) {
                    mFloatingActionButton.startAnimation(rotate_backward);
                    mFloatingActionButton1.startAnimation(fab_close);
                    mFloatingActionButton2.startAnimation(fab_close);
                    mFloatingActionButton1.setClickable(false);
                    mFloatingActionButton2.setClickable(false);
                    mIsFabOpen = false;
                }
                makeFabDisappear();
            }
        });

        mUserRefListener = mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                /**
                 * Set the activity title to current user name if user is not null
                 */
                if (user != null) {
                    /* Assumes that the first word in the user's name is the user's first name. */
                    String firstName = user.getName().split("\\s+")[0];
                    String title = firstName + ".. Looking good!";
                    setTitle(title);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(LOG_TAG,
                        getString(R.string.log_error_the_read_failed) +
                                firebaseError.getMessage());
            }
        });
    }

    private void makeFabReappear() {
        mFloatingActionButton.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }

    private void makeFabDisappear() {
        mFloatingActionButton.animate().translationY(mFloatingActionButton.getHeight() + mFabMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab:
                animateFAB();
                break;
            case R.id.fab1:
                Intent intent = new Intent(MainActivity.this, FifteenMeditationActivity.class);
                startActivity(intent);
                break;
            case R.id.fab2:
                // insert new activity
                break;
        }
    }


    public void animateFAB() {
        if (mIsFabOpen) {
            mFloatingActionButton.startAnimation(rotate_backward);
            mFloatingActionButton1.startAnimation(fab_close);
            mFloatingActionButton2.startAnimation(fab_close);
            mFloatingActionButton1.setClickable(false);
            mFloatingActionButton2.setClickable(false);
            mIsFabOpen = false;
            Log.d("Raj", "close");
        } else {
            mFloatingActionButton.startAnimation(rotate_forward);
            mFloatingActionButton1.startAnimation(fab_open);
            mFloatingActionButton2.startAnimation(fab_open);
            mFloatingActionButton1.setClickable(true);
            mFloatingActionButton2.setClickable(true);
            mIsFabOpen = true;
            Log.d("Raj", "open");
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
                    makeFabDisappear();
                    //TODO make FAB reappear after nav closes
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
