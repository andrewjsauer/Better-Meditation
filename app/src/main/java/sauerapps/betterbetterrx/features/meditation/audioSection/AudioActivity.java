package sauerapps.betterbetterrx.features.meditation.audioSection;

import android.os.Bundle;

import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.app.BaseActivity;

public class AudioActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_audio);

        AudioListFragment audioListFragment = AudioListFragment.newInstance(mEncodedEmail);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container_audio, audioListFragment)
                    .commit();
        }
    }
}