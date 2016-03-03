package sauerapps.betterbetterrx.features.meditation;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import sauerapps.betterbetterrx.R;
import sauerapps.betterbetterrx.features.SoundCloud.Track;

/**
 * Created by andrewsauer on 3/1/16.
 */
public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private static final String TAG = TrackAdapter.class.getSimpleName();

    private Context mContext;
    private List<Track> mTracks;
    private static MyClickListener myClickListener;



    public TrackAdapter (List<Track> mTracks, Context context) {
        this.mTracks = mTracks;
        this.mContext = context;
    }



    public static class TrackViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected ImageView trackImageView;
        protected TextView titleTextView;
        protected CardView mCardView;
        protected TextView titledescription;


        public TrackViewHolder(View view) {
            super(view);
            trackImageView = (ImageView) view.findViewById(R.id.track_image);
            titleTextView = (TextView) view.findViewById(R.id.track_title);
            titledescription = (TextView) view.findViewById(R.id.track_description);
            mCardView = (CardView) itemView.findViewById(R.id.cv);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "Click!");
            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }



    @Override
    public void onBindViewHolder(TrackViewHolder holder, int position) {
        Track track = mTracks.get(position);
        holder.titleTextView.setText(track.getTitle());
        holder.titledescription.setText(track.getDescription());
        Picasso.with(mContext).load(track.getArtworkURL())
                .error(R.drawable.ic_default_art)
                .placeholder(R.drawable.ic_default_art)
                .into(holder.trackImageView);
    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.track_list_row, parent, false);

        return new TrackViewHolder(view);
    }


    @Override
    public int getItemCount() {
        return mTracks.size();
    }


    public interface MyClickListener {
        void onItemClick(int position, View v);
    }

}
