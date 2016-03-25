package sauerapps.betterbetterrx.features.journal.activeListDetails;

import android.app.Dialog;
import android.os.Bundle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;

import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.features.journal.activeList.JournalList;
import sauerapps.betterbetterrx.model.JournalListItem;
import sauerapps.betterbetterrx.model.User;
import sauerapps.betterbetterrx.utils.Constants;
import sauerapps.betterbetterrx.utils.Utils;

/**
 * Lets user add new list item.
 */
public class AddListItemDialogFragment extends EditListDialogFragment {

    public static AddListItemDialogFragment newInstance(JournalList journalList, String listId,
                                                        String encodedEmail,
                                                        HashMap<String, User> sharedWithUsers) {
        AddListItemDialogFragment addListItemDialogFragment = new AddListItemDialogFragment();
        Bundle bundle = EditListDialogFragment.newInstanceHelper(journalList,
                R.layout.dialog_add_item, listId, encodedEmail, sharedWithUsers);
        addListItemDialogFragment.setArguments(bundle);

        return addListItemDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.createDialogHelper(R.string.positive_button_add_list_item);
    }

    @Override
    protected void doListEdit() {
        String mItemName = mEditTextForList.getText().toString();

        if (!mItemName.equals("")) {

            Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);
            Firebase itemsRef = new Firebase(Constants.FIREBASE_URL_JOURNAL_LIST_ITEMS).child(mListId);

            /* Make a map for the item you are adding */
            HashMap<String, Object> updatedItemToAddMap = new HashMap<String, Object>();

            /* Save push() to maintain same random Id */
            Firebase newRef = itemsRef.push();
            String itemId = newRef.getKey();

            /* Make a POJO for the item and immediately turn it into a HashMap */
            JournalListItem itemToAddObject = new JournalListItem(mItemName, mEncodedEmail);
            HashMap<String, Object> itemToAdd =
                    (HashMap<String, Object>) new ObjectMapper().convertValue(itemToAddObject, Map.class);


            /* Add the item to the update map*/
            updatedItemToAddMap.put("/" + Constants.FIREBASE_LOCATION_JOURNAL_ENTRY_ITEMS + "/"
                    + mListId + "/" + itemId, itemToAdd);

            /* Update affected lists timestamps */
            Utils.updateMapWithTimestampLastChanged(mSharedWith,
                    mListId, mOwner, updatedItemToAddMap);

            /* Do the update */
            firebaseRef.updateChildren(updatedItemToAddMap, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    /* Now that we have the timestamp, update the reversed timestamp */
                    Utils.updateTimestampReversed(firebaseError, "AddListItem", mListId,
                            mSharedWith, mOwner);
                }
            });

            AddListItemDialogFragment.this.getDialog().cancel();
        }
    }
}
