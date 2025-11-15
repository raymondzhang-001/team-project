package use_case.displayRoute;

import java.io.IOException;

/**
 * DAO interface for the display route usecase
 */

public interface DisplayRouteDataAccessInterface {

    /**
     * computes a route between two points
     * @param startLongitude longitude of start point
     * @param startLatitude latitude of start point
     * @param endLongitude longitude of end point
     * @param endLatitude latitude of end point
     * @return a RouteResponseModel representing the route
     */

    RouteResponseModel getRoute(double startLongitude, double startLatitude,
                                double endLongitude, double endLatitude)
        throws IOException, InterruptedException;


}
