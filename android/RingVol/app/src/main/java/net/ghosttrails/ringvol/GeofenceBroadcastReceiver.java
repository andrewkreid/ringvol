package net.ghosttrails.ringvol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

  private static String TAG = "ringvol.GeofenceBroadcastReceiver";

  static final String GEOFENCE_ACTION = "net.ghosttrails.ringvol.action.GEOFENCE_ACTION";

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i(TAG, "onReceive");
    if (intent != null) {
      Log.i(TAG, "Action : " + intent.getAction());
      GeofenceTransitionsIntentService.enqueueWork(context, intent);
    }
  }
}
