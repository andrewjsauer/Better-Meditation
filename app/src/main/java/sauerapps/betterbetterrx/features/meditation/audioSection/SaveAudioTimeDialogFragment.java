package sauerapps.betterbetterrx.features.meditation.audioSection;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.features.journal.activeList.JournalList;
import sauerapps.betterbetterrx.utils.Constants;
import sauerapps.betterbetterrx.utils.Utils;

/**
 * Created by andrewsauer on 3/10/16.
 */
public class SaveAudioTimeDialogFragment extends DialogFragment {

    private static final String ARG_ENCODED_EMAIL = "argEncodedEmail";
    private static final String ARG_CURRENT_AUDIO = "argCurrentAudio";


    protected String mEncodedEmail;
    protected double mCurrentTime;

    protected String mTime;

    public static SaveAudioTimeDialogFragment newInstance(String encodedEmail, double currentAudioPosition) {
        SaveAudioTimeDialogFragment saveAudioTimeDialogFragment = new SaveAudioTimeDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_ENCODED_EMAIL, encodedEmail);
        bundle.putDouble(ARG_CURRENT_AUDIO, currentAudioPosition);
        saveAudioTimeDialogFragment.setArguments(bundle);
        return saveAudioTimeDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEncodedEmail = getArguments().getString(ARG_ENCODED_EMAIL);
        mCurrentTime = getArguments().getDouble(ARG_CURRENT_AUDIO);

        double time = mCurrentTime;


        mTime = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes((long) time),
                TimeUnit.MILLISECONDS.toSeconds((long) time));

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomTheme_Dialog)
                .setTitle(getString(R.string.audio_save_time_title))
                .setMessage("Would you like to save: " + mTime)
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

        Firebase userAudioTime = new Firebase(Constants.FIREBASE_URL_USER_AUDIO).
                child(mEncodedEmail);

        final Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);

        Firebase newListRef = userAudioTime.push();

        /* Save listsRef.push() to maintain same random Id */
        final String listId = newListRef.getKey();

        /* HashMap for data to update */
        HashMap<String, Object> updateAudioTime = new HashMap<>();

        /**
         * Set raw version of date to the ServerValue.TIMESTAMP value and save into
         * timestampCreatedMap
         */
        HashMap<String, Object> timestampCreated = new HashMap<>();
        timestampCreated.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

        /* Build the list */
        AudioList newAudioList = new AudioList(mCurrentTime, mEncodedEmail,
                timestampCreated);

        HashMap<String, Object> audioListMap = (HashMap<String, Object>)
                new ObjectMapper().convertValue(newAudioList, Map.class);

        AudioUtils.updateMapForAllWithValue(null, listId, mEncodedEmail,
                updateAudioTime, "", audioListMap);

        updateAudioTime.put("/" + Constants.FIREBASE_LOCATION_OWNER_MAPPINGS + "/" + listId,
                mEncodedEmail);

            /* Do the update */
        firebaseRef.updateChildren(updateAudioTime, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    /* Now that we have the timestamp, update the reversed timestamp */
                AudioUtils.updateTimestampReversed(firebaseError, "AddAudioTime", listId,
                        null, mEncodedEmail);
            }
        });

            /* Close the dialog fragment */
        SaveAudioTimeDialogFragment.this.getDialog().cancel();

    }


}
