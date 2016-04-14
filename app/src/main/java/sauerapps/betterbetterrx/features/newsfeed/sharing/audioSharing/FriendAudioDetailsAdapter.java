package sauerapps.betterbetterrx.features.newsfeed.sharing.audioSharing;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;

import java.util.HashMap;
import java.util.Map;

import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.features.meditation.audioSection.audioListDetails.AudioList;
import sauerapps.betterbetterrx.model.User;
import sauerapps.betterbetterrx.utils.AudioListUtil;
import sauerapps.betterbetterrx.utils.Constants;

/**
 * Created by andrewsauer on 3/24/16.
 */
public class FriendAudioDetailsAdapter extends FirebaseListAdapter<User> {

    private static final String LOG_TAG = FriendAudioDetailsAdapter.class.getSimpleName();
    private AudioList mAudioList;
    private Firebase mFirebaseRef;
    private HashMap<String, User> mSharedWith;
    private HashMap <Firebase, ValueEventListener> mLocationListenerMap;
    private String mEncodedEmail;

    public FriendAudioDetailsAdapter(Activity activity, Class<User> modelClass, int modelLayout,
                         Query ref, String encodedEmail) {
        super(activity, modelClass, modelLayout, ref);

        this.mActivity = activity;
        mFirebaseRef = new Firebase(Constants.FIREBASE_URL);
        mLocationListenerMap = new HashMap<>();
        this.mEncodedEmail = encodedEmail;

    }

    @Override
    protected void populateView(View view, final User friend) {

        ((TextView) view.findViewById(R.id.user_name)).setText(friend.getName());

        final ImageButton buttonToggleShare = (ImageButton) view.findViewById(R.id.button_toggle_share);

        final Firebase sharedFriendAudioDetailsRef = new Firebase(Constants.FIREBASE_URL_AUDIO_DETAILS_SHARED_WITH)
                .child(mEncodedEmail)
                .child(friend.getEmail());


        ValueEventListener listener = sharedFriendAudioDetailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                final User sharedFriendAudioDetails = snapshot.getValue(User.class);

                if (sharedFriendAudioDetails != null) {

                    buttonToggleShare.setImageResource(R.drawable.ic_shared_check);
                    buttonToggleShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            HashMap<String, Object> updatedUserData = updateFriendInSharedWith(false, friend);

                            mFirebaseRef.updateChildren(updatedUserData, new Firebase.CompletionListener() {
                                @Override
                                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                    AudioListUtil.updateTimestampReversed(firebaseError, LOG_TAG, mEncodedEmail,
                                            mSharedWith, mAudioList.getOwner());

                                }
                            });
                            Log.d(LOG_TAG, "Green thing");
                        }
                    });
                } else {

                    buttonToggleShare.setImageResource(R.drawable.ic_person_add_24dp);
                    buttonToggleShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            HashMap<String, Object> updatedUserData = updateFriendInSharedWith(true, friend);

                            mFirebaseRef.updateChildren(updatedUserData, new Firebase.CompletionListener() {
                                @Override
                                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                    AudioListUtil.updateTimestampReversed(firebaseError, LOG_TAG, mEncodedEmail,
                                            mSharedWith, mAudioList.getOwner());
                                }
                            });

                            Log.d(LOG_TAG, "No green thing");

                        }
                    });
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(LOG_TAG,
                        mActivity.getString(R.string.log_error_the_read_failed) +
                                firebaseError.getMessage());
            }
        });

        /* Add the listener to the HashMap so that it can be removed on cleanup */
        mLocationListenerMap.put(sharedFriendAudioDetailsRef, listener);

    }

    public void setSharedWithUsers(HashMap<String, User> sharedWith) {
        this.mSharedWith = sharedWith;
        this.notifyDataSetChanged();
    }

    public void setAudioList(AudioList audioList) {
        this.mAudioList = audioList;
        this.notifyDataSetChanged();
    }

    private HashMap<String, Object> updateFriendInSharedWith(Boolean addFriend, User friendToAddOrRemove) {

        HashMap<String, Object> updatedUserData = new HashMap<String, Object>();

        HashMap<String, User> newSharedWith = new HashMap<String, User>(mSharedWith);

        if (addFriend) {
            Log.d(LOG_TAG, "addFriend");

            mAudioList.setTimestampLastChangedToNow();
            /* Make it a HashMap of the shopping list and user */
            final HashMap<String, Object> audioListFireBase = (HashMap<String, Object>)
                    new ObjectMapper().convertValue(mAudioList, Map.class);

            final HashMap<String, Object> friendForFirebase = (HashMap<String, Object>)
                    new ObjectMapper().convertValue(friendToAddOrRemove, Map.class);

            /* Add the friend to the shared list */
            updatedUserData.put("/" + Constants.FIREBASE_LOCATION_USER_AUDIO_DETAILS_SHARED_WITH + "/" + mEncodedEmail +
                    "/" + friendToAddOrRemove.getEmail(), friendForFirebase);

            /* Add that shopping list hashmap to the new user's active lists */
            updatedUserData.put("/" + Constants.FIREBASE_LOCATION_USER_AUDIO_DETAILS_LIST + "/" + friendToAddOrRemove.getEmail()
                    + "/" + mEncodedEmail, audioListFireBase);

        } else {

            /* Remove the friend from the shared list */
            updatedUserData.put("/" + Constants.FIREBASE_LOCATION_USER_AUDIO_DETAILS_SHARED_WITH + "/" + mEncodedEmail +
                    "/" + friendToAddOrRemove.getEmail(), null);

            /* Remove the list from the shared friend */
            updatedUserData.put("/" + Constants.FIREBASE_LOCATION_USER_AUDIO_DETAILS_LIST + "/" + friendToAddOrRemove.getEmail()
                    + "/" + mEncodedEmail, null);

            newSharedWith.remove(friendToAddOrRemove.getEmail());

        }

        return updatedUserData;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        /* Clean up the event listeners */
        for (HashMap.Entry<Firebase, ValueEventListener> listenerToClean : mLocationListenerMap.entrySet())
        {
            listenerToClean.getKey().removeEventListener(listenerToClean.getValue());
        }
    }
}
