package sauerapps.betterbetterrx.features.meditation.audioSection.audioListDetails;

import android.content.Intent;
import android.os.Bundle;

import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.app.BaseActivity;
import sauerapps.betterbetterrx.utils.Constants;

public class AudioActivity extends BaseActivity {

    String mUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        Intent intent = this.getIntent();
        mUserName = intent.getStringExtra(Constants.KEY_NAME);

        AudioListFragment audioListFragment = AudioListFragment.newInstance(mEncodedEmail,
                mUserName);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container_audio, audioListFragment)
                    .commit();
        }
    }
}