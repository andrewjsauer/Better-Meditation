package sauerapps.betterbetterrx.features.journal.activeList;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.utils.Constants;
import sauerapps.betterbetterrx.utils.Utils;

/**
 * Created by andrewsauer on 3/8/16.
 */
public class AddJournalEntryDialogFragment extends DialogFragment {
    String mEncodedEmail;
    EditText mEditTextListName;

    protected String mCurrentDate;

    public static AddJournalEntryDialogFragment newInstance(String encodedEmail) {
        AddJournalEntryDialogFragment addListDialogFragment = new AddJournalEntryDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_ENCODED_EMAIL, encodedEmail);
        addListDialogFragment.setArguments(bundle);
        return addListDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEncodedEmail = getArguments().getString(Constants.KEY_ENCODED_EMAIL);

        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy", Locale.ENGLISH);
        mCurrentDate = df.format(Calendar.getInstance().getTime());
    }

    /**
     * Open the keyboard automatically when the dialog fragment is opened
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomTheme_Dialog);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_add_list, null);
        mEditTextListName = (EditText) rootView.findViewById(R.id.edit_text_list_name);

        mEditTextListName.setText(mCurrentDate);

        mEditTextListName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    addJournalEntry();
                }
                return true;
            }
        });

        /* Inflate and set the layout for the dialog */
        /* Pass null as the parent view because its going in the dialog layout*/
        builder.setView(rootView)
                /* Add action buttons */
                .setPositiveButton(R.string.positive_button_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        addJournalEntry();
                    }
                });

        return builder.create();
    }

    private void addJournalEntry() {
        String userEnteredEntryName = mEditTextListName.getText().toString();

        if (!userEnteredEntryName.equals("")) {

            Firebase userListsRef = new Firebase(Constants.FIREBASE_URL_USER_LISTS).
                    child(mEncodedEmail);

            final Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);

            Firebase newListRef = userListsRef.push();

            /* Save listsRef.push() to maintain same random Id */
            final String listId = newListRef.getKey();

            /* HashMap for data to update */
            HashMap<String, Object> updateJournalList = new HashMap<>();

            /**
             * Set raw version of date to the ServerValue.TIMESTAMP value and save into
             * timestampCreatedMap
             */
            HashMap<String, Object> timestampCreated = new HashMap<>();
            timestampCreated.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

            /* Build the list */
            JournalList newJournalList = new JournalList(userEnteredEntryName, mEncodedEmail,
                    timestampCreated);

            HashMap<String, Object> journalListMap = (HashMap<String, Object>)
                    new ObjectMapper().convertValue(newJournalList, Map.class);

            Utils.updateMapForAllWithValue(null, listId, mEncodedEmail,
                    updateJournalList, "", journalListMap);

            updateJournalList.put("/" + Constants.FIREBASE_LOCATION_OWNER_MAPPINGS + "/" + listId,
                    mEncodedEmail);

            /* Do the update */
            firebaseRef.updateChildren(updateJournalList, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    /* Now that we have the timestamp, update the reversed timestamp */
                    Utils.updateTimestampReversed(firebaseError, "AddList", listId,
                            null, mEncodedEmail);
                }
            });

            /* Close the dialog fragment */
            AddJournalEntryDialogFragment.this.getDialog().cancel();
        }

    }

}
