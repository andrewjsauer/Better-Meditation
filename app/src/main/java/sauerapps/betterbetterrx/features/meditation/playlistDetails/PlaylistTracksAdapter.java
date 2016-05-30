package sauerapps.betterbetterrx.features.meditation.playlistDetails;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.List;

import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.features.meditation.soundcloud.Track;

public class PlaylistTracksAdapter extends RecyclerView.Adapter<PlaylistTracksHolder> {

    private final PlaylistTracksClickListener mListener;
    private Context mContext;
    private List<Track> mTracks;

    public PlaylistTracksAdapter(List<Track> mTracks, Context context, PlaylistTracksClickListener listener) {
        this.mTracks = mTracks;
        this.mContext = context;
        mListener = listener;
    }

    @Override
    public PlaylistTracksHolder onCreateViewHolder(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View view = inflater.inflate(R.layout.playlist_tracks_row, container, false);

        return new PlaylistTracksHolder(view);
    }

    @Override
    public void onBindViewHolder(final PlaylistTracksHolder holder, final int position) {
        Track track = mTracks.get(position);
        holder.mTitleTextView.setText(track.getTitle());
        holder.mTitleDescription.setText(track.getDescription());
        Picasso.with(mContext).load(track.getArtworkURL())
                .error(R.drawable.ic_default_art)
                .placeholder(R.drawable.ic_default_art)
                .into(holder.mTrackImageView);

        ViewCompat.setTransitionName(holder.mTrackImageView, String.valueOf(position) + "_image");
        ViewCompat.setTransitionName(holder.mTitleTextView, String.valueOf(position) + "_title");
        ViewCompat.setTransitionName(holder.mTitleDescription, String.valueOf(position) + "_description");

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onTrackClicked(holder, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTracks.size();
    }
}