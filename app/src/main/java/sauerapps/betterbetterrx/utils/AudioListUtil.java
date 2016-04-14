package sauerapps.betterbetterrx.utils;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;

import sauerapps.betterbetterrx.features.meditation.audioSection.audioListDetails.AudioList;
import sauerapps.betterbetterrx.model.User;

/**
 * Created by andrewsauer on 3/24/16.
 */
public class AudioListUtil {

    public static HashMap<String, Object> updateMapForAllWithValue
            (final HashMap<String, User> sharedWith, final String ownerEmail,
             final String owner, HashMap<String, Object> mapToUpdate,

             String propertyToUpdate, Object valueToUpdate) {
        mapToUpdate.put("/" + Constants.FIREBASE_LOCATION_USER_AUDIO_DETAILS_LIST + "/" + owner + "/"
                + ownerEmail + "/" + propertyToUpdate, valueToUpdate);
        if (sharedWith != null) {
            for (User user : sharedWith.values()) {
                mapToUpdate.put("/" + Constants.FIREBASE_LOCATION_USER_AUDIO_DETAILS_LIST + "/" + user.getEmail() + "/"
                        + ownerEmail + "/" + propertyToUpdate, valueToUpdate);
            }
        }

        return mapToUpdate;
    }

    public static void updateTimestampReversed(FirebaseError firebaseError, final String logTagFromActivity,
                                               final String ownerEmail, final HashMap<String, User> sharedWith,
                                               final String owner) {
        if (firebaseError != null) {
            Log.d(logTagFromActivity, "Error updating timestamp: " + firebaseError.getMessage());
        } else {
            final Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);
            firebaseRef.child(Constants.FIREBASE_LOCATION_USER_AUDIO_DETAILS).child(owner)
                    .child(ownerEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    AudioList list = dataSnapshot.getValue(AudioList.class);
                    if (list != null) {
                        long timeReverse = -(list.getTimestampLastChangedLong());
                        String timeReverseLocation = Constants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED_REVERSE
                                + "/" + Constants.FIREBASE_PROPERTY_TIMESTAMP;

                        HashMap<String, Object> updateAudioListData = new HashMap<String, Object>();

                        updateMapForAllWithValue(sharedWith, ownerEmail, owner, updateAudioListData,
                                timeReverseLocation, timeReverse);
                        firebaseRef.updateChildren(updateAudioListData);
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
