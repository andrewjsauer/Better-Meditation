package sauerapps.betterbetterrx.features.journal.journalListDetails;

/**
 * Created by andrewsauer on 3/9/16.
 */

import android.app.Dialog;
import android.os.Bundle;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;

import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.features.journal.journalList.JournalList;
import sauerapps.betterbetterrx.model.User;
import sauerapps.betterbetterrx.utils.Constants;
import sauerapps.betterbetterrx.utils.Utils;

public class EditListItemNameDialogFragment extends EditListDialogFragment {
    String mItemName, mItemId;

    public static EditListItemNameDialogFragment newInstance(JournalList journalList, String itemName,
                                                             String itemId, String listId, String encodedEmail,
                                                             HashMap<String, User> sharedWithUsers) {
        EditListItemNameDialogFragment editListItemNameDialogFragment = new EditListItemNameDialogFragment();

        Bundle bundle = EditListDialogFragment.newInstanceHelper(journalList, R.layout.dialog_edit_item,
                listId, encodedEmail, sharedWithUsers);
        bundle.putString(Constants.KEY_LIST_ITEM_NAME, itemName);
        bundle.putString(Constants.KEY_LIST_ITEM_ID, itemId);
        editListItemNameDialogFragment.setArguments(bundle);

        return editListItemNameDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItemName = getArguments().getString(Constants.KEY_LIST_ITEM_NAME);
        mItemId = getArguments().getString(Constants.KEY_LIST_ITEM_ID);
    }


    @Override

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = super.createDialogHelper(R.string.positive_button_edit_item);

        super.helpSetDefaultValueEditText(mItemName);

        return dialog;
    }

    protected void doListEdit() {
        String nameInput = mEditTextForList.getText().toString();

        if (!nameInput.equals("") && !nameInput.equals(mItemName)) {
            Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);

            /* Make a map for the item we are changing the name of */
            HashMap<String, Object> updatedDataItemToEditMap = new HashMap<String, Object>();

            /* Add the new name to the update map*/
            updatedDataItemToEditMap.put("/" + Constants.FIREBASE_LOCATION_JOURNAL_ENTRY_ITEMS + "/"
                            + mListId + "/" + mItemId + "/" + Constants.FIREBASE_PROPERTY_ITEM_ENTRY,
                    nameInput);

            /* Update affected lists timestamps */
            Utils.updateMapWithTimestampLastChanged(mSharedWith, mListId, mOwner, updatedDataItemToEditMap);

            /* Do the update */
            firebaseRef.updateChildren(updatedDataItemToEditMap, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        /* Now that we have the timestamp, update the reversed timestamp */
                    Utils.updateTimestampReversed(firebaseError, "EditListItem", mListId,
                            mSharedWith, mOwner);
                }
            });
        }
    }
}