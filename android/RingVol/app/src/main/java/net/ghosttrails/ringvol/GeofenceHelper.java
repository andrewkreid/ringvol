package net.ghosttrails.ringvol;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.libraries.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;

class GeofenceHelper {

  private static String TAG = "net.ghosttrails.ringvol.GeofencingRequest";

  static GeofencingRequest getGeofencingRequest(LatLng latLng, int radiusMeters) {

    GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
    builder.setInitialTrigger(
        GeofencingRequest.INITIAL_TRIGGER_DWELL
            | GeofencingRequest.INITIAL_TRIGGER_ENTER
            | GeofencingRequest.INITIAL_TRIGGER_EXIT)
        .addGeofences(getGeofenceList(latLng, radiusMeters));
    GeofencingRequest request = builder.build();
    Log.i(TAG, request.toString());
    return request;
  }

  static PendingIntent makeGeofencePendingIntent(Context context) {
    Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
    intent.setAction(GeofenceBroadcastReceiver.GEOFENCE_ACTION);
    // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
    // calling addGeofences() and removeGeofences().
    return PendingIntent.getBroadcast(context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT);
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
            Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_ENTER)
        .setLoiteringDelay(120000)  // 5 min in ms.
        .build());
    return geofenceList;
  }
}
