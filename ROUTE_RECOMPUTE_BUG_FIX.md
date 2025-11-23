# Route Recompute Bug Fix

## Problem Description

When adding multiple markers to the map and then editing them (moving up/down or deleting), the application would automatically recompute the route to include **all** markers, even those that hadn't been formally routed yet.

### Specific Issue
1. User adds 2 markers and clicks "Route" → A route is computed connecting them
2. User adds a 3rd marker (not part of the route yet since they didn't click "Route" after adding it)
3. User then deletes one of the original markers
4. **BUG**: The application would automatically recompute and include the new marker in the route, even though it was never connected via the "Route" button

## Root Cause

The `computeAndDisplayRouteIfAuto()` method was being called whenever markers were edited (moved or deleted), and it would recompute the route for **all** currently available stops without distinguishing between:
- Markers that are part of an established route
- Markers that have been added but never formally routed

## Solution

Implemented a **state tracking mechanism** using a boolean flag `routeHasBeenComputed` to track whether a route has been explicitly computed and connected by the user.

### Key Changes in `SearchView.java`

#### 1. Added State Tracking Flag (Line 42)
```java
private boolean routeHasBeenComputed = false;
```
This flag indicates whether the user has explicitly clicked "Route" or pressed Enter to connect markers.

#### 2. Set Flag When Route is Computed (Line 625)
When `computeAndDisplayRoute()` successfully completes, the flag is set to `true`:
```java
mapPanel.setRouteSegments(segs);
routeHasBeenComputed = true;  // Mark that a route has been established
```

#### 3. Modified Auto-Routing Logic (Line 669)
Updated `computeAndDisplayRouteIfAuto()` to only recompute if:
- At least 2 stops exist
- AND a route has already been computed
```java
private void computeAndDisplayRouteIfAuto() {
    if (routingDao != null && stopPositions.size() >= 2 && routeHasBeenComputed) {
        // Only recompute if a route was previously established
        computeAndDisplayRoute();
    } else if (stopPositions.size() < 2) {
        mapPanel.clearRoute();
        routeHasBeenComputed = false;  // Reset when dropping below 2 stops
    }
}
```

#### 4. Reset Flag When Needed (Lines 673, 686)
- Reset when all stops are removed: `routeHasBeenComputed = false;`
- Reset when clearing all stops and routes: `routeHasBeenComputed = false;`

## Behavior After Fix

### Scenario 1: Add markers without routing
1. User adds marker 1
2. User adds marker 2
3. User adds marker 3
4. **No route is computed yet** (user hasn't clicked "Route")

### Scenario 2: Add, route, add, edit
1. User adds marker 1
2. User adds marker 2
3. User clicks "Route" → Route is computed (flag set to `true`)
4. User adds marker 3
5. User deletes marker 2
6. **Route is recomputed using only markers 1 and 3** (marker 3 is ignored since it wasn't part of the original route)

### Scenario 3: Multiple routes
1. User adds markers 1, 2, 3
2. User clicks "Route" → Route connects all three (flag set to `true`)
3. User adds marker 4
4. User deletes marker 2
5. **Route is recomputed between markers 1, 3, and 4** (marker 4 is NOT included since it was added after the route)

## Result

- ✅ Only markers that have been explicitly connected via the "Route" button trigger re-routing when edited
- ✅ New markers added after a route is established don't automatically become part of the route
- ✅ Route only recomputes when necessary (editing of already-connected markers)
- ✅ Users have full control over which markers are included in the route

