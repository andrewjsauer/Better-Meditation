package sauerapps.sauermeditation.utils;

import sauerapps.sauermeditation.BuildConfig;

/**
 * Created by andrewsauer on 2/12/16.
 */
public class Constants {

    /**
     * Constants related to locations in Firebase, such as the name of the node
     * where user lists are stored (ie "userLists")
     */
    public static final String FIREBASE_LOCATION_USERS = "users";
    public static final String FIREBASE_LOCATION_JOURNAL_ENTERIES = "userJournalEnteries";
    public static final String FIREBASE_LOCATION_USER_FRIENDS = "userFriends";
    public static final String FIREBASE_LOCATION_JOURNAL_LISTS_SHARED_WITH = "sharedWith";
    public static final String FIREBASE_LOCATION_UID_MAPPINGS = "uidMappings";
    public static final String FIREBASE_LOCATION_OWNER_MAPPINGS = "ownerMappings";
    public static final String FIREBASE_LOCATION_JOURNAL_ENTRY_ITEMS = "journalEntryItems";

    public static final String FIREBASE_LOCATION_USER_AUDIO = "userAudio";
    public static final String FIREBASE_LOCATION_USER_AUDIO_DETAILS = "userAudioDetails";
    public static final String FIREBASE_LOCATION_USER_AUDIO_DETAILS_LIST = "userAudioDetailsList";
    public static final String FIREBASE_LOCATION_USER_AUDIO_DETAILS_SHARED_WITH = "audioSharedWith";





    /**
     * Constants for Firebase object properties
     */
    public static final String FIREBASE_PROPERTY_BOUGHT_BY = "boughtBy";
    public static final String FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED = "timestampLastChanged";
    public static final String FIREBASE_PROPERTY_TIMESTAMP = "timestamp";
    public static final String FIREBASE_PROPERTY_ITEM_ENTRY = "itemEntry";
    public static final String FIREBASE_PROPERTY_EMAIL = "email";
    public static final String FIREBASE_PROPERTY_USER_HAS_LOGGED_IN_WITH_PASSWORD = "hasLoggedInWithPassword";
    public static final String FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED_REVERSE = "timestampLastChangedReverse";


    /**
     * Constants for Firebase URL
     */
    public static final String FIREBASE_URL = BuildConfig.UNIQUE_FIREBASE_ROOT_URL;
    public static final String FIREBASE_URL_USERS = FIREBASE_URL + "/" + FIREBASE_LOCATION_USERS;
    public static final String FIREBASE_URL_USER_LISTS = FIREBASE_URL + "/" + FIREBASE_LOCATION_JOURNAL_ENTERIES;
    public static final String FIREBASE_URL_USER_FRIENDS = FIREBASE_URL + "/" + FIREBASE_LOCATION_USER_FRIENDS;
    public static final String FIREBASE_URL_LISTS_SHARED_WITH = FIREBASE_URL + "/" + FIREBASE_LOCATION_JOURNAL_LISTS_SHARED_WITH;

    public static final String FIREBASE_URL_AUDIO_DETAILS_SHARED_WITH = FIREBASE_URL + "/" + FIREBASE_LOCATION_USER_AUDIO_DETAILS_SHARED_WITH;

    public static final String FIREBASE_URL_USER_AUDIO = FIREBASE_URL + "/" + FIREBASE_LOCATION_USER_AUDIO;
    public static final String FIREBASE_URL_USER_AUDIO_DETAILS = FIREBASE_URL + "/" + FIREBASE_LOCATION_USER_AUDIO_DETAILS;
    public static final String FIREBASE_URL_USER_AUDIO_DETAILS_LIST = FIREBASE_URL + "/" + FIREBASE_LOCATION_USER_AUDIO_DETAILS_LIST;

    public static final String FIREBASE_URL_JOURNAL_LIST_ITEMS = FIREBASE_URL + "/" + FIREBASE_LOCATION_JOURNAL_ENTRY_ITEMS;

    /**
     * Constants for bundles, extras and shared preferences keys
     */
    public static final String KEY_NAME = "LIST_NAME";
    public static final String KEY_USER_NAME = "USER_NAME";
    public static final String KEY_EMAIL_CHECK = "EMAIL_CHECK";
    public static final String KEY_LAYOUT_RESOURCE = "LAYOUT_RESOURCE";
    public static final String KEY_LIST_ID = "LIST_ID";
    public static final String KEY_SIGNUP_EMAIL = "SIGNUP_EMAIL";
    public static final String KEY_LIST_ITEM_NAME = "ITEM_NAME";
    public static final String KEY_LIST_ITEM_ID = "LIST_ITEM_ID";
    public static final String KEY_PROVIDER = "PROVIDER";
    public static final String KEY_ENCODED_EMAIL = "ENCODED_EMAIL";
    public static final String KEY_USERS_EMAIL = "USERS_EMAIL";
    public static final String KEY_LIST_OWNER = "LIST_OWNER";
    public static final String KEY_GOOGLE_EMAIL = "GOOGLE_EMAIL";
    public static final String KEY_PREF_SORT_ORDER_LISTS = "PERF_SORT_ORDER_LISTS";
    public static final String KEY_SHARED_WITH_USERS = "SHARED_WITH_USERS";
    public static final String KEY_PLAYLIST_POSITION = "PLAYLIST_POSITION";
    public static final String KEY_TRACK_POSITION = "TRACK_POSITION";





    /**
     * Constants for Firebase login
     */
    public static final String PASSWORD_PROVIDER = "password";
    public static final String GOOGLE_PROVIDER = "google";
    public static final String PROVIDER_DATA_DISPLAY_NAME = "displayName";


    /**
     * Constant for sorting
     */
    public static final String ORDER_BY_KEY = "orderByPushKey";
    public static final String ORDER_BY_OWNER_EMAIL = "orderByOwnerEmail";




}
