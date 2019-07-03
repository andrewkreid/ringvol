package net.ghosttrails.ringvol;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

class RingVolumeHelper {

  private static String TAG = "RingVolumeHelper";

  static void setRingVolume(Context context, int volume) {
    try {
      AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
      if (am != null) {
        Log.i(TAG, "Setting volume to " + volume);
        am.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
      }
    } catch (SecurityException e) {
      Log.e(TAG, "SecurityException " + e.getMessage(), e);
    }
  }
}
