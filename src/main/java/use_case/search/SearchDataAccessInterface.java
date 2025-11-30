package use_case.search;

import entity.Location;

import java.io.IOException;

public interface SearchDataAccessInterface {

    boolean existsByName(String locationName) throws IOException, InterruptedException;

    Location get(String locationName) throws IOException, InterruptedException;

}
