package sauerapps.betterbetterrx.features.journal.journalListDetails;

/**
 * Created by andrewsauer on 3/9/16.
 */

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseListAdapter;

import java.util.HashMap;

import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.features.journal.JournalListItem;
import sauerapps.betterbetterrx.app.User;
import sauerapps.betterbetterrx.features.journal.journalList.JournalList;
import sauerapps.betterbetterrx.utils.Constants;
import sauerapps.betterbetterrx.utils.Utils;

public class JournalListItemAdapter extends FirebaseListAdapter<JournalListItem> {
    private JournalList mJournalList;
    private String mListId;
    private String mEncodedEmail;
    private HashMap<String, User> mSharedWithUsers;

    public JournalListItemAdapter(Activity activity, Class<JournalListItem> modelClass, int modelLayout,
                                  Query ref, String listId, String encodedEmail) {
        super(activity, modelClass, modelLayout, ref);
        this.mActivity = activity;
        this.mListId = listId;
        this.mEncodedEmail = encodedEmail;
    }

    public void setJournalList(JournalList journalList) {
        this.mJournalList = journalList;
        this.notifyDataSetChanged();
    }

    public void setSharedWithUsers(HashMap<String, User> sharedWithUsers) {
        this.mSharedWithUsers = sharedWithUsers;
        this.notifyDataSetChanged();
    }

    @Override
    protected void populateView(View view, final JournalListItem item, int position) {

        ImageButton buttonRemoveItem = (ImageButton) view.findViewById(R.id.button_remove_item);
        TextView textViewItemName = (TextView) view.findViewById(R.id.text_view_active_list_item_name);

        textViewItemName.setText(item.getItemName());

        /* Gets the id of the item to remove */
        final String itemToRemoveId = this.getRef(position).getKey();

        buttonRemoveItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity, R.style.CustomTheme_Dialog)
                        .setTitle(mActivity.getString(R.string.remove_item_option))
                        .setMessage(mActivity.getString(R.string.dialog_message_are_you_sure_remove_item))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                removeItem(itemToRemoveId);
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

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
    }

    private void removeItem(String itemId) {
        Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);

        /* Make a map for the removal */
        HashMap<String, Object> updatedRemoveItemMap = new HashMap<String, Object>();

        /* Remove the item by passing null */
        updatedRemoveItemMap.put("/" + Constants.FIREBASE_LOCATION_JOURNAL_ENTRY_ITEMS + "/"
                + mListId + "/" + itemId, null);

        /* Add the updated timestamp */
        Utils.updateMapWithTimestampLastChanged(mSharedWithUsers,
                mListId, mJournalList.getOwner(), updatedRemoveItemMap);

        /* Do the update */
        firebaseRef.updateChildren(updatedRemoveItemMap, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                Utils.updateTimestampReversed(firebaseError, "ActListItemAdap", mListId,
                        mSharedWithUsers, mJournalList.getOwner());
            }
        });
    }
}
