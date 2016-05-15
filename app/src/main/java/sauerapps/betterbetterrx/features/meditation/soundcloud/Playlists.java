package sauerapps.betterbetterrx.features.meditation.soundcloud;

import com.google.gson.annotations.SerializedName;

/**
 * Created by andrewsauer on 4/14/16.
 */
public class Playlists {

    @SerializedName("title")
    private String mTitle;

    @SerializedName("id")
    private int mID;

    @SerializedName("stream_url")
    private String mStreamURL;

    @SerializedName("artwork_url")
    private String mArtworkURL;

    @SerializedName("description")
    private String mDescription;

    public String getTitle() {
        return mTitle;
    }

    public int getID() {
        return mID;
    }

    public String getStreamURL() {
        return mStreamURL;
    }

    public String getArtworkURL() {
        return mArtworkURL;
    }

    public String getDescription() {
        return mDescription;
    }

}
