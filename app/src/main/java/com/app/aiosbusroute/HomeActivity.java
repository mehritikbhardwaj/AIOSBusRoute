package com.app.aiosbusroute;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.app.aiosbusroute.common.Constants;
import com.app.aiosbusroute.common.SuccessModel;
import com.app.aiosbusroute.common.Utilities;
import com.app.aiosbusroute.common.Utils;
import com.app.aiosbusroute.retrofit.ApiService;
import com.app.aiosbusroute.retrofit.RetroClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    ProgressDialog pd;
    LinearLayout llLayout;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationManager mLocationManager;
    String id = "", latitude = "", longitude = "";
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 1000 * 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        pd = new ProgressDialog(this);
        pd.setCancelable(false);
        llLayout = findViewById(R.id.llLayout);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        id = Utilities.getStringPref(this, Constants.BUS_ID, Constants.PREF_NAME);

      /*  if (Utils.isNetworkAvailable(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                checkPermissionAndGetLocation();
            } else getLocationData();
        } else {
            Toast.makeText(this, "Please check your internet connection!", Toast.LENGTH_SHORT).show();
        }

*/
        llLayout.setOnClickListener(view -> logout());

    }

    private void getLocationData() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                OnGPS();
            } else {
                getLocation();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //  getLocationData();
        handler.postDelayed(runnable = () -> {
            handler.postDelayed(runnable, delay);
            if (Utils.isNetworkAvailable(this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    checkPermissionAndGetLocation();
                } else getLocationData();
            } else {
                Toast.makeText(this, "Please check your internet connection!", Toast.LENGTH_SHORT).show();
            }
        }, delay);

    }

    private void callUpdateLocationAPI() {
        ApiService apiService = RetroClient.getApiService();

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("apiusername", Constants.API_USER_NAME);
        headers.put("apipassword", Constants.API_PASSWORD);
        headers.put("uid", "0");

        Call<SuccessModel> call = apiService.BusInserLocation(headers,
                Integer.valueOf(id), latitude, longitude);

        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(Call<SuccessModel> call, Response<SuccessModel> response) {

                if (response.isSuccessful()) {
                    SuccessModel model = response.body();

                    if (model.getStatus().equalsIgnoreCase("success")) {
                        Toast.makeText(HomeActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                    } else if (model.getStatus().equalsIgnoreCase("")) {
                        Toast.makeText(HomeActivity.this, "Invalid", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SuccessModel> call, Throwable t) {
                Log.e("ONFAILURE", t.toString());
            }
        });
    }

    private void logout() {
        Utilities.setStringPreference(this, Constants.IS_LOGGED_IN,
                "NO", Constants.PREF_NAME);

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }


    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermissionAndGetLocation();
            // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        } else {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                Location location = task.getResult();
                if (location != null) {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (location != null) {
                            latitude = String.valueOf(addresses.get(0).getLatitude());
                            longitude = String.valueOf(addresses.get(0).getLongitude());
                            Toast.makeText(this, String.valueOf(addresses.get(0).getLatitude()), Toast.LENGTH_SHORT).show();
                            callUpdateLocationAPI();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (location == null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Toast.makeText(HomeActivity.this, "null", Toast.LENGTH_SHORT).show();
                    } else find_Location(this);
                }
            });
        }

    }

    public void find_Location(Context con) {
        Log.d("Find Location", "in find_location");
        String location_context = Context.LOCATION_SERVICE;
        LocationManager locationManager = (LocationManager) con.getSystemService(location_context);
        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // checkPermissionAndGetLocation();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            }
            locationManager.requestLocationUpdates(provider, 1000, 0,
                    new LocationListener() {

                        public void onLocationChanged(Location location) {
                        }

                        public void onProviderDisabled(String provider) {
                        }

                        public void onProviderEnabled(String provider) {
                        }

                        public void onStatusChanged(String provider, int status,
                                                    Bundle extras) {
                        }
                    });
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());
                //addr = ConvertPointToLocation(latitude, longitude);
                //  String temp_c = SendToUrl(addr);
                Toast.makeText(con, latitude + " latitude\n" + longitude + "longitude", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                dialog.cancel();
            }
        }).setNegativeButton("No", (dialog, which) -> {
            OnGPS();
            dialog.cancel();
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void backGroundLocation() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable location Permission for all time").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(HomeActivity.this,
                            new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 44);
                }
                //  startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", (dialog, which) -> {
            OnGPS();
            dialog.cancel();
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void checkPermissionAndGetLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                OnGPS();
            } else {
                getLocation();
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 44:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "Please allow all the time location permission.",
                                    Toast.LENGTH_SHORT).show();
                            ActivityCompat.requestPermissions(HomeActivity.this,
                                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 44);
                        }
                    } else {
                        getLocation();
                    }
                } else {
                    //not granted
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }
                break;
            case 45:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        getLocation();
                    }
                } else {
                    //not granted
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ActivityCompat.requestPermissions(HomeActivity.this,
                                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 44);
                    } else getLocation();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


}


