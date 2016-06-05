package sauerapps.sauermeditation.features.journal;

/**
 * Created by andrewsauer on 3/9/16.
 */
public class JournalListItem {
    private String itemName;
    private String owner;
    private String boughtBy;
    private boolean bought;

    public JournalListItem() {
    }

    public JournalListItem(String itemName, String owner) {
        this.itemName = itemName;
        this.owner = owner;
        this.boughtBy = null;
        this.bought = false;

    }

    public String getItemName() { return itemName; }

    public String getOwner() {
        return owner;
    }

    public String getBoughtBy() {
        return boughtBy;
    }

    public boolean isBought() {
        return bought;
    }

}
