package sauerapps.betterbetterrx.features.newsfeed;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.utils.Constants;

/**
 * Created by andrewsauer on 3/12/16.
 */
public class SummaryUserFragment extends Fragment {


    @Bind(R.id.summary_total_sessions_text_view)
    protected TextView mTotalSessions;
    @Bind(R.id.summary_total_time_text_view)
    protected TextView mTotalTime;

    private String mEncodedEmail;


    public SummaryUserFragment() {
        /* Required empty public constructor */
    }

    public static SummaryUserFragment newInstance(String encodedEmail) {
        SummaryUserFragment fragment = new SummaryUserFragment();
        Bundle args = new Bundle();
        args.putString(Constants.KEY_ENCODED_EMAIL, encodedEmail);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEncodedEmail = getArguments().getString(Constants.KEY_ENCODED_EMAIL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_summary, container, false);
        ButterKnife.bind(this, view);

        initializeScreen(view);


        return view;
    }

    private void initializeScreen(View view) {
        // test
    }

}
