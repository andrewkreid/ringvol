package net.ghosttrails.ringvol;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/** Handles re-adding geofences on reboot (called by {@link BootReceiver}). */
public class BootJobService extends JobIntentService {

  private static String TAG = "RingVol_BootJobService";
  public static final int JOB_ID = 0x01;

  public static void enqueueWork(Context context, Intent work) {
    enqueueWork(context, BootJobService.class, JOB_ID, work);
  }

  @Override
  protected void onHandleWork(@NonNull Intent intent) {
    SavedPreferences savedPreferences = new SavedPreferences(this);
    savedPreferences.loadFromPreferences();

    if (savedPreferences.isGeofenceEnabled()) {
      if (savedPreferences.getWorkLatLng() == null) {
        return;
      }
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
          == PackageManager.PERMISSION_GRANTED) {
        GeofencingClient geofencingClient = LocationServices.getGeofencingClient(this);
        geofencingClient.addGeofences(
            GeofenceHelper.getGeofencingRequest(
                savedPreferences.getWorkLatLng(), savedPreferences.getRadiusMeters()),
            GeofenceHelper.makeGeofencePendingIntent(this));
        Log.i(TAG, "Added Geofence on boot");
      }
    }
  }
}
