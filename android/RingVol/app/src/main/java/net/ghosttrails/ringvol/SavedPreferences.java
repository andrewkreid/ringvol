package net.ghosttrails.ringvol;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import com.google.android.libraries.maps.model.LatLng;

public class SavedPreferences {

  public static final int DEFAULT_RADIUS_METERS = 300;
  public static final int DEFAULT_WORK_VOLUME = 3;
  public static final int DEFAULT_HOME_VOLUME = 10;

  private final Context context;

  private @Nullable LatLng workLatLng;
  private int radiusMeters = DEFAULT_RADIUS_METERS;
  private int workVolume = DEFAULT_WORK_VOLUME;
  private int homeVolume = DEFAULT_HOME_VOLUME;
  private boolean geofenceEnabled;

  public SavedPreferences(Context context) {
    this.context = context;
  }

  public SavedPreferences(
      Context context,
      @Nullable LatLng workLatLng,
      int radiusMeters,
      int workVolume,
      int homeVolume,
      boolean geofenceEnabled) {
    this.context = context;
    this.workLatLng = workLatLng;
    this.radiusMeters = radiusMeters;
    this.workVolume = workVolume;
    this.homeVolume = homeVolume;
    this.geofenceEnabled = geofenceEnabled;
  }

  void saveToPreferences() {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = preferences.edit();
    if (workLatLng != null) {
      editor.putFloat("workLat", (float) workLatLng.latitude);
      editor.putFloat("workLng", (float) workLatLng.longitude);
    }
    editor.putInt("radiusMeters", radiusMeters);
    editor.putInt("workVolume", workVolume);
    editor.putInt("homeVolume", homeVolume);
    editor.putBoolean("geofenceEnabled", geofenceEnabled);
    editor.apply();
  }

  void loadFromPreferences() {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    float lat = preferences.getFloat("workLat", Float.NaN);
    float lng = preferences.getFloat("workLng", Float.NaN);
    if (!Float.isNaN(lat) && !Float.isNaN(lng)) {
      workLatLng = new LatLng(lat, lng);
    } else {
      workLatLng = null;
    }
    radiusMeters = preferences.getInt("radiusMeters", DEFAULT_RADIUS_METERS);
    workVolume = preferences.getInt("workVolume", DEFAULT_WORK_VOLUME);
    homeVolume = preferences.getInt("homeVolume", DEFAULT_HOME_VOLUME);
    geofenceEnabled = preferences.getBoolean("geofenceEnabled", false);
  }

  @Nullable
  LatLng getWorkLatLng() {
    return workLatLng;
  }

  public void setWorkLatLng(@Nullable LatLng workLatLng) {
    this.workLatLng = workLatLng;
  }

  int getRadiusMeters() {
    return radiusMeters;
  }

  public void setRadiusMeters(int radiusMeters) {
    this.radiusMeters = radiusMeters;
  }

  int getHomeVolume() {
    return homeVolume;
  }

  public void setHomeVolume(int homeVolume) {
    this.homeVolume = homeVolume;
  }

  int getWorkVolume() {
    return workVolume;
  }

  public void setWorkVolume(int workVolume) {
    this.workVolume = workVolume;
  }

  boolean isGeofenceEnabled() {
    return geofenceEnabled;
  }

  public void setGeofenceEnabled(boolean geofenceEnabled) {
    this.geofenceEnabled = geofenceEnabled;
  }
}
