package use_case.add_marker;

import entity.Location;

/**
AddMarkerInteractor
 */
public class AddMarkerInteractor implements AddMarkerInputBoundary {
    private final AddMarkerDataAccessInterface addMarkerAccess;
    private final AddMarkerOutputBoundary addMarkerPresenter;

    public AddMarkerInteractor(AddMarkerDataAccessInterface addMarkerAccess,
                               AddMarkerOutputBoundary addMarkerPresenter) {
        this.addMarkerAccess = addMarkerAccess;
        this.addMarkerPresenter = addMarkerPresenter;
    }

    @Override
    public void execute(AddMarkerInputData inputData) {
        double lat = inputData.getLatitude();
        double lon = inputData.getLongitude();

        Location location = new Location("", lat, lon);

        if ()
    }



}
