package net.ghosttrails.ringvol;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.maps.CameraUpdate;
import com.google.android.libraries.maps.CameraUpdateFactory;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.libraries.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.libraries.maps.MapFragment;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.model.Circle;
import com.google.android.libraries.maps.model.CircleOptions;
import com.google.android.libraries.maps.model.LatLng;
import com.google.android.libraries.maps.model.Marker;
import com.google.android.libraries.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
    OnMyLocationButtonClickListener,
    OnMyLocationClickListener {

  private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1337;
  private static final int DEFAULT_RADIUS_METERS = 300;
  private static final float DEFAULT_ZOOM = 15.0f;
  private static final int DEFAULT_WORK_VOLUME = 30;
  private static final int DEFAULT_HOME_VOLUME = 100;

  private FusedLocationProviderClient fusedLocationClient;

  private View settingsView;
  private View mapView;
  private View eventView;
  private TextView geofenceLabel;
  private SeekBar radiusSeekBar;
  private SeekBar homeVolumeSeekBar;
  private SeekBar workVolumeSeekBar;
  private TextView homeVolumeValue;
  private TextView workVolumeValue;

  private GoogleMap mMap;
  private Marker workMarker;
  private Circle radiusCircle;

  private int currentTab;

  /**
   * LatLng of the saved work location.
   */
  private LatLng workLatLng;
  private int radiusMeters = DEFAULT_RADIUS_METERS;

  /**
   * Volume settings
   */
  private int workVolume = DEFAULT_WORK_VOLUME;
  private int homeVolume = DEFAULT_HOME_VOLUME;

  private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
      = new BottomNavigationView.OnNavigationItemSelectedListener() {

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
      switch (item.getItemId()) {
        case R.id.navigation_settings:
          currentTab = item.getItemId();
          switchToSettings();
          return true;
        case R.id.navigation_map:
          currentTab = item.getItemId();
          switchToMap();
          return true;
        case R.id.navigation_event:
          currentTab = item.getItemId();
          switchToEvents();
          return true;
      }
      return false;
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    BottomNavigationView navView = findViewById(R.id.nav_view);
    navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    settingsView = findViewById(R.id.settings_view);
    mapView = findViewById(R.id.map_view);
    eventView = findViewById(R.id.events_view);
    geofenceLabel = findViewById(R.id.geofence_text);
    radiusSeekBar = findViewById(R.id.radius_seekbar);
    workVolumeSeekBar = findViewById(R.id.work_volume_seekbar);
    homeVolumeSeekBar = findViewById(R.id.home_volume_seekbar);
    homeVolumeValue = findViewById(R.id.home_volume_value);
    workVolumeValue = findViewById(R.id.work_volume_value);

    setupCallbacks();

    MapFragment mapFragment = (MapFragment) getFragmentManager()
        .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);

    currentTab = R.id.navigation_map;

    if (savedInstanceState != null) {
      currentTab = savedInstanceState.getInt("currentTab", R.id.navigation_map);
      double lat = savedInstanceState.getDouble("workLat", Double.NaN);
      double lng = savedInstanceState.getDouble("workLng", Double.NaN);
      if (!Double.isNaN(lat) && !Double.isNaN(lng)) {
        workLatLng = new LatLng(lat, lng);
      }
      radiusMeters = savedInstanceState.getInt("radiusMeters", DEFAULT_RADIUS_METERS);
    } else {
      loadFromPreferences();
    }

    navView.setSelectedItemId(currentTab);
    radiusSeekBar.setProgress(radiusMeters);
    setSettingsUIFromValues();
    setGeofenceLabel();
  }

  @Override
  protected void onPause() {
    super.onPause();
    saveToPreferences();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt("currentTab", currentTab);
    if (workLatLng != null) {
      outState.putDouble("workLat", workLatLng.latitude);
      outState.putDouble("workLng", workLatLng.longitude);
    }
    outState.putInt("radiusMeters", radiusMeters);
    outState.putInt("workVolume", workVolume);
    outState.putInt("homeVolume", homeVolume);
  }

  private void saveToPreferences() {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor editor = preferences.edit();
    if (workLatLng != null) {
      editor.putFloat("workLat", (float) workLatLng.latitude);
      editor.putFloat("workLng", (float) workLatLng.longitude);
    }
    editor.putInt("radiusMeters", radiusMeters);
    editor.putInt("homeVolume", homeVolume);
    editor.putInt("workVolume", workVolume);
    editor.apply();
  }

  private void loadFromPreferences() {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    float lat = preferences.getFloat("workLat", Float.NaN);
    float lng = preferences.getFloat("workLng", Float.NaN);
    if (!Float.isNaN(lat) && !Float.isNaN(lng)) {
      workLatLng = new LatLng(lat, lng);
    }
    radiusMeters = preferences.getInt("radiusMeters", DEFAULT_RADIUS_METERS);
    workVolume = preferences.getInt("workVolume", DEFAULT_WORK_VOLUME);
    homeVolume = preferences.getInt("workVolume", DEFAULT_HOME_VOLUME);
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
      mMap.setMyLocationEnabled(true);
    } else {
      checkLocationPermission();
    }
    if (workLatLng != null) {
      setMapMarkers();
      mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(workLatLng, DEFAULT_ZOOM));
    }
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode,
      @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    // If request is cancelled, the result arrays are empty.
    if (requestCode == MY_PERMISSIONS_REQUEST_FINE_LOCATION) {
      if (grantResults.length > 0
          && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        // permission was granted, yay!
        if (mMap != null) {
          mMap.setMyLocationEnabled(true);
          mMap.setOnMyLocationButtonClickListener(this);
          mMap.setOnMyLocationClickListener(this);
        }
      } else {
        if (mMap != null) {
          mMap.setMyLocationEnabled(false);
        }
      }
    }
  }

  @Override
  public boolean onMyLocationButtonClick() {
    centerMapOnUserLocation();
    return true;
  }

  @Override
  public void onMyLocationClick(Location location) {

  }

  private void setupCallbacks() {
    Button setLocationButton = findViewById(R.id.set_location_button);
    setLocationButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        onSetLocationClick();
      }
    });

    radiusSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        radiusMeters = i;
        setGeofenceLabel();
        setMapMarkers();
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        setGeofenceLabel();
        setMapMarkers();
      }
    });

    // Get the granularity of the ringer volume.
    AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    int maxVol = am.getStreamMaxVolume(AudioManager.STREAM_RING);
    // TODO(andrewr): there's a getStreamMinVolume in API 28. Assume 0 for now.
    homeVolumeSeekBar.setMin(1);
    homeVolumeSeekBar.setMax(maxVol);
    workVolumeSeekBar.setMin(1);
    workVolumeSeekBar.setMax(maxVol);
    if (homeVolume > maxVol) {
      homeVolume = maxVol;
    }
    if (workVolume > maxVol) {
      workVolume = maxVol;
    }

    homeVolumeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        homeVolume = i;
        setSettingsUIFromValues();
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    workVolumeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        workVolume = i;
        setSettingsUIFromValues();
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    Button testWorkVolumeButton = findViewById(R.id.work_volume_test_button);
    testWorkVolumeButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        setRingVolume(workVolume);
      }
    });

    Button testHomeVolumeButton = findViewById(R.id.home_volume_test_button);
    testHomeVolumeButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        setRingVolume(homeVolume);
      }
    });
  }

  private void setRingVolume(int volume) {
    try {
      AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
      if (am != null) {
        Toast.makeText(this,
            String.format(Locale.getDefault(), "newvol: %d", volume),
            Toast.LENGTH_SHORT).show();
        am.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
      }
    } catch (SecurityException e) {
      Toast.makeText(this, "SecurityException " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  private void setSettingsUIFromValues() {
    workVolumeSeekBar.setProgress(workVolume);
    homeVolumeSeekBar.setProgress(homeVolume);
    homeVolumeValue.setText(String.format(Locale.getDefault(), "%3d", homeVolume));
    workVolumeValue.setText(String.format(Locale.getDefault(), "%3d", workVolume));
  }

  private void setGeofenceLabel() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format(Locale.getDefault(), "Radius: %5d", radiusMeters));
    if (workLatLng != null) {
      sb.append(String.format(
          Locale.getDefault(), " (%.3f, %.3f)",
          workLatLng.latitude,
          workLatLng.longitude));
    }
    geofenceLabel.setText(sb.toString());
  }

  private void onSetLocationClick() {
    if (mMap == null) {
      return;
    }
    workLatLng = mMap.getCameraPosition().target;
    setMapMarkers();
    setGeofenceLabel();
  }

  /**
   * Add or update the marker and radius disc on the map.
   */
  private void setMapMarkers() {
    if (mMap == null || workLatLng == null) {
      return;
    }

    if (workMarker != null) {
      workMarker.remove();
    }
    workMarker = mMap.addMarker(new MarkerOptions().position(workLatLng).title("Work"));

    if (radiusCircle != null) {
      radiusCircle.remove();
    }
    radiusCircle = mMap.addCircle(
        new CircleOptions()
            .center(workLatLng)
            .radius(radiusMeters)
            .fillColor(Color.argb(60, 255, 128, 128))
            .strokeColor(Color.argb(255, 255, 128, 128)));

  }

  private void centerMapOnUserLocation() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
      fusedLocationClient.getLastLocation()
          .addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
              // Got last known location. In some rare situations this can be null.
              if (location != null && mMap != null) {
                // Logic to handle location object
                CameraUpdate moveCam = CameraUpdateFactory
                    .newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
                mMap.moveCamera(moveCam);
              }
            }
          });
    }
  }

  private void switchToSettings() {
    settingsView.setVisibility(View.VISIBLE);
    mapView.setVisibility(View.GONE);
    eventView.setVisibility(View.GONE);
  }

  private void switchToMap() {
    settingsView.setVisibility(View.GONE);
    mapView.setVisibility(View.VISIBLE);
    eventView.setVisibility(View.GONE);
  }

  private void switchToEvents() {
    settingsView.setVisibility(View.GONE);
    mapView.setVisibility(View.GONE);
    eventView.setVisibility(View.VISIBLE);
  }

  private void checkLocationPermission() {
    // Permission is not granted
    // Should we show an explanation?
    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
        Manifest.permission.ACCESS_FINE_LOCATION)) {
      // Show an explanation to the user *asynchronously* -- don't block
      // this thread waiting for the user's response! After the user
      // sees the explanation, try again to request the permission.
    } else {
      // No explanation needed; request the permission
      ActivityCompat.requestPermissions(this,
          new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
          MY_PERMISSIONS_REQUEST_FINE_LOCATION);
    }
  }

}
