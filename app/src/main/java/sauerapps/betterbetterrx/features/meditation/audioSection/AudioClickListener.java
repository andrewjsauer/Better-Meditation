package sauerapps.betterbetterrx.features.meditation.audioSection;

/**
 * Listener for kitten click events in the grid of kittens
 *
 * @author bherbst
 */

public interface AudioClickListener {
    /**
     * Called when a kitten is clicked
     * @param holder The ViewHolder for the clicked kitten
     * @param position The position in the grid of the kitten that was clicked
     */
    void onTrackClicked(TrackRxHolder holder, int position);
}
