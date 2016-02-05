package com.guilermetell.pickmeup;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds
    private static final String TAG = HomeActivity.class.getSimpleName();

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Button PickMeBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        PickMeBtn = (Button) findViewById(R.id.pick_me);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //http://stackoverflow.com/questions/32615013/is-it-available-to-set-checkselfpermission-on-minimum-sdk-23
        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        locationListener = new MyLocationListener();
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MINIMUM_TIME_BETWEEN_UPDATES,
                MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
                locationListener
        );


        PickMeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(HomeActivity.this,"PICK ME!", Toast.LENGTH_LONG).show();
                showCurrentLocation();
            }
        });
    }

    protected void showCurrentLocation() {

        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        String cityName = null;

        if (location != null) {
            cityName = getCityNameFromCoordinates(location);
            String message = String.format(
//                    "https://www.google.com/maps/@%1$s,%2$s",
                    "http://maps.google.com/maps?daddr=%1$s,%2$s",
                    location.getLongitude(), location.getLatitude()
            );
            onClickWhatsApp(message);
            sendSMS(message, "+5592000000000");
            Toast.makeText(HomeActivity.this, message,
                    Toast.LENGTH_LONG).show();
        } else {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                cityName = getCityNameFromCoordinates(location);
                String message = String.format(
                        //"https://www.google.com/maps/@%1$s,%2$s",
                        "http://maps.google.com/maps?daddr=%1$s,%2$s",
                        location.getLongitude(), location.getLatitude()
                );
                onClickWhatsApp(message);
                sendSMS(message, "+5592000000000");
                Toast.makeText(HomeActivity.this, message,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(HomeActivity.this, "location == null",
                        Toast.LENGTH_LONG).show();
            }
        }

    }

    public String getCityNameFromCoordinates(Location location) {
        String cityName = null;
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
            if (addresses.size() > 0) {
                System.out.println(addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;
    }

    public void onClickWhatsApp(String message) {

        PackageManager pm=getPackageManager();
        try {

            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");

            PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            //Check if package exists or not. If not then code
            //in catch block will be called
            waIntent.setPackage("com.whatsapp");

            waIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(waIntent, "Share with"));

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "WhatsApp not Installed", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    private class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
//            String message = String.format(
//                    "New Location \n Longitude: %1$s \n Latitude: %2$s",
//                    location.getLongitude(), location.getLatitude()
//            );
//            Toast.makeText(HomeActivity.this, message, Toast.LENGTH_LONG).show();
//            Toast.makeText(
//                    getBaseContext(),
//                    "Location changed: Lat: " + location.getLatitude() + " Lng: "
//                            + location.getLongitude(), Toast.LENGTH_SHORT).show();
            String longitude = "Longitude: " + location.getLongitude();
            Log.d(TAG, longitude);
            String latitude = "Latitude: " + location.getLatitude();
            Log.d(TAG, latitude);

        }

        public void onStatusChanged(String s, int i, Bundle b) {
            Log.d(TAG, "Provider status changed");
        }

        public void onProviderDisabled(String s) {
            Log.d(TAG, "Provider disabled by the user. GPS turned off");
        }

        public void onProviderEnabled(String s) {
            Log.d(TAG, "Provider enabled by the user. GPS turned on");
        }

    }

    public void sendSMS(String message, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phoneNumber));
        intent.putExtra("sms_body", message);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
