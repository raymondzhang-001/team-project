package use_case.add_marker;

import entity.Coordinate;

public class AddMarkerInputData
{
    private final Coordinate coordinate;

    public AddMarkerInputData(Coordinate coordinate) {
        this.coordinate = coordinate;

    }

    Coordinate getCoordinate() {
        return coordinate;
    }
}
