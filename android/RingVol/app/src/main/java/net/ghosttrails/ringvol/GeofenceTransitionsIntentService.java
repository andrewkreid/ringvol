package net.ghosttrails.ringvol;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import java.util.ArrayList;
import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {

  private static String TAG = "GeofenceTransitionsIntentService";

  GeofenceTransitionsIntentService() {
    super(GeofenceTransitionsIntentService.class.getSimpleName());
  }

  protected void onHandleIntent(Intent intent) {
    GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
    if (geofencingEvent.hasError()) {
      //String errorMessage = GeofenceErrorMessages.getErrorString(this,
      //    geofencingEvent.getErrorCode());
      Log.e(TAG, GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode()));
      return;
    }

    // Get the transition type.
    int geofenceTransition = geofencingEvent.getGeofenceTransition();

    // Test that the reported transition was of interest.
    if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL ||
        geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

      // Get the geofences that were triggered. A single event can trigger
      // multiple geofences.
      List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

      // Get the transition details as a String.
      String geofenceTransitionDetails = getGeofenceTransitionDetails(
          geofenceTransition,
          triggeringGeofences
      );

      // Send notification and log the transition details.
      NotificationHelper.sendNotification(this, "Geofence Event", geofenceTransitionDetails);
      Log.i(TAG, geofenceTransitionDetails);
    } else {
      // Log the error.
      Log.e(TAG, "Error: " + getTransitionString(geofenceTransition));
    }
  }

  private String getGeofenceTransitionDetails(
      int geofenceTransition,
      List<Geofence> triggeringGeofences) {

    String geofenceTransitionString = getTransitionString(geofenceTransition);

    // Get the Ids of each geofence that was triggered.
    ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
    for (Geofence geofence : triggeringGeofences) {
      triggeringGeofencesIdsList.add(geofence.getRequestId());
    }
    String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

    return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
  }

  private String getTransitionString(int transitionType) {
    switch (transitionType) {
      case Geofence.GEOFENCE_TRANSITION_ENTER:
        return "Geofence Entered";
      case Geofence.GEOFENCE_TRANSITION_EXIT:
        return "Geofence Exited";
      case Geofence.GEOFENCE_TRANSITION_DWELL:
        return "Geofence Dwell";
      default:
        return "Geofence Unknown transition";
    }
  }
}