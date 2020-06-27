package com.robusta.photoweather.ui.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.robusta.photoweather.R;
import com.robusta.photoweather.data.models.WeatherInfo;
import com.robusta.photoweather.ui.history.HistoryActivity;
import com.robusta.photoweather.utils.Utilities;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity implements MainContractor.View {
    private Button getHistory;
    private TextView main, description, temp;
    private ImageView img;
    private SwipeRefreshLayout refreshLayout;
    private double longitude;
    private double latitude;
    private static final String TAG = "MainActivity";
    private final int REQUEST_IMAGE_CAPTURE = 1053;
    private final int LOCATION_PERMISSION = 1052;
    private final int WRITE_PERMISSION = 1054;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init views
        Button takePhoto = findViewById(R.id.take_a_photo);
        Button addToHistory = findViewById(R.id.add_to_history);
        getHistory = findViewById(R.id.get_history);
        main = findViewById(R.id.main);
        description = findViewById(R.id.description);
        temp = findViewById(R.id.temp);
        img = findViewById(R.id.img);
        refreshLayout = findViewById(R.id.swipe);
        rootView = findViewById(R.id.root_view);

        MainContractor.Presenter presenter = new MainPresenter(this);

        //set data
        setLatLng();
        if (Utilities.isNetworkConnected(this)) {
            showProgress();
            presenter.getWeatherInfo(latitude, longitude);
        } else {
            Toast.makeText(this, R.string.no_connection, Toast.LENGTH_LONG).show();
        }

        //refresh listener
        refreshLayout.setOnRefreshListener(() -> {
            setLatLng();
            if (Utilities.isNetworkConnected(this)) {
                showProgress();
                presenter.getWeatherInfo(latitude, longitude);
            } else {
                Toast.makeText(this, R.string.no_connection, Toast.LENGTH_LONG).show();
            }
        });

        //take photo listener
        takePhoto.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //requesting permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.CAMERA
                        },
                        REQUEST_IMAGE_CAPTURE);
                return;
            } else {
                cameraIntent();
            }
        });

        //add to history listener
        addToHistory.setOnClickListener(v -> {
            //checking permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //requesting permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        WRITE_PERMISSION);
                return;
            } else {
                store(rootView);
            }
        });

        //get history listener
        getHistory.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
        });
    }

    @Override
    public void showProgress() {
        refreshLayout.setRefreshing(true);
    }

    @Override
    public void hideProgress() {
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void setLatLng() {
        if (isLocationEnabled()) {
            LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //requesting permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        LOCATION_PERMISSION);
                return;
            } else {
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                try {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                } catch (NullPointerException e) {

                }
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
            }

        } else {
            Toast.makeText(this, R.string.enable_your_location, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void setWeatherInfo(WeatherInfo weatherInfo) {
        hideProgress();
        main.setText(weatherInfo.getWeather().get(0).getMain());
        description.setText(weatherInfo.getWeather().get(0).getDescription());
        temp.setText(String.valueOf(weatherInfo.getMain().getTemp()));
    }

    @Override
    public void setWeatherInfoError(String errorMsg) {
        hideProgress();
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
    }

    // location listener to track location updates
    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            Log.d(TAG, "onLocationChanged: " + latitude + longitude);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    //handling permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted.
                    setLatLng();
                } else {
                    // Permission denied
                    Toast.makeText(this, R.string.you_have_to_give_location_permission, Toast.LENGTH_LONG).show();
                }
                return;
            }
            case REQUEST_IMAGE_CAPTURE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted.
                    cameraIntent();
                } else {
                    // Permission denied
                    Toast.makeText(this, R.string.you_have_to_accept_cam_permission, Toast.LENGTH_LONG).show();
                }
                return;
            }
            case WRITE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted.
                    store(rootView);
                } else {
                    // Permission denied
                    Toast.makeText(this, R.string.give_write, Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    //check if location enabled or not
    public Boolean isLocationEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
// This is new method provided in API 28
            LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
// This is Deprecated in API 28
            int mode = Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            img.setImageBitmap(imageBitmap);
        }
    }

    public Bitmap getScreenShot(View view) {
        view.setDrawingCacheEnabled(true);
        RelativeLayout mLayout = (RelativeLayout) findViewById(R.id.root_view);
        int totalHeight = mLayout.getChildAt(0).getHeight();
        int totalWidth = mLayout.getChildAt(0).getWidth();
        mLayout.layout(0, 0, totalWidth, totalHeight);
        mLayout.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(mLayout.getDrawingCache());
        mLayout.setDrawingCacheEnabled(false);
        return bitmap;
    }

    public void store(View view) {
        final String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PhotoWeatherHistory";
        File dir = new File(dirPath);
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(dirPath, System.currentTimeMillis() + "_weatherphoto.png");
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            getScreenShot(view).compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    
}