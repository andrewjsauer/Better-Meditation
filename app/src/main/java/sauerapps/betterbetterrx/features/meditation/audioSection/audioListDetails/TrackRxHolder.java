package sauerapps.betterbetterrx.features.meditation.audioSection.audioListDetails;


import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import sauerapps.betterbetterrx.R;

public class TrackRxHolder extends RecyclerView.ViewHolder {
    protected ImageView mTrackImageView;
    protected TextView mTitleTextView;
    protected CardView mCardView;
    protected TextView mTitleDescription;

    public TrackRxHolder(View itemView) {
        super(itemView);
        mTrackImageView = (ImageView) itemView.findViewById(R.id.track_image);
        mTitleTextView = (TextView) itemView.findViewById(R.id.track_title);
        mTitleDescription = (TextView) itemView.findViewById(R.id.track_artist);
        mCardView = (CardView) itemView.findViewById(R.id.cv);
    }
}
