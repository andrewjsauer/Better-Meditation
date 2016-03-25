//package sauerapps.betterbetterrx.features.meditation.audioSection;
//
//import android.util.Log;
//
//import com.firebase.client.DataSnapshot;
//import com.firebase.client.Firebase;
//import com.firebase.client.FirebaseError;
//import com.firebase.client.ValueEventListener;
//
//import java.util.HashMap;
//
//import sauerapps.betterbetterrx.features.journal.activeList.JournalList;
//import sauerapps.betterbetterrx.model.User;
//import sauerapps.betterbetterrx.utils.Constants;
//
///**
// * Created by andrewsauer on 3/24/16.
// */
//public class AudioListUtil {
//
//
//    public static void updateTimestampReversed(FirebaseError firebaseError, final String logTagFromActivity,
//                                               final HashMap<String, User> sharedWith,
//                                               final String owner) {
//        if (firebaseError != null) {
//            Log.d(logTagFromActivity, "Error updating timestamp: " + firebaseError.getMessage());
//        } else {
//            final Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);
//
//            firebaseRef.child(Constants.FIREBASE_LOCATION_USER_AUDIO_DETAILS).child(owner)
//                    .addListenerForSingleValueEvent(new ValueEventListener() {
//
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//
//                            AudioList audioList = dataSnapshot.getValue(AudioList.class);
//                            if (audioList != null) {
//                                long timeReverse = -(audioList.getTimestampLastChangedLong());
//                                String timeReverseLocation = Constants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED_REVERSE
//                                        + "/" + Constants.FIREBASE_PROPERTY_TIMESTAMP;
//
//                                /**
//                                 * Create map and fill it in with deep path multi write operations list
//                                 */
//                                HashMap<String, Object> updatedShoppingListData = new HashMap<String, Object>();
//
//                                updateMapForAllWithValue(sharedWith, listId, owner, updatedShoppingListData,
//                                        timeReverseLocation, timeReverse);
//                                firebaseRef.updateChildren(updatedShoppingListData);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(FirebaseError firebaseError) {
//                            Log.d(logTagFromActivity, "Error updating data: " + firebaseError.getMessage());
//                        }
//                    });
//        }
//    }
//}
