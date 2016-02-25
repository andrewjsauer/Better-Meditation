package sauerapps.betterbetterrx.features.gratitudes;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.app.BaseActivity;
import sauerapps.betterbetterrx.database.User;
import sauerapps.betterbetterrx.utils.Constants;

public class GratitudeActivity extends BaseActivity {

    private static final String LOG_TAG = GratitudeActivity.class.getSimpleName();

    @Bind(R.id.toolbar)
    protected Toolbar mToolbar;


    @Bind(R.id.firstGratefulLayout)
    protected TextInputLayout mFirstGratefulLayout;
    @Bind(R.id.secondGratefulLayout)
    protected TextInputLayout mSecondGratefulLayout;
    @Bind(R.id.thirdGratefulLayout)
    protected TextInputLayout mThirdGratefulLayout;
    @Bind(R.id.fourthGratefulLayout)
    protected TextInputLayout mFourthGratefulLayout;
    @Bind(R.id.fifthGratefulLayout)
    protected TextInputLayout mFifthGratefulLayout;
    @Bind(R.id.firstGratefulText)
    protected EditText mFirstGratefulText;
    @Bind(R.id.secondGratefulText)
    protected EditText mSecondGratefulText;
    @Bind(R.id.thirdGratefulText)
    protected EditText mThirdGratefulText;
    @Bind(R.id.fourthGratefulText)
    protected EditText mFourthGratefulText;
    @Bind(R.id.fifthGratefulText)
    protected EditText mFifthGratefulText;

    private ValueEventListener mUserRefListener;
    private Firebase mUserRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gratitude);
        ButterKnife.bind(this);

        //initialize views
        initializeScreen();

    }


    public void initializeScreen() {

        mToolbar.setTitle("Gratitude Journal");
        setSupportActionBar(mToolbar);

        // example of setting Error messages for later
//        phoneLayout.setErrorEnabled(true);
//        phoneLayout.setError("Please enter a phone number");
//        EditText phone = (EditText) findViewById(R.id.phone);
//        phone.setError("Required");
//

        //note: use this.getAssets if calling from an Activity
//        Typeface robotoLight = Typeface.createFromAsset(this.getAssets(),
//                "font/Roboto-Light.ttf");



    }
}
