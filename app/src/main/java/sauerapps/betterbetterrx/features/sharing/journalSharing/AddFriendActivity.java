package sauerapps.betterbetterrx.features.sharing.journalSharing;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;

import com.firebase.client.Firebase;

import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.app.BaseActivity;
import sauerapps.betterbetterrx.model.User;
import sauerapps.betterbetterrx.utils.Constants;

/**
 * Created by andrewsauer on 3/9/16.
 */
public class AddFriendActivity extends BaseActivity {

    private EditText mEditTextAddFriendEmail;
    private AutocompleteFriendAdapter mFriendsAutocompleteAdapter;
    private String mInput;
    private ListView mListViewAutocomplete;
    private Firebase mUsersRef;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        mUsersRef = new Firebase(Constants.FIREBASE_URL_USERS);

        initializeScreen();

        mEditTextAddFriendEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                /* Get the input after every textChanged event and transform it to lowercase */
                mInput = mEditTextAddFriendEmail.getText().toString().toLowerCase();

            /* Clean up the old adapter */
                if (mFriendsAutocompleteAdapter != null) mFriendsAutocompleteAdapter.cleanup();
            /* Nullify the adapter data if the input length is less than 2 characters */
                if (mInput.equals("") || mInput.length() < 2) {
                    mListViewAutocomplete.setAdapter(null);

            /* Define and set the adapter otherwise. */
                } else {
                    mFriendsAutocompleteAdapter = new AutocompleteFriendAdapter(AddFriendActivity.this, User.class,
                            R.layout.single_autocomplete_item, mUsersRef.orderByChild(Constants.FIREBASE_PROPERTY_EMAIL)
                            .startAt(mInput).endAt(mInput + "~").limitToFirst(5), mEncodedEmail);

                    mListViewAutocomplete.setAdapter(mFriendsAutocompleteAdapter);
                }

            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFriendsAutocompleteAdapter != null) {
            mFriendsAutocompleteAdapter.cleanup();
        }
    }

    public void initializeScreen() {
        mListViewAutocomplete = (ListView) findViewById(R.id.list_view_friends_autocomplete);
        mEditTextAddFriendEmail = (EditText) findViewById(R.id.edit_text_add_friend_email);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_add_friend);
        setSupportActionBar(toolbar);
        /* Add back button to the action bar */
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
