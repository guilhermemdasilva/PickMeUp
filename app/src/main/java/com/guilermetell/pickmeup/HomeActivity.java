package com.guilermetell.pickmeup;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {

    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds
    private static final int INITIAL_REQUEST = 2110;
    private static final String TAG = HomeActivity.class.getSimpleName();
    private static final String SELECTED_CONTACT_NUMBER = "SELECTED_CONTACT_NUMBER";
    private static final String UNKNOWN_CONTACT_NUMBER = "UNKNOWN_CONTACT_NUMBER";
    private static final String MSG_TYPE_SWITCH = "MSG_TYPE_SWITCH";

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Button PickMeBtn;
    protected Switch MsgTypeSwitch;

    private static boolean isWhatsApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        PickMeBtn = (Button) findViewById(R.id.pick_me);
        MsgTypeSwitch = (Switch) findViewById(R.id.msg_type_switch);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
                return;
            }
        }
        locationListener = new MyLocationListener();
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MINIMUM_TIME_BETWEEN_UPDATES,
                MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
                locationListener
        );

        enableGPS();

        isWhatsApp = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean(MSG_TYPE_SWITCH, false);
        MsgTypeSwitch.setChecked(isWhatsApp);

        MsgTypeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putBoolean(MSG_TYPE_SWITCH, isChecked).commit();
                isWhatsApp = isChecked;
            }
        });

        PickMeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCurrentLocation();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected void showCurrentLocation() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
                return;
            }
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        String SelectedContactNumber = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(SELECTED_CONTACT_NUMBER, UNKNOWN_CONTACT_NUMBER);

        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location == null) {
                enableGPS();
                return;
            }
        }
        String message = String.format(
                "http://maps.google.com/maps?daddr=%1$s,%2$s",
                location.getLatitude(), location.getLongitude()
        );
        if(isWhatsApp) {
            onClickWhatsApp(message);
        } else {
            if(SelectedContactNumber.equals(UNKNOWN_CONTACT_NUMBER)) {
                pickContact();
            } else {
                sendSMS(message, SelectedContactNumber);
            }
        }
    }

    public void enableGPS() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(HomeActivity.this, "Please, turn on you Location Settings",
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    public void onClickWhatsApp(String message) {
        PackageManager pm = getPackageManager();
        try {
            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            whatsappIntent.setType("text/plain");
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            whatsappIntent.setPackage("com.whatsapp");
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(whatsappIntent, "Share with"));
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "WhatsApp not Installed", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            location.getLongitude();
            location.getLatitude();
            Log.d(TAG, "Provider Location changed");
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

    private void pickContact() {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(pickContactIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                Cursor c = null;
                try {
                    c = getContentResolver().query(uri, new String[]{
                                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                                    ContactsContract.CommonDataKinds.Phone.TYPE },
                            null, null, null);
                    if (c != null && c.moveToFirst()) {
                        String SelectedContactNumber = c.getString(0);
                        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putString(SELECTED_CONTACT_NUMBER, SelectedContactNumber).commit();
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_contact) {
            pickContact();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
