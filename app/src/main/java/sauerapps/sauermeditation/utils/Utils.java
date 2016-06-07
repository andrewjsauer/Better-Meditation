package sauerapps.sauermeditation.utils;

import android.content.Context;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;

import sauerapps.sauermeditation.features.journal.journalList.JournalList;
import sauerapps.sauermeditation.app.User;

/**
 * Created by andrewsauer on 2/9/16.
 */
public class Utils {

    private Context mContext = null;

    public Utils(Context con) {
        mContext = con;
    }

    public static String encodeEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }

    public static String decodeEmail(String userEmail) {
        return userEmail.replace(",", ".");
    }

    public static HashMap<String, Object> updateMapForAllWithValue
    (final HashMap<String, User> sharedWith, final String listId,
     final String owner, HashMap<String, Object> mapToUpdate,
     String propertyToUpdate, Object valueToUpdate) {

        mapToUpdate.put("/" + Constants.FIREBASE_LOCATION_JOURNAL_ENTERIES + "/" + owner + "/"
                + listId + "/" + propertyToUpdate, valueToUpdate);
        if (sharedWith != null) {
            for (User user : sharedWith.values()) {
                mapToUpdate.put("/" + Constants.FIREBASE_LOCATION_JOURNAL_ENTERIES + "/" + user.getEmail() + "/"
                        + listId + "/" + propertyToUpdate, valueToUpdate);
            }
        }

        return mapToUpdate;
    }

    public static HashMap<String, Object> updateMapWithTimestampLastChanged
    (final HashMap<String, User> sharedWith, final String listId,
     final String owner, HashMap<String, Object> mapToAddDateToUpdate) {
        HashMap<String, Object> timestampNowHash = new HashMap<>();
        timestampNowHash.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

        updateMapForAllWithValue(sharedWith, listId, owner, mapToAddDateToUpdate,
                Constants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED, timestampNowHash);

        return mapToAddDateToUpdate;
    }

    public static void updateTimestampReversed(FirebaseError firebaseError, final String logTagFromActivity,
                                               final String listId, final HashMap<String, User> sharedWith,
                                               final String owner) {
        if (firebaseError != null) {
            Log.d(logTagFromActivity, "Error updating timestamp: " + firebaseError.getMessage());
        } else {
            final Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);
            firebaseRef.child(Constants.FIREBASE_LOCATION_JOURNAL_ENTERIES).child(owner)
                    .child(listId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    JournalList list = dataSnapshot.getValue(JournalList.class);
                    if (list != null) {
                        long timeReverse = -(list.getTimestampLastChangedLong());
                        String timeReverseLocation = Constants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED_REVERSE
                                + "/" + Constants.FIREBASE_PROPERTY_TIMESTAMP;

                        HashMap<String, Object> updatedShoppingListData = new HashMap<String, Object>();

                        updateMapForAllWithValue(sharedWith, listId, owner, updatedShoppingListData,
                                timeReverseLocation, timeReverse);
                        firebaseRef.updateChildren(updatedShoppingListData);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.d(logTagFromActivity, "Error updating data: " + firebaseError.getMessage());
                }
            });
        }
    }
}
