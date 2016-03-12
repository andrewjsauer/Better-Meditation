package sauerapps.betterbetterrx.features.meditation.audioSection;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import sauerapps.betterbetterrx.features.journal.activeList.JournalList;
import sauerapps.betterbetterrx.model.User;
import sauerapps.betterbetterrx.utils.Constants;

/**
 * Created by andrewsauer on 3/11/16.
 */
public class AudioUtils {

    /**
     * Adds values to a pre-existing HashMap for updating a property for all of the ShoppingList copies.
     * The HashMap can then be used with {@link Firebase#updateChildren(Map)} to update the property
     * for all ShoppingList copies.
     *
     * @param sharedWith            The list of users the shopping list that has been updated is shared with.
     * @param listId           The id of the shopping list.
     * @param owner            The owner of the shopping list.
     * @param mapToUpdate      The map containing the key, value pairs which will be used
     *                         to update the Firebase database. This MUST be a Hashmap of key
     *                         value pairs who's urls are absolute (i.e. from the root node)
     * @param propertyToUpdate The property to update
     * @param valueToUpdate    The value to update
     * @return The updated HashMap with the new value inserted in all lists
     */
    public static HashMap<String, Object> updateMapForAllWithValue
            (final HashMap<String, User> sharedWith, final String listId,
             final String owner, HashMap<String, Object> mapToUpdate,
             String propertyToUpdate, Object valueToUpdate) {

        mapToUpdate.put("/" + Constants.FIREBASE_LOCATION_USER_AUDIO + "/" + owner + "/"
                + listId + "/" + propertyToUpdate, valueToUpdate);
        if (sharedWith != null) {
            for (User user : sharedWith.values()) {
                mapToUpdate.put("/" + Constants.FIREBASE_LOCATION_USER_AUDIO + "/" + user.getEmail() + "/"
                        + listId + "/" + propertyToUpdate, valueToUpdate);
            }
        }

        return mapToUpdate;
    }

    /**
     * Once an update is made to a ShoppingList, this method is responsible for updating the
     * reversed timestamp to be equal to the negation of the current timestamp. This comes after
     * the updateMapWithTimestampChanged because ServerValue.TIMESTAMP must be resolved to a long
     * value.
     *
     * @param firebaseError      The Firebase error, if there was one, from the original update. This
     *                           method should only run if the shopping list's timestamp last changed
     *                           was successfully updated.
     * @param logTagFromActivity The log tag from the activity calling this method
     * @param listId             The updated shopping list push ID
     * @param sharedWith         The list of users that this updated shopping list is shared with
     * @param owner              The owner of the updated shopping list
     */
    public static void updateTimestampReversed(FirebaseError firebaseError, final String logTagFromActivity,
                                               final String listId, final HashMap<String, User> sharedWith,
                                               final String owner) {
        if (firebaseError != null) {
            Log.d(logTagFromActivity, "Error updating timestamp: " + firebaseError.getMessage());
        } else {
            final Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);
            firebaseRef.child(Constants.FIREBASE_LOCATION_USER_AUDIO).child(owner)
                    .child(listId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    AudioList list = dataSnapshot.getValue(AudioList.class);
                    if (list != null) {
                        long timeReverse = -(list.getTimestampLastChangedLong());
                        String timeReverseLocation = Constants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED_REVERSE
                                + "/" + Constants.FIREBASE_PROPERTY_TIMESTAMP;

                        /**
                         * Create map and fill it in with deep path multi write operations list
                         */
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
