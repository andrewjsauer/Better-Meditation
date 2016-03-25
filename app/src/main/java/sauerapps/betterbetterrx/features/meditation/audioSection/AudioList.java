package sauerapps.betterbetterrx.features.meditation.audioSection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.firebase.client.ServerValue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import sauerapps.betterbetterrx.utils.Constants;

public class AudioList {

    private String owner;
    private String userName;
    private double audioTime;
    private String trackTitle;
    private String trackDescription;
    private HashMap<String, Object> timestampCreated;

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
}