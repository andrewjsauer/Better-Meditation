package sauerapps.sauermeditation.features.main_newsfeed;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import sauerapps.sauermeditation.R;

public class FriendsDetailListHolder extends RecyclerView.ViewHolder {

    TextView userFriendName;
    TextView userFriendDate;
    TextView userFriendTrackTitle;
    TextView userFriendTrackDesc;
    TextView userFriendTrackTime;


    public FriendsDetailListHolder(View itemView) {
        super(itemView);

        userFriendName = (TextView) itemView.findViewById(R.id.user_friend_name);
        userFriendDate = (TextView) itemView.findViewById(R.id.user_friend_date);
        userFriendTrackTitle = (TextView) itemView.findViewById(R.id.user_friend_track_title);
        userFriendTrackDesc = (TextView) itemView.findViewById(R.id.user_friend_track_desc);
        userFriendTrackTime = (TextView) itemView.findViewById(R.id.user_friend_time);

    }
}
