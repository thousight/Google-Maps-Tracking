package www.markwen.space.google_maps_tracking;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.rd.PageIndicatorView;
import com.rd.animation.AnimationType;

import www.markwen.space.google_maps_tracking.components.DBHelper;
import www.markwen.space.google_maps_tracking.components.FragmentPagerItemAdapter;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener {

    private static GoogleMap mMap;
    private static Location currentLocation;
    private static LocationManager locationManager;
    private static Sensor magnetometer, accelerometer;
    private static SensorManager sensorManager;
    private static final int LOCATIONS_GRANTED = 1;
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    ViewPager viewPager;
    FrameLayout frameLayout;
    AppCompatCheckBox satellite;
    ImageView compass;
    PageIndicatorView indicator;
    private float currentDegree = 0f;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Check permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATIONS_GRANTED);
        }

        // Initialize views
        viewPager = (ViewPager) findViewById(R.id.pager);
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        satellite = (AppCompatCheckBox) findViewById(R.id.satellite);
        compass = (ImageView) findViewById(R.id.compass);
        indicator = (PageIndicatorView) findViewById(R.id.pageIndicatorView);

        // Set up page indicator
        indicator.setCount(2);
        indicator.setViewPager(viewPager);
        indicator.setSelectedColor(R.color.indicatorSelected);
        indicator.setUnselectedColor(R.color.indicatorUnselected);
        indicator.setAnimationType(AnimationType.WORM);
        indicator.setAnimationDuration(300);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setLayoutDimentions(mapFragment); // Set elements' heights based on screen sizes

        // Obtain current location
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); // Preferred
            if (currentLocation == null) {
                currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }

        // Initialize magnetometer for compass
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Initialize SQLiteDB
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // GoogleMaps configs
        mMap.setPadding(0, getStatusBarHeight(), 0, 0); // Remove overlay of status bar
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(false);

        centerCamera();

        // Satellite checkbox functions
        satellite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    satellite.setTextColor(Color.parseColor("#ffffff"));
                    compass.setColorFilter(Color.parseColor("#ffffff"));
                } else {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    satellite.setTextColor(Color.parseColor("#333333"));
                    compass.setColorFilter(Color.parseColor("#333333"));
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATIONS_GRANTED: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); // Preferred
                    if (currentLocation == null) {
                        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                    updateMapUI(true);
                    centerCamera();
                }
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMapUI(true);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateMapUI(false);
        sensorManager.unregisterListener(this, magnetometer);
        sensorManager.unregisterListener(this, accelerometer);
    }

    private void setLayoutDimentions(SupportMapFragment mapFragment) {
        // Get device display dimentions
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        // Set Map height to 2/3 of the display
        ViewGroup.LayoutParams mapParams = mapFragment.getView().getLayoutParams();
        mapParams.height = displayMetrics.heightPixels * 2 / 3;

        // Set Framelayout and Checkbox height to 2/3 of the display
        ViewGroup.LayoutParams frameParams = frameLayout.getLayoutParams();
        frameParams.height = displayMetrics.heightPixels * 2 / 3;

        // Set Checkbox margin
        ViewGroup.MarginLayoutParams checkboxParams = (ViewGroup.MarginLayoutParams) satellite.getLayoutParams();
        checkboxParams.setMargins(getStatusBarHeight() * 3, (int)(getStatusBarHeight() * 1.5), getStatusBarHeight() * 3, 0);

        // Set Button Wrapper height to 1/4 of the display
        ViewGroup.LayoutParams wrapperParams = viewPager.getLayoutParams();
        wrapperParams.height = displayMetrics.heightPixels / 3;
    }

    // Optimize battery usage, called when user is using the app
    private void updateMapUI(boolean isLocationEnabled) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && mMap != null) {
            mMap.setMyLocationEnabled(isLocationEnabled);
            mMap.getUiSettings().setCompassEnabled(isLocationEnabled);
            mMap.getUiSettings().setMyLocationButtonEnabled(isLocationEnabled);
        }
    }

    // To help optimize code
    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    // Allow other fragments to use to move camera of the Map
    public static void moveCameraTo(LatLng location) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)      // Sets the center of the map to location user
                .zoom(17)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void centerCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (currentLocation != null) {
            // Zoom camera to center around current location
            moveCameraTo(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        }
    }

    float[] mGravity;
    float[] mGeomagnetic;

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Rotate compass
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                int azimut = (int) Math.round(Math.toDegrees(orientation[0]));
                RotateAnimation animation = new RotateAnimation(
                currentDegree, azimut,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(200);
                animation.setFillAfter(true);
                compass.startAnimation(animation);
                currentDegree = azimut - 10f; // -10f is to correct the image angle
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // Start recording location data
    public void startRecording() {
        String points = "";
        isRecording = true;

    }

    // Stop recording location data and store it in DB
    public void stopRecording() {
        isRecording = false;

    }
}
