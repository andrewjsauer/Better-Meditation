package sauerapps.betterbetterrx.features.meditation.audioSection.audioListDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.firebase.client.ServerValue;

import java.util.HashMap;

import sauerapps.betterbetterrx.utils.Constants;

public class AudioList {

    private String owner;
    private String userName;
    private double audioTime;
    private String trackTitle;
    private String trackDescription;
    private HashMap<String, Object> timestampLastChanged;
    private HashMap<String, Object> timestampCreated;
    private HashMap<String, Object> timestampLastChangedReverse;

    public AudioList() {

    }

    public AudioList(String owner, String userName, double audioTime, String trackTitle, String trackDescription,
                     HashMap<String, Object> timestampCreated) {

        this.owner = owner;
        this.userName = userName;
        this.audioTime = audioTime;
        this.trackTitle = trackTitle;
        this.trackDescription = trackDescription;
        this.timestampCreated = timestampCreated;
        HashMap<String, Object> timestampNowObject = new HashMap<String, Object>();
        timestampNowObject.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
        this.timestampLastChanged = timestampNowObject;
        this.timestampLastChangedReverse = null;
    }

    public String getOwner() {
        return owner;
    }

    public String getUserName() {
        return userName;
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

    @JsonIgnore
    public long getTimestampLastChangedLong() {

        return (long) timestampLastChanged.get(Constants.FIREBASE_PROPERTY_TIMESTAMP);
    }

    @JsonIgnore
    public long getTimestampCreatedLong() {
        return (long) timestampLastChanged.get(Constants.FIREBASE_PROPERTY_TIMESTAMP);
    }

    @JsonIgnore
    public long getTimestampLastChangedReverseLong() {

        return (long) timestampLastChangedReverse.get(Constants.FIREBASE_PROPERTY_TIMESTAMP);
    }

    public void setTimestampLastChangedToNow() {
        HashMap<String, Object> timestampNowObject = new HashMap<String, Object>();
        timestampNowObject.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
        this.timestampLastChanged = timestampNowObject;
    }

    public HashMap<String, Object> getTimestampLastChanged() {
        return timestampLastChanged;
    }


    public HashMap<String, Object> getTimestampLastChangedReverse() {
        return timestampLastChangedReverse;
    }
}