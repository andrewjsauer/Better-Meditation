package sauerapps.betterbetterrx.features.newsfeed;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import sauerapps.betterbetterrx.app.BaseActivity;
import sauerapps.betterbetterrx.features.authentication.User;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.features.meditation.audioSection.AudioActivity;
import sauerapps.betterbetterrx.utils.Constants;

public class MainActivity extends BaseActivity {

    //TODO change all images to SVG
    //TODO add "be the most important person for 15 - 30 min, and turn on Airplane Mode - calls, texts will interfere"


    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private Firebase mUserRef;
    private ValueEventListener mUserRefListener;

    private SimpleRecyclerAdapter mSimpleRecyclerAdapter;

    @Bind(R.id.toolbar)
    protected Toolbar mToolbar;
    @Bind(R.id.single_meditation)
    protected ImageButton mSingleMeditation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mUserRef = new Firebase(Constants.FIREBASE_URL_USERS).child(mEncodedEmail);

        initializeScreen();
    }

    private void initializeScreen() {

        setSupportActionBar(mToolbar);

        mSingleMeditation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AudioActivity.class);
                startActivity(intent);
            }
        });


        // example recycler view
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
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
}
