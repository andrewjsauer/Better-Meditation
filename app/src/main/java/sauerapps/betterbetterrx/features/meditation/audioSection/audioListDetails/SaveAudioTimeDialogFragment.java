package sauerapps.betterbetterrx.features.meditation.audioSection.audioListDetails;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.model.User;
import sauerapps.betterbetterrx.utils.AudioListUtil;
import sauerapps.betterbetterrx.utils.Constants;

public class SaveAudioTimeDialogFragment extends DialogFragment {

    private static final String TAG = SaveAudioTimeDialogFragment.class.getSimpleName();
    private static final String ARG_CURRENT_AUDIO = "argCurrentAudio";
    private static final String ARG_TRACK_TITLE = "argTrackTitle";
    private static final String ARG_TRACK_DESCRIPTION = "argTrackDescription";
    protected String mEncodedEmail;
    protected double mCurrentTime;
    protected String mTrackTitle;
    protected String mTrackDescription;
    protected String mUsersName;
    protected String mTime;
    protected HashMap mSharedWith;
    private Firebase userAudioRef, userAudioDetailsRef, userAudioDetailsListRef;

    public static SaveAudioTimeDialogFragment newInstance(String encodedEmail, String usersName, double currentAudioPosition,
                                                          String trackDescription, String trackTitle, HashMap<String, User> sharedWith) {
        SaveAudioTimeDialogFragment saveAudioTimeDialogFragment = new SaveAudioTimeDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_ENCODED_EMAIL, encodedEmail);
        bundle.putString(Constants.KEY_NAME, usersName);
        bundle.putDouble(ARG_CURRENT_AUDIO, currentAudioPosition);
        bundle.putString(ARG_TRACK_DESCRIPTION, trackDescription);
        bundle.putString(ARG_TRACK_TITLE, trackTitle);
        bundle.putSerializable(Constants.KEY_SHARED_WITH_USERS, sharedWith);
        saveAudioTimeDialogFragment.setArguments(bundle);
        return saveAudioTimeDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEncodedEmail = getArguments().getString(Constants.KEY_ENCODED_EMAIL);
        mUsersName = getArguments().getString(Constants.KEY_NAME);
        mCurrentTime = getArguments().getDouble(ARG_CURRENT_AUDIO);
        mTrackDescription = getArguments().getString(ARG_TRACK_DESCRIPTION);
        mTrackTitle = getArguments().getString(ARG_TRACK_TITLE);
        mSharedWith = (HashMap) getArguments().getSerializable(Constants.KEY_SHARED_WITH_USERS);

        userAudioRef = new Firebase(Constants.FIREBASE_URL_USER_AUDIO).child(mEncodedEmail);
        userAudioDetailsRef = new Firebase(Constants.FIREBASE_URL_USER_AUDIO_DETAILS).child(mEncodedEmail);
        userAudioDetailsListRef = new Firebase(Constants.FIREBASE_URL_USER_AUDIO_DETAILS_LIST)
                .child(mEncodedEmail).child(mEncodedEmail);

        double time = mCurrentTime;

        mTime = String.format(Locale.ENGLISH, "%01d hr %02d min",
                TimeUnit.MILLISECONDS.toHours((long) time),
                TimeUnit.MILLISECONDS.toMinutes((long) time)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours((long) time)));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomTheme_Dialog)
                .setTitle(getString(R.string.audio_save_time_title))
                .setMessage("Save: " + mTime)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        getSaveSession();
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
                .setIcon(android.R.drawable.ic_menu_save);

        return builder.create();
    }

    private void getSaveSession() {

        userAudioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    double lastAudioTime = (double) dataSnapshot.getValue();

                    double newTime = lastAudioTime + mCurrentTime;
                    userAudioRef.setValue(newTime);

                } else {
                    userAudioRef.setValue(mCurrentTime);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG,
                        getString(R.string.log_error_the_read_failed) +
                                firebaseError.getMessage());
            }
        });

        final String ownerEmail = mEncodedEmail;

        final Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);

        HashMap<String, Object> updateAudioListData = new HashMap<>();

        HashMap<String, Object> timestampCreated = new HashMap<>();
        timestampCreated.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

        AudioList audioList = new AudioList(mEncodedEmail, mUsersName, mCurrentTime, mTrackTitle,
                mTrackDescription, timestampCreated);

        HashMap<String, Object> audioListMap = (HashMap<String, Object>)
                new ObjectMapper().convertValue(audioList, Map.class);

        AudioListUtil.updateMapForAllWithValue(mSharedWith, ownerEmail, mEncodedEmail,
                updateAudioListData, "", audioListMap);

        updateAudioListData.put("/" + Constants.FIREBASE_LOCATION_OWNER_MAPPINGS + "/" + ownerEmail,
                mEncodedEmail);

         /* Do the update */
        firebaseRef.updateChildren(updateAudioListData, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                AudioListUtil.updateTimestampReversed(firebaseError, "AddList", ownerEmail,
                        null, mEncodedEmail);
            }
        });

        userAudioDetailsRef.push().setValue(audioList);

        SaveAudioTimeDialogFragment.this.getDialog().cancel();
    }
}