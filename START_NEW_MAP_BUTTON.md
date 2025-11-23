# "Start new map" Button Implementation

## Summary of Changes

Added a "Start new map" button to the bottom right of the UI that clears all stops, routes, and markers when clicked, reverting the UI to its original state.

## Changes Made:

### 1. Added "Start new map" Button (SearchView.java, ~line 472-485)
- Created a new `JButton` called `startNewMapButton`
- Positioned in the bottom-right corner using `FlowLayout(FlowLayout.RIGHT, ...)`
- Added tooltip: "Clear all stops and routes to start over"
- Button size: 120 x 34 pixels
- Added action listener that calls `clearAllStopsAndRoutes()` method

### 2. Integrated Button into UI Layout
- Added the button to the existing `progressPanelContainer`
- The button appears next to the reroute progress bar area
- Layout remains aligned to the right side with proper spacing

### 3. Implemented clearAllStopsAndRoutes() Method (SearchView.java, ~line 726-737)
This new private method performs the following actions:
- Clears all stops from `stopsListModel` (sidebar list)
- Clears all stop positions from `stopPositions` list
- Calls `mapPanel.clearStops()` to remove all markers from the map
- Calls `mapPanel.clearRoute()` (via clearStops) to remove the route visualization
- Clears the search input field
- Clears the current suggestions list

## UI Behavior:

### Before Clicking "Start new map":
- Sidebar displays all added locations with numbered badges and connecting lines
- Map shows all markers and the route connecting them
- Search field contains the last search query

### After Clicking "Start new map":
- Sidebar becomes empty (all locations cleared)
- All markers disappear from the map
- Route visualization is removed from the map
- Search field is cleared
- Map reverts to its initial state (same as when program first opens)
- Route progress bar is hidden if it was visible

## File Modified:
- `src/main/java/view/SearchView.java`
  - Line ~472-485: Added "Start new map" button creation and layout
  - Line ~726-737: Added `clearAllStopsAndRoutes()` method

## Integration with Existing Code:
- Uses existing `MapPanel.clearStops()` method to clear markers and routes
- Reuses existing UI components and styling
- No changes to other classes needed
- Fully backward compatible with existing functionality

## Build Status:
✓ Project compiles successfully with Maven
✓ No compilation errors (only pre-existing warnings)
✓ All existing functionality preserved

