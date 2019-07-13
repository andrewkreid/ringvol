package net.ghosttrails.ringvol;

import static net.ghosttrails.ringvol.SavedPreferences.DEFAULT_HOME_VOLUME;
import static net.ghosttrails.ringvol.SavedPreferences.DEFAULT_WORK_VOLUME;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import java.util.ArrayList;
import java.util.List;

public class GeofenceTransitionsIntentService extends JobIntentService {

  private static String TAG = "ringVol.GeofenceTransitionsIntentService";

  static final int JOB_ID = 1000;

  static void enqueueWork(Context context, Intent work) {
    enqueueWork(context, GeofenceTransitionsIntentService.class, JOB_ID, work);
  }

  @Override
  protected void onHandleWork(@NonNull Intent intent) {
   GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
    if (geofencingEvent.hasError()) {
      Log.e(TAG, GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode()));
      return;
    }
    Log.i(TAG, "Got Intent");

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

      setVolume(geofenceTransition);

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

  private void setVolume(int mode) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    if (mode == Geofence.GEOFENCE_TRANSITION_DWELL) {
      if (preferences.contains("workVolume")) {
        int workVolume = preferences.getInt("workVolume", DEFAULT_WORK_VOLUME);
        RingVolumeHelper.setRingVolume(this, workVolume);
      }
    } else if (mode == Geofence.GEOFENCE_TRANSITION_EXIT) {
      int homeVolume = preferences.getInt("homeVolume", DEFAULT_HOME_VOLUME);
      RingVolumeHelper.setRingVolume(this, homeVolume);
    } else {
      Log.e(TAG, "Unrecognised transition type " + mode);
    }
  }

}