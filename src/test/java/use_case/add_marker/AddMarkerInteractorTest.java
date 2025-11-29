// src/test/java/use_case/add_marker/AddMarkerInteractorTest.java
package use_case.add_marker;

import entity.Location;
import entity.Marker;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for AddMarkerInteractor.
 * - 중복이 아닌 좌표가 들어왔을 때 정상적으로 저장되고
 *   성공 Presenter가 호출되는지 테스트.
 */
class AddMarkerInteractorTest {

    /**
     * 간단한 인메모리 더블: DB 대신 사용.
     */
    private static class InMemoryAddMarkerDataAccess implements AddMarkerDataAccessInterface {

        // 위도,경도 문자열을 key로 저장
        private final Set<String> keys = new HashSet<>();

        private String keyOf(Location loc) {
            return loc.getLatitude() + "," + loc.getLongitude();
        }

        @Override
        public boolean exists(Location location) {
            return keys.contains(keyOf(location));
        }


        @Override
        public void save(Marker marker) {
            keys.add(keyOf(marker.getLocation()));
        }

    }

    /**
     * Presenter 더블: Interactor가 무엇을 넘기는지 체크만 함.
     */
    @Nested
    public class TestAddMarkerPresenter implements AddMarkerOutputBoundary {

        AddMarkerOutputData successData;
        String failMessage;

        @Override
        public void prepareSuccessView(AddMarkerOutputData response) {
            this.successData = response;
        }

        @Override
        public void prepareFailView(String error) {
            this.failMessage = error;
        }


        @Test
        void addsNewMarkerWhenNotDuplicate() {
            // given
            InMemoryAddMarkerDataAccess repo = new InMemoryAddMarkerDataAccess();
            TestAddMarkerPresenter presenter = new TestAddMarkerPresenter();
            AddMarkerInteractor interactor = new AddMarkerInteractor(repo, presenter);

            double lat = 43.6532;
            double lon = -79.3832;
            AddMarkerInputData input = new AddMarkerInputData(lat, lon);

            // when
            interactor.execute(input);

            // then: 성공 뷰가 호출됐는지
            assertNotNull(presenter.successData.toString(), "Success output should not be null");
            assertNull(presenter.failMessage, "Fail message should be null on success");

            assertEquals(lat, presenter.successData.getLatitude(), 1e-9);
            assertEquals(lon, presenter.successData.getLongitude(), 1e-9);
        }
    }
}
