package sauerapps.betterbetterrx.features.newsfeed;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import sauerapps.betterbetterrx.app.BaseActivity;
import sauerapps.betterbetterrx.model.User;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.features.journal.JournalActivity;
import sauerapps.betterbetterrx.features.meditation.audioSection.AudioActivity;
import sauerapps.betterbetterrx.utils.Constants;

public class MainActivity extends BaseActivity {

    //TODO change all images to SVG
    //TODO add "be the most important person for 15 - 30 min, and turn on Airplane Mode - calls, texts will interfere"

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private Firebase mUserRef;
    private ValueEventListener mUserRefListener;
    protected Boolean isFabOpen = false;
    protected FloatingActionButton mMeditationFab, mSingleMeditationFab, mJournalMeditationFab;
    protected Animation fab_open, fab_close, rotate_forward, rotate_backward;

    @Bind(R.id.toolbar_main_activity)
    protected Toolbar mToolbar;

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

        mUserRef = new Firebase(Constants.FIREBASE_URL_USERS).child(mEncodedEmail);

        initializeScreen();
    }

    private void initializeScreen() {
        setSupportActionBar(mToolbar);


        // fab section
        mMeditationFab = (FloatingActionButton) findViewById(R.id.meditation_fab);
        mSingleMeditationFab = (FloatingActionButton) findViewById(R.id.single_meditation_fab);
        mJournalMeditationFab = (FloatingActionButton) findViewById(R.id.journal_fab);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);

        mMeditationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAnimateFAB();
            }
        });

        mSingleMeditationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AudioActivity.class);
                startActivity(intent);
            }
        });

        mJournalMeditationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, JournalActivity.class);
                startActivity(intent);
            }
        });

        // getting first name for toolbar reference
        mUserRefListener = mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    /* Assumes that the first word in the user's name is the user's first name. */
                    String firstName = user.getName().split("\\s+")[0];
                    String title = firstName + ", welcome back.";
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUserRef.removeEventListener(mUserRefListener);
    }

    private void getAnimateFAB() {
            if (isFabOpen) {
                mMeditationFab.startAnimation(rotate_backward);
                mSingleMeditationFab.startAnimation(fab_close);
                mJournalMeditationFab.startAnimation(fab_close);
                mSingleMeditationFab.setClickable(false);
                mJournalMeditationFab.setClickable(false);
                isFabOpen = false;
            } else {
                mMeditationFab.startAnimation(rotate_forward);
                mSingleMeditationFab.startAnimation(fab_open);
                mJournalMeditationFab.startAnimation(fab_open);
                mSingleMeditationFab.setClickable(true);
                mJournalMeditationFab.setClickable(true);
                isFabOpen = true;
            }
    }
}
