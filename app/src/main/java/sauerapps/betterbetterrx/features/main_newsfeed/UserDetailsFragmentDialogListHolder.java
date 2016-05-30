package sauerapps.betterbetterrx.features.main_newsfeed;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import sauerapps.betterbetterrx.R;

/**
 * Created by andrewsauer on 5/29/16.
 */
public class UserDetailsFragmentDialogListHolder extends RecyclerView.ViewHolder {

    TextView userDate;
    TextView userTrackTitle;
    TextView userTrackDesc;
    TextView userTrackTime;


    public UserDetailsFragmentDialogListHolder(View itemView) {
        super(itemView);

        userDate = (TextView) itemView.findViewById(R.id.dialog_user_friend_date);
        userTrackTitle = (TextView) itemView.findViewById(R.id.dialog_user_friend_track_title);
        userTrackDesc = (TextView) itemView.findViewById(R.id.dialog_user_friend_track_desc);
        userTrackTime = (TextView) itemView.findViewById(R.id.dialog_user_friend_time);

    }
}

