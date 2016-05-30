package sauerapps.betterbetterrx.features.meditation.playlists;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.List;

import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.features.meditation.soundcloud.Playlists;

/**
 * Created by andrewsauer on 4/14/16.
 */
public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsHolder> {

    private final PlaylistClickListener mListener;
    private Context mContext;
    private List<Playlists> mPlaylists;

    public PlaylistsAdapter (List<Playlists> mPlaylists, Context context, PlaylistClickListener listener) {
        this.mPlaylists = mPlaylists;
        this.mContext = context;
        mListener = listener;
    }

    @Override
    public PlaylistsHolder onCreateViewHolder(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View view = inflater.inflate(R.layout.playlists_row, container, false);

        return new PlaylistsHolder(view);
    }

    @Override
    public void onBindViewHolder(final PlaylistsHolder holder, @SuppressLint("RecyclerView") final int position) {
        Playlists playlists = mPlaylists.get(position);
        holder.mPlaylistTitle.setText(playlists.getTitle());
        holder.mPlaylistDescription.setText(playlists.getDescription());
        Picasso.with(mContext).load(playlists.getArtworkURL())
                .error(R.drawable.ic_default_art)
                .placeholder(R.drawable.ic_default_art)
                .into(holder.mPlaylistImage);

        ViewCompat.setTransitionName(holder.mPlaylistImage, String.valueOf(position) + "_image");
        ViewCompat.setTransitionName(holder.mPlaylistTitle, String.valueOf(position) + "_title");
        ViewCompat.setTransitionName(holder.mPlaylistDescription, String.valueOf(position) + "_description");

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPlaylistClicked(holder, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPlaylists.size();
    }
}