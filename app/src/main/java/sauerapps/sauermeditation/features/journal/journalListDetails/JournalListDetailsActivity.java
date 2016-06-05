package sauerapps.sauermeditation.features.journal.journalListDetails;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;

import sauerapps.sauermeditation.R;
import sauerapps.sauermeditation.app.BaseActivity;
import sauerapps.sauermeditation.features.journal.journalList.JournalList;
import sauerapps.sauermeditation.features.journal.JournalListItem;
import sauerapps.sauermeditation.app.User;
import sauerapps.sauermeditation.utils.Constants;


public class JournalListDetailsActivity extends BaseActivity {

    private static final String LOG_TAG = JournalListDetailsActivity.class.getSimpleName();
    public String mTotalEntries;
    private Firebase mCurrentListRef, mSharedWithRef, mTotalEntriesRef;
    private JournalListItemAdapter mJournalListItemAdapter;
    private ListView mListView;
    private String mListId;
    private JournalList mJournalList;
    private ValueEventListener mCurrentListRefListener, mSharedWithListener, mTotalEntriesListener;
    private HashMap<String, User> mSharedWithUsers;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list_detail);

        /* Get the push ID from the extra passed by JournalListFragment */
        Intent intent = this.getIntent();
        mListId = intent.getStringExtra(Constants.KEY_LIST_ID);
        if (mListId == null) {
            /* No point in continuing without a valid ID. */
            finish();
            return;
        }

        mCurrentListRef = new Firebase(Constants.FIREBASE_URL_USER_LISTS).child(mEncodedEmail).child(mListId);
        mSharedWithRef = new Firebase (Constants.FIREBASE_URL_LISTS_SHARED_WITH).child(mListId);
        mTotalEntriesRef = new Firebase(Constants.FIREBASE_URL_JOURNAL_LIST_ITEMS).child(mListId);
        Firebase listItemsRef = new Firebase(Constants.FIREBASE_URL_JOURNAL_LIST_ITEMS).child(mListId);

        initializeScreen();

        mJournalListItemAdapter = new JournalListItemAdapter(this, JournalListItem.class,
                R.layout.active_single_list_item, listItemsRef.orderByChild(Constants.FIREBASE_PROPERTY_BOUGHT_BY),
                mListId, mEncodedEmail);

        /* Create JournalListItemAdapter and set to listView */
        mListView.setAdapter(mJournalListItemAdapter);

        mTotalEntriesListener = mTotalEntriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                long totalEntries = dataSnapshot.getChildrenCount();

                mTotalEntries = Long.toString(totalEntries);

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(LOG_TAG,
                        getString(R.string.log_error_the_read_failed) +
                                firebaseError.getMessage());
            }
        });

        mCurrentListRefListener = mCurrentListRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                JournalList journalList = snapshot.getValue(JournalList.class);

                if (journalList == null) {
                    finish();
                    return;
                }
                mJournalList = journalList;

                mJournalListItemAdapter.setJournalList(mJournalList);

                /* Calling invalidateOptionsMenu causes onCreateOptionsMenu to be called */
                invalidateOptionsMenu();

                /* Set title appropriately. */
                setTitle(journalList.getEntryTitle());

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(LOG_TAG,
                        getString(R.string.log_error_the_read_failed) +
                                firebaseError.getMessage());
            }
        });

        mSharedWithListener = mSharedWithRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mSharedWithUsers = new HashMap<String, User>();
                for (DataSnapshot currentUser : dataSnapshot.getChildren()) {
                    mSharedWithUsers.put(currentUser.getKey(), currentUser.getValue(User.class));
                }
                mJournalListItemAdapter.setSharedWithUsers(mSharedWithUsers);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(LOG_TAG,
                        getString(R.string.log_error_the_read_failed) +
                                firebaseError.getMessage());
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view.getId() != R.id.list_view_footer_empty) {
                    JournalListItem journalListItem = mJournalListItemAdapter.getItem(position);

                    String itemName = journalListItem.getItemName();
                    String itemId = mJournalListItemAdapter.getRef(position).getKey();
                    showEditListItemNameDialog(itemName, itemId);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_list_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_remove_list) {
            removeList();
            return true;
        }

//        if (id == R.id.action_archive) {
//            archiveList();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mJournalListItemAdapter.cleanup();
        mCurrentListRef.removeEventListener(mCurrentListRefListener);
        mSharedWithRef.removeEventListener(mSharedWithListener);
        mTotalEntriesRef.removeEventListener(mTotalEntriesListener);
    }

    private void initializeScreen() {
        mListView = (ListView) findViewById(R.id.list_view_journal_entries);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);

        /* Common toolbar setup */
        setSupportActionBar(toolbar);
        /* Add back button to the action bar */

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        /* Inflate the footer, set root layout to null*/
        View footer = getLayoutInflater().inflate(R.layout.footer_empty, null);

        View emptyView = getLayoutInflater().inflate(R.layout.list_empty_journal_enteries, null);

        addContentView(emptyView, mListView.getLayoutParams());

        mListView.setEmptyView(emptyView);

        mListView.addFooterView(footer);
    }

    /**
     * Archive current list when user selects "Archive" menu item
     */
//    public void archiveList() {
//    }
//

    public void removeList() {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = RemoveListDialogFragment.newInstance(mJournalList, mListId,
                mSharedWithUsers);
        dialog.show(getFragmentManager(), "RemoveListDialogFragment");
    }

    public void showAddListItemDialog(View view) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = AddListItemDialogFragment.newInstance(mJournalList, mListId,
                mEncodedEmail, mSharedWithUsers);
        dialog.show(getFragmentManager(), "AddListItemDialogFragment");
    }

    public void showEditListItemNameDialog(String itemName, String itemId) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = EditListItemNameDialogFragment.newInstance(mJournalList, itemName,
                itemId, mListId, mEncodedEmail, mSharedWithUsers);

        dialog.show(this.getFragmentManager(), "EditListItemNameDialogFragment");
    }
}
