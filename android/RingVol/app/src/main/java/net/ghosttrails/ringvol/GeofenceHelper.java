package net.ghosttrails.ringvol;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.libraries.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;

class GeofenceHelper {

  static GeofencingRequest getGeofencingRequest(LatLng latLng, int radiusMeters) {

    GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
    builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
    builder.addGeofences(getGeofenceList(latLng, radiusMeters));
    return builder.build();
  }

  private static List<Geofence> getGeofenceList(LatLng latLng, int radiusMeters) {
    List<Geofence> geofenceList = new ArrayList<>();
    geofenceList.add(new Geofence.Builder()
        // Set the request ID of the geofence. This is a string to identify this
        // geofence.
        .setRequestId("ringVolRequestID")

        .setCircularRegion(
            latLng.latitude,
            latLng.longitude,
            radiusMeters
        )
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL |
            Geofence.GEOFENCE_TRANSITION_EXIT)
        .setLoiteringDelay(300000)
        .build());
    return geofenceList;
  }
}
