package use_case.search;

import entity.Location;

import java.io.IOException;

/**
 * DAO interface for the Login Use Case.
 */
public interface SearchDataAccessInterface {

    /**
     * Checks if the given locationName exists.
     * @param locationName the locationName to look for
     * @return true if a user with the given locationName exists; false otherwise
     */
    boolean existsByName(String locationName) throws Exception;

    /**
     * Saves the user.
     * @param location the user to save
     */

    void save(Location location);

    /**
    void save(Location location);
     * Returns the user with the given username.
     * @param locationName the username to look up
     * @return the user with the given username
     */
    Location get(String locationName) throws IOException, InterruptedException;

    void setCurrentLocation(String locationName);

    String getCurrentLocationName();
}
