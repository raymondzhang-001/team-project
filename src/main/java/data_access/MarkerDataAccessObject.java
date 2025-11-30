package data_access;

import entity.Location;
import entity.Marker;
import use_case.add_marker.AddMarkerDataAccessInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * AddMarker 전용 DAO.
 * - 메모리에 Marker 들을 저장만 한다.
 * - Coordinate 안 쓰고 Location/Marker 만 사용.
 */
public class MarkerDataAccessObject implements AddMarkerDataAccessInterface {

    private final List<Marker> markers = new ArrayList<>();

    @Override
    public boolean exists(Location location) {
        for (Marker m : markers) {
            if (Double.compare(m.getLatitude(), location.getLatitude()) == 0 &&
                    Double.compare(m.getLongitude(), location.getLongitude()) == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void save(Marker marker) {
        markers.add(marker);
    }

    @Override
    public List<Marker> getAllMarkers() {
        return new ArrayList<>(markers);
    }
}
