package sauerapps.sauermeditation.features.meditation.playlistDetails;


import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import sauerapps.sauermeditation.R;

public class PlaylistTracksHolder extends RecyclerView.ViewHolder {
    ImageView mTrackImageView;
    TextView mTitleTextView;
    CardView mCardView;
    TextView mTitleDescription;

    public PlaylistTracksHolder(View itemView) {
        super(itemView);
        mTrackImageView = (ImageView) itemView.findViewById(R.id.playlist_track_image);
        mTitleTextView = (TextView) itemView.findViewById(R.id.playlist_track_title);
        mTitleDescription = (TextView) itemView.findViewById(R.id.playlist_track_description);
        mCardView = (CardView) itemView.findViewById(R.id.cv);
    }
}
