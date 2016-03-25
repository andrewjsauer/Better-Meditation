package sauerapps.betterbetterrx.features.meditation.audioSection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.firebase.client.ServerValue;

import java.util.HashMap;

import sauerapps.betterbetterrx.utils.Constants;

public class AudioList {

    private String owner;
    private double audioTime;
    private String trackTitle;
    private String trackDescription;
    private HashMap<String, Object> timestampCreated;

    public AudioList() {

    }

    public AudioList(String owner, double audioTime, String trackTitle, String trackDescription,
                     HashMap<String, Object> timestampCreated) {

        this.owner = owner;
        this.audioTime = audioTime;
        this.trackTitle = trackTitle;
        this.trackDescription = trackDescription;
        this.timestampCreated = timestampCreated;
    }

    public String getOwner() {
        return owner;
    }

    public double getAudioTime() {
        return audioTime;
    }

    public String getTrackTitle() {
        return trackTitle;
    }

    public String getTrackDescription() {
        return trackDescription;
    }

    public HashMap<String, Object> getTimestampCreated() {
        return timestampCreated;
    }
}