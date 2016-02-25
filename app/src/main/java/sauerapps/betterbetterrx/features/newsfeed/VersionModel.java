package sauerapps.betterbetterrx.features.newsfeed;

/**
 * Created by andrewsauer on 2/9/16.
 */
public class VersionModel {
    public String name;

    public static final String[] data = {"Cupcake", "Donut", "Eclair",
            "Froyo", "Gingerbread", "Honeycomb",
            "Icecream Sandwich", "Jelly Bean", "Kitkat", "Lollipop"};

    VersionModel(String name){
        this.name=name;
    }
}
