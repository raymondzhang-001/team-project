package use_case.add_marker;

import entity.Coordinate;

import java.util.ArrayList;

public interface AddMarkerAccessInterface {

    /**
     * Checks if the marker with the same coordinate exists
     * @param coordinate the coordinate to look for
     * @return true if the same coordinate exists
     */
    boolean existsByCoordinate(Coordinate coordinate) throws Exception;

    /**
     * Saves the coordinate
     * @param coordinate the user to save
     */
    void save(Coordinate coordinate);

    ArrayList<Coordinate> getCurrentMarker();

}
