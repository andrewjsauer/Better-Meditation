package sauerapps.betterbetterrx.features.main_newsfeed.menu;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.utils.Constants;

/**
 * Created by andrewsauer on 5/24/16.
 */
public class ChangePasswordDialog extends Dialog {

    private static final String TAG = ChangePasswordDialog.class.getSimpleName();

    private Firebase mSavePasswordRef;

    private Context mContext;

    @Bind(R.id.edit_text_email_change_password_dialog)
    protected EditText mCurrentEmailEditText;

    @Bind(R.id.edit_text_new_password_change_password_dialog)
    protected EditText mNewPasswordEditText;

    @Bind(R.id.edit_text_old_password_change_password_dialog)
    protected EditText mOldPasswordEditText;

    public ChangePasswordDialog(Context context) {
        super(context);

        mContext = context;

        setContentView(R.layout.dialog_change_password);

        ButterKnife.bind(this);

        mSavePasswordRef = new Firebase(Constants.FIREBASE_URL);

        setTitle("Change Password");
    }

    public void setUserEmail(String usersEmail) {
        if (!TextUtils.isEmpty(usersEmail)) {
            String modifyEmail = usersEmail.replaceAll(",",".");
            mCurrentEmailEditText.setText(modifyEmail);
        }
    }


    @OnClick(R.id.change_password_save_button)
    void saveChangedPassword() {

        String currentEmail = mCurrentEmailEditText.getText().toString();
        String newPassword = mNewPasswordEditText.getText().toString();
        String oldPassword = mOldPasswordEditText.getText().toString();

        if (TextUtils.isEmpty(currentEmail) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(oldPassword)) {
            Toast.makeText(mContext, "Something went wrong. Please make sure all fields are filled out.", Toast.LENGTH_SHORT).show();
            return;
        }

        mSavePasswordRef.changePassword(currentEmail,
                oldPassword,
                newPassword,
                new Firebase.ResultHandler() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(mContext, "Password changed", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {
                        Toast.makeText(mContext, "Something went wrong: " + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, firebaseError.getDetails());
                    }
                });
    }

    @OnClick(R.id.change_password_cancel_button)
    void cancelChangePassword() {
        dismiss();
    }
}