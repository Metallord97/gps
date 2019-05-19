package com.example.gps_app;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_REQUEST_LOCATION = 1;
    private String providerId = LocationManager.GPS_PROVIDER;
    private Geocoder geo = null;
    private LocationManager locationManager = null;
    private static final int MIN_DIST = 20;
    private static final int MIN_PERIOD = 3000;

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateGUI(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            // il GPS è attivo sul dispositivo
            updateText(R.id.tvEnabled2, "TRUE");
        }

        @Override
        public void onProviderDisabled(String provider) {
            // il GPS non è attivo sul dispositivo
            updateText(R.id.tvEnabled2, "FALSE");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkLocationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        geo = new Geocoder(this, Locale.getDefault());
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null)
                updateGUI(location);
            if (locationManager != null && locationManager.isProviderEnabled(providerId))
                updateText(R.id.tvEnabled2, "TRUE");
            else
                updateText(R.id.tvEnabled2, "FALSE");
            locationManager.requestLocationUpdates(providerId, MIN_PERIOD, MIN_DIST, locationListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (locationManager != null && locationManager.isProviderEnabled(providerId))
                locationManager.removeUpdates(locationListener);
        }
    }

    private void updateGUI(Location location){
        Date timestamp = new Date(location.getTime());
        updateText(R.id.tvTimestamp2, timestamp.toString());
        double latitude = location.getLatitude();
        updateText(R.id.tvLatitude2, String.valueOf(latitude));
        double longitude = location.getLongitude();
        updateText(R.id.tvLongitude2, String.valueOf(longitude));
        new AddressSolver().execute(location);
    }

    private void updateText(int id, String text){
        TextView textView = findViewById(id);
        textView.setText(text);
    }

    public class AddressSolver extends AsyncTask<Location, Void, String> {

        @Override
        protected String doInBackground(Location... params) {
            Location pos = params[0];
            double latitude = pos.getLatitude();
            double longitude = pos.getLongitude();
            List<Address> addresses = null;
            try {
                addresses = geo.getFromLocation(latitude, longitude, 1);
            }
            catch (IOException e) {}
            if (addresses != null) {
                if (addresses.isEmpty()) {
                    return null;
                } else {
                    if (addresses.size() > 0) {
                        StringBuffer address = new StringBuffer();
                        Address tmp = addresses.get(0);
                        for (int y = 0; y < tmp.getMaxAddressLineIndex(); y++)
                            address.append(tmp.getAddressLine(y) + "\n");
                        return  address.toString();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null)
                updateText(R.id.tvLocation2, result);
            else
                updateText(R.id.tvLocation2, "N.A.");
        }
    }

    public boolean checkLocationPermission(){
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permissione)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSION_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSION_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(providerId , MIN_PERIOD , MIN_DIST, locationListener);
                    }
                } else {
                    // permission denied, disable the fuctionality that depends on this permissione
                    Toast.makeText(this, "Permessi negati, l'app ha bisogno dei permessi per funzionare",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
