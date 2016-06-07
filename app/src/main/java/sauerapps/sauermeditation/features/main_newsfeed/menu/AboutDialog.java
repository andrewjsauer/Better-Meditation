package sauerapps.sauermeditation.features.main_newsfeed.menu;

import android.app.Dialog;
import android.content.Context;

import butterknife.ButterKnife;
import butterknife.OnClick;
import sauerapps.sauermeditation.R;

/**
 * Created by andrewsauer on 5/28/16.
 */
public class AboutDialog extends Dialog {

    public AboutDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_about_section);

        ButterKnife.bind(this);

        setTitle("About");

    }

    @OnClick (R.id.about_got_it)
    void gotItButton() {
        dismiss();
    }
}
