package use_case.add_marker;

import entity.Location;
import entity.Marker;

import java.util.List;

/**
 * DAO interface for AddMarkerAccessInterface
 */

public interface AddMarkerAccessInterface {
    /**
     * checks if a marker already exists at the given location
     */
    boolean exists(Location location);

    /**
     * saves the marker
     */
    void save(Marker marker);

    /**
     * Returns all markers currently stored
     */
    List<Marker> getAllMarkers();

}
