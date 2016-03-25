package sauerapps.betterbetterrx.features.journal.activeListDetails;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;

import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.features.journal.activeList.JournalList;
import sauerapps.betterbetterrx.model.User;
import sauerapps.betterbetterrx.utils.Constants;
import sauerapps.betterbetterrx.utils.Utils;

public class RemoveListDialogFragment extends DialogFragment {
    String mListId;
    String mListOwner;
    HashMap mSharedWith;

    final static String LOG_TAG = RemoveListDialogFragment.class.getSimpleName();

    public static RemoveListDialogFragment newInstance(JournalList journalList, String listId,
                                                       HashMap<String, User> sharedWithUsers) {
        RemoveListDialogFragment removeListDialogFragment = new RemoveListDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_LIST_ID, listId);
        bundle.putString(Constants.KEY_LIST_OWNER, journalList.getOwner());
        bundle.putSerializable(Constants.KEY_SHARED_WITH_USERS, sharedWithUsers);
        removeListDialogFragment.setArguments(bundle);
        return removeListDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListId = getArguments().getString(Constants.KEY_LIST_ID);
        mListOwner = getArguments().getString(Constants.KEY_LIST_OWNER);
        mSharedWith = (HashMap) getArguments().getSerializable(Constants.KEY_SHARED_WITH_USERS);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomTheme_Dialog)
                .setTitle(getActivity().getResources().getString(R.string.action_remove_list))
                .setMessage(getString(R.string.dialog_message_are_you_sure_remove_list))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        removeList();
                        /* Dismiss the dialog */
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        /* Dismiss the dialog */
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert);

        return builder.create();
    }

    private void removeList() {
        Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);

        HashMap<String, Object> removeListData = new HashMap<String, Object>();

        /* Remove the ShoppingLists from both user lists and active lists */
        Utils.updateMapForAllWithValue(mSharedWith, mListId, mListOwner, removeListData, "", null);

        /* Remove the associated list items */
        removeListData.put("/" + Constants.FIREBASE_LOCATION_JOURNAL_ENTRY_ITEMS + "/" + mListId,
                null);

        removeListData.put("/" + Constants.FIREBASE_LOCATION_OWNER_MAPPINGS + "/" + mListId,
                null);
        /* Do a deep-path update */
        firebaseRef.updateChildren(removeListData, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {

                if (firebaseError != null) {
                    Log.e(LOG_TAG, getString(R.string.log_error_updating_data) + firebaseError.getMessage());
                }
            }
        });
    }

}
