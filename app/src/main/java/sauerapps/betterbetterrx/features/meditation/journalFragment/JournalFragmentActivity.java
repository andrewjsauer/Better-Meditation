package sauerapps.betterbetterrx.features.meditation.journalFragment;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import sauerapps.betterbetterrx.R;

public class JournalFragmentActivity extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_journal_fragment, container, false);

        return v;
    }

    public void initializeScreen() {

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
