package entity;

public class Coordinate {
    private final double latitude;
    private final double longitude;

    public Coordinate(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    Pair<double[], double[]> getCoordinates(){
        return new Pair(latitude, longitude);
    }
}
