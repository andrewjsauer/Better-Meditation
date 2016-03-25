package sauerapps.betterbetterrx.features.journal.journalList;

/**
 * Created by andrewsauer on 3/8/16.
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.firebase.client.ServerValue;

import java.util.HashMap;

import sauerapps.betterbetterrx.utils.Constants;

/**
 * Defines the data structure for both Active and Archived ShoppingList objects.
 */

public class JournalList {
    private String entryTitle;
//    private String numberOfJournalItems;
    private String owner;
    private HashMap<String, Object> timestampLastChanged;
    private HashMap<String, Object> timestampCreated;
    private HashMap<String, Object> timestampLastChangedReverse;

    /**
     * Required public constructor
     */
    public JournalList() {

    }

    /**
     * Use this constructor to create new Lists.
     * Takes shopping list entryTitle and meditator. Set's the last
     * changed time to what is stored in ServerValue.TIMESTAMP
     *
     */

    public JournalList(String entryTitle, String owner, HashMap<String, Object> timestampCreated) {
        this.entryTitle = entryTitle;
//        this.numberOfJournalItems = numberOfJournalItems;
        this.owner = owner;
        this.timestampCreated = timestampCreated;
        HashMap<String, Object> timestampNowObject = new HashMap<String, Object>();
        timestampNowObject.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
        this.timestampLastChanged = timestampNowObject;
        this.timestampLastChangedReverse = null;
    }

    public String getEntryTitle() {
        return entryTitle;
    }

//    public String getNumberOfJournalItems() {
//        return numberOfJournalItems;
//    }
//
//    public void setNumberOfJournalItems(String numberOfJournalItems) {
//        this.numberOfJournalItems = numberOfJournalItems;
//    }

    public String getOwner() {
        return owner;
    }

    public HashMap<String, Object> getTimestampLastChanged() {
        return timestampLastChanged;
    }

    public HashMap<String, Object> getTimestampCreated() {
        return timestampCreated;
    }

    public HashMap<String, Object> getTimestampLastChangedReverse() {
        return timestampLastChangedReverse;
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


}

