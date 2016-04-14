package sauerapps.betterbetterrx.features.journal.journalList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.firebase.client.Firebase;
import com.firebase.client.Query;

import butterknife.Bind;
import butterknife.ButterKnife;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.app.BaseActivity;
import sauerapps.betterbetterrx.features.journal.journalListDetails.JournalListDetailsActivity;
import sauerapps.betterbetterrx.utils.Constants;

public class JournalListFragment extends Fragment {

    @Bind(R.id.toolbar_journal)
    protected Toolbar mToolbar;
    private String mEncodedEmail;
    private JournalListAdapter mJournalListAdapter;
    private ListView mListView;

    public JournalListFragment() {
        /* Required empty public constructor */
    }

    public static JournalListFragment newInstance(String encodedEmail) {
        JournalListFragment fragment = new JournalListFragment();
        Bundle args = new Bundle();
        args.putString(Constants.KEY_ENCODED_EMAIL, encodedEmail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEncodedEmail = getArguments().getString(Constants.KEY_ENCODED_EMAIL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journal_list, container, false);
        ButterKnife.bind(this, view);

        initializeScreen(view);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JournalList selectedList = mJournalListAdapter.getItem(position);
                if (selectedList != null) {
                    Intent intent = new Intent(getActivity(), JournalListDetailsActivity.class);
                    String listId = mJournalListAdapter.getRef(position).getKey();
                    intent.putExtra(Constants.KEY_LIST_ID, listId);
                    startActivity(intent);
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String sortOrder = sharedPref.getString(Constants.KEY_PREF_SORT_ORDER_LISTS, Constants.ORDER_BY_KEY);

        Query orderedActiveUserListsRef;
        Firebase activeListsRef = new Firebase(Constants.FIREBASE_URL_USER_LISTS)
                .child(mEncodedEmail);

        if (sortOrder.equals(Constants.ORDER_BY_KEY)) {
            orderedActiveUserListsRef = activeListsRef.orderByKey();
        } else {
            orderedActiveUserListsRef = activeListsRef.orderByChild(sortOrder);
        }

        mJournalListAdapter = new JournalListAdapter(getActivity(), JournalList.class,
                R.layout.active_single_list, orderedActiveUserListsRef,
                mEncodedEmail);

        View emptyView = getActivity().getLayoutInflater().inflate(R.layout.list_empty_journal, null);

        getActivity().addContentView(emptyView, mListView.getLayoutParams());

        mListView.setEmptyView(emptyView);

        mListView.setAdapter(mJournalListAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        mJournalListAdapter.cleanup();
    }

    private void initializeScreen(View rootView) {
        mListView = (ListView) rootView.findViewById(R.id.list_view_active_lists);

        BaseActivity activity = (BaseActivity) getActivity();

        activity.setSupportActionBar(mToolbar);
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            activity.setTitle("Gratitude Journal");
        }
    }
}

