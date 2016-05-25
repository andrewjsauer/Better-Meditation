package sauerapps.betterbetterrx.features.main_newsfeed.menu;

import android.app.Dialog;
import android.content.Context;

import sauerapps.betterbetterrx.R;

/**
 * Created by andrewsauer on 5/24/16.
 */
public class ChangePasswordDialog extends Dialog {

    public ChangePasswordDialog(Context context, String email) {
        super(context);
        setContentView(R.layout.dialog_change_password);
    }
}
