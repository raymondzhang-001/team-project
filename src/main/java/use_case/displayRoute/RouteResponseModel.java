package use_case.displayRoute;

/**
 * A simple data object representing a computed route.
 * This is returned by RouteDataAccessInterface and consumed by the interactor
 */

public class RouteResponseModel {

    private final double[] longitudes;
    private final double[] latitudes;
    private final double distanceInMeters;
    private final double durationInSeconds;

    /**
     * @param latitudes latitude coordinates along the route polyline
     * @param longitudes longitude coordinates along the route polyline
     * @param distanceInMeters total distance of route
     * @param durationInSeconds estimated travel time
     */

    public RouteResponseModel(double[] longitudes, double[] latitudes,
                              double distanceInMeters, double durationInSeconds) {

        this.longitudes = longitudes;
        this.latitudes = latitudes;
        this.distanceInMeters = distanceInMeters;
        this.durationInSeconds = durationInSeconds;
    }

    public double[] getLongitudes() {
        return longitudes;
    }

    public double[] getLatitudes() {
        return latitudes;
    }

    public double getDistanceInMeters() {
        return distanceInMeters;
    }

    public double getDurationInSeconds() {
        return durationInSeconds;
    }
}
