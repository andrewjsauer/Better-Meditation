package sauerapps.betterbetterrx.features.sharing.audioSharing;

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
import sauerapps.betterbetterrx.features.meditation.audioSection.AudioList;
import sauerapps.betterbetterrx.model.User;
import sauerapps.betterbetterrx.utils.Constants;

/**
 * Created by andrewsauer on 3/24/16.
 */
public class FriendAudioDetailsAdapter extends FirebaseListAdapter<User> {

    private AudioList mAudioList;
    private static final String LOG_TAG = FriendAudioDetailsAdapter.class.getSimpleName();
    private Firebase mFirebaseRef, mAudioDetailsListRef;
    private HashMap<String, User> mSharedUsersList;
    private HashMap <Firebase, ValueEventListener> mLocationListenerMap;
    private String mEncodedEmail;
    private ValueEventListener mAudioDetailsListListener;

    public FriendAudioDetailsAdapter(Activity activity, Class<User> modelClass, int modelLayout,
                         Query ref, String encodedEmail) {
        super(activity, modelClass, modelLayout, ref);

        this.mActivity = activity;
        mFirebaseRef = new Firebase(Constants.FIREBASE_URL);
        mLocationListenerMap = new HashMap<Firebase, ValueEventListener>();
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

                final AudioList sharedFriendAudioDetails = snapshot.getValue(AudioList.class);

                if (sharedFriendAudioDetails != null) {

                    buttonToggleShare.setImageResource(R.drawable.ic_shared_check);
                    buttonToggleShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            HashMap<String, Object> updatedUserData = updateFriendInSharedWith(false, friend);

                            mFirebaseRef.updateChildren(updatedUserData, new Firebase.CompletionListener() {
                                @Override
                                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                    Log.d(LOG_TAG, "green should show");

                                }
                            });
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
                                    Log.d(LOG_TAG, "add person should show");
                                }
                            });
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

    public void setSharedWithUsers(HashMap<String, User> sharedUsersList) {
        this.mSharedUsersList = sharedUsersList;
        this.notifyDataSetChanged();
    }

    private HashMap<String, Object> updateFriendInSharedWith(Boolean addFriend, User friendToAddOrRemove) {
        HashMap<String, Object> updatedUserData = new HashMap<String, Object>();

        /* The newSharedWith lists contains all users who need their last time changed updated */
//        HashMap<String, User> newSharedWith = new HashMap<String, User>(mSharedUsersList);

        if (addFriend) {

            mAudioDetailsListRef = new Firebase(Constants.FIREBASE_URL_USER_AUDIO_DETAILS_LIST).child(friendToAddOrRemove.getEmail());

            mAudioDetailsListRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    AudioList audioList = dataSnapshot.getValue(AudioList.class);

                    if (audioList != null) {
                        mAudioList = audioList;
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e(LOG_TAG,
                            mActivity.getString(R.string.log_error_the_read_failed) +
                                    firebaseError.getMessage());
                }
            });

            /* Make it a HashMap of the list and user */
            final HashMap<String, Object> audioDetailsListFirebase = (HashMap<String, Object>)
                    new ObjectMapper().convertValue(mAudioList, Map.class);

            /* Add the friend to the shared list */
            updatedUserData.put("/" + Constants.FIREBASE_LOCATION_USER_AUDIO_DETAILS_SHARED_WITH + "/" + mEncodedEmail +
                    "/" + friendToAddOrRemove.getEmail(), audioDetailsListFirebase);

        } else {
            /* Remove the friend from the shared list */
            updatedUserData.put("/" + Constants.FIREBASE_LOCATION_USER_AUDIO_DETAILS_SHARED_WITH + "/" + mEncodedEmail +
                    "/" + friendToAddOrRemove.getEmail(), null);


//            newSharedWith.remove(friendToAddOrRemove.getEmail());

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
