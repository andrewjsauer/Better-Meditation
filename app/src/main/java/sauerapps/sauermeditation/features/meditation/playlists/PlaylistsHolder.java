package sauerapps.sauermeditation.features.meditation.playlists;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import sauerapps.sauermeditation.R;

/**
 * Created by andrewsauer on 4/14/16.
 */
public class PlaylistsHolder extends RecyclerView.ViewHolder {
    protected ImageView mPlaylistImage;
    protected TextView mPlaylistTitle;
    protected CardView mCardView;
    protected TextView mPlaylistDescription;

    public PlaylistsHolder(View itemView) {
        super(itemView);
        mPlaylistImage = (ImageView) itemView.findViewById(R.id.playlist_image);
        mPlaylistTitle = (TextView) itemView.findViewById(R.id.playlist_title);
        mPlaylistDescription = (TextView) itemView.findViewById(R.id.playlist_description);
        mCardView = (CardView) itemView.findViewById(R.id.cv_playlist);
    }
}