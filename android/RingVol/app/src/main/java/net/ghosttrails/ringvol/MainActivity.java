package net.ghosttrails.ringvol;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.libraries.maps.model.LatLng;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
    OnMyLocationButtonClickListener,
    OnMyLocationClickListener {

  private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1337;

  private FusedLocationProviderClient fusedLocationClient;

  private View settingsView;
  private View mapView;
  private View eventView;
  private BottomNavigationView navView;
  private GoogleMap mMap;
  private int currentTab;

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
    navView = findViewById(R.id.nav_view);
    navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    settingsView = findViewById(R.id.settings_view);
    mapView = findViewById(R.id.map_view);
    eventView = findViewById(R.id.events_view);

    MapFragment mapFragment = (MapFragment) getFragmentManager()
        .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);

    currentTab = R.id.navigation_map;

    if (savedInstanceState != null) {
      currentTab = savedInstanceState.getInt("currentTab", R.id.navigation_map);
    }

    navView.setSelectedItemId(currentTab);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt("currentTab", currentTab);
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
                CameraUpdate moveCam = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
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
