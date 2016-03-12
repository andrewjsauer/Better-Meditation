package sauerapps.betterbetterrx.model;

/**
 * Created by andrewsauer on 3/9/16.
 */
/**
 * Defines the data structure for ShoppingListItem objects.
 */
public class JournalListItem {
    private String itemName;
    private String owner;
    private String boughtBy;
    private boolean bought;

    /**
     * Required public constructor
     */
    public JournalListItem() {
    }

    /**
     * Use this constructor to create new ShoppingListItem.
     * Takes shopping list item name and list item owner email as params
     *
     * @param itemName
     * @param owner
     */
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
