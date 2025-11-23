# Mouse Scroll Wheel Zoom - Cursor-Based Center

## Summary of Changes

Modified the mouse scroll wheel zoom functionality to center on the mouse cursor position instead of the map center. This provides a more intuitive zooming experience where you zoom towards where your mouse is pointing.

## How It Works:

### Before:
- Mouse scroll wheel zoom was centered on the middle of the map UI
- Zooming in/out would keep the same point at the center visible

### After:
- Mouse scroll wheel zoom is now centered on the **current mouse cursor position**
- The location under your cursor stays under your cursor as you zoom in/out
- Works regardless of whether the mouse button is clicked or not - just hover and scroll

## Technical Implementation:

The zoom logic in `MapPanel.java` now performs the following steps:

1. **Capture Mouse Position**: Get the screen coordinates of the mouse cursor from the scroll event (`e.getPoint()`)

2. **Convert to Geographic Coordinates**: Convert the screen position to a geographic position using `mapViewer.convertPointToGeoPosition()`

3. **Apply Zoom**: Change the zoom level by the amount calculated from the scroll wheel rotation

4. **Recalculate Screen Position**: Convert the same geographic position back to screen coordinates at the new zoom level using `mapViewer.convertGeoPositionToPoint()`

5. **Calculate Shift**: Determine how far the geo-location moved on the screen due to the zoom change (dx, dy)

6. **Pan to Compensate**: Adjust the map center so that the original geographic location stays under the cursor

7. **Update Display**: Set the new center location on the map viewer

## Code Changes:

File: `src/main/java/view/MapPanel.java` (lines ~207-252)

**Old Logic:**
```java
GeoPosition center = mapViewer.getAddressLocation();
mapViewer.setZoom(targetInt);
mapViewer.setAddressLocation(center);  // Just keep the same center
```

**New Logic:**
```java
Point mousePos = e.getPoint();
GeoPosition geoAtMouse = mapViewer.convertPointToGeoPosition(mousePos);

if (geoAtMouse != null) {
    mapViewer.setZoom(targetInt);
    java.awt.geom.Point2D newMouseScreenPos2D = mapViewer.convertGeoPositionToPoint(geoAtMouse);
    
    if (newMouseScreenPos2D != null) {
        // Calculate shift and pan to keep geo-location under cursor
        // ... [compensation logic] ...
    }
}
```

## Behavior Examples:

| Scenario | Behavior |
|----------|----------|
| **Hover over a location and scroll forward** | Zooms in, keeping that location centered under your cursor |
| **Hover over a location and scroll backward** | Zooms out, keeping that location centered under your cursor |
| **Move cursor to different spot and scroll** | Zooms towards the new cursor position |
| **Trackpad pinch** | Unaffected - continues to work as before |

## Build Status:
✓ Project compiles successfully with Maven
✓ No compilation errors
✓ All existing functionality preserved
✓ Ready to use!

## Notes:
- This enhancement works exclusively for hardware mouse wheel scrolling
- Trackpad pinch-to-zoom and two-finger scrolling remain unchanged
- The feature is backwards compatible with all existing code

