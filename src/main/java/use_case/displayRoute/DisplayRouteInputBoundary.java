package use_case.displayRoute;

/**
 * Input Boundary for actions related to calculating and displaying a route.
 */

public interface DisplayRouteInputBoundary {
    /**
     * Executes the Display Route use case. *
     * @param inputData the input data containing start and end coordinates
     */
    void execute(DisplayRouteInputData inputData);
}