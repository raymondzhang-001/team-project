package interface_adapter.addMarker;

public class AddMarkerState {

    private Double lastMarkerLatitude;
    private Double lastMarkerLongitude;
    private String errorMessage;

    public AddMarkerState() {
    }

    public Double getLastMarkerLatitude() {
        return lastMarkerLatitude;
    }

    public void setLastMarkerLatitude(Double lastMarkerLatitude) {
        this.lastMarkerLatitude = lastMarkerLatitude;
    }

    public Double getLastMarkerLongitude() {
        return lastMarkerLongitude;
    }

    public void setLastMarkerLongitude(Double lastMarkerLongitude) {
        this.lastMarkerLongitude = lastMarkerLongitude;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

