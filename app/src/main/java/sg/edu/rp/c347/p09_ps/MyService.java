package sg.edu.rp.c347.p09_ps;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class MyService extends Service {

    boolean started;

    FusedLocationProviderClient client;
    LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;

    double lat, lng;

    SharedPreferences setPrefs;
    SharedPreferences.Editor myEdit;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {

        client = LocationServices.getFusedLocationProviderClient(this);
        setPrefs = getSharedPreferences("lastLocation", MODE_PRIVATE);
        myEdit  = setPrefs.edit();

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (checkPermission() == true) {

            if (started == false) {

                started = true;

                mLocationRequest = LocationRequest.create();
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationRequest.setInterval(10000);
                mLocationRequest.setFastestInterval(5000);
                mLocationRequest.setSmallestDisplacement(100);

                mLocationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult != null) {
                            Location data = locationResult.getLastLocation();
                            lat = data.getLatitude();
                            lng = data.getLongitude();
                            myEdit.putString("lat", String.valueOf(lat));
                            myEdit.putString("lng", String.valueOf(lng));
                            try {
                                String folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Folder";
                                File targetFile = new File(folderLocation, "LastLocation.txt");
                                FileWriter writer = new FileWriter(targetFile, true);
                                writer.write(lat + " , " + lng + "\n");
                                writer.flush();
                                writer.close();
                            } catch (Exception e) {
                                Toast.makeText(MyService.this, "Failed to write!", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    }
                };

                client.requestLocationUpdates(mLocationRequest, mLocationCallback, null);

                Toast.makeText(getApplicationContext(), "Service has started running", Toast.LENGTH_SHORT).show();

            }else {
                Toast.makeText(getApplicationContext(), "Service is already running.....", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        client.removeLocationUpdates(mLocationCallback);
        Toast.makeText(getApplicationContext(), "Service has stop", Toast.LENGTH_SHORT).show();
        myEdit.commit();

        super.onDestroy();
    }

    private boolean checkPermission() {
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
                MyService.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MyService.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED
                || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

}
