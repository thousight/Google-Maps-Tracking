package www.markwen.space.google_maps_tracking;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.rd.PageIndicatorView;
import com.rd.animation.AnimationType;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import www.markwen.space.google_maps_tracking.components.CustomViewPager;
import www.markwen.space.google_maps_tracking.components.DBHelper;
import www.markwen.space.google_maps_tracking.components.FragmentPagerItemAdapter;
import www.markwen.space.google_maps_tracking.components.Record;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        SensorEventListener,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static GoogleMap mMap;
    private static Location currentLocation;
    private static LocationRequest locationRequest;
    private FusedLocationProviderApi fusedLocationProvider;
    private GoogleApiClient googleApiClient;
    private static Sensor magnetometer, accelerometer;
    private static SensorManager sensorManager;
    private static final int LOCATIONS_GRANTED = 1;
    private static int accentColor;
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    CustomViewPager viewPager;
    FrameLayout frameLayout;
    AppCompatCheckBox satellite;
    ImageView compass;
    PageIndicatorView indicator;
    private float currentDegree = 0f;
    float[] mGravity;
    float[] mGeomagnetic;
    private boolean isRecording = false;
    private ArrayList<LatLng> recordedPoints = new ArrayList<>();
    private static ArrayList<Polyline> linesOnMap = new ArrayList<>();
    private Record record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Check permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, LOCATIONS_GRANTED);
            }
        }

        // Initialize views
        viewPager = (CustomViewPager) findViewById(R.id.pager);
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        satellite = (AppCompatCheckBox) findViewById(R.id.satellite);
        compass = (ImageView) findViewById(R.id.compass);
        indicator = (PageIndicatorView) findViewById(R.id.pageIndicatorView);
        accentColor = ResourcesCompat.getColor(getResources(), R.color.colorAccent, null);

        // Set up page indicator
        indicator.setCount(2);
        indicator.setViewPager(viewPager);
        indicator.setSelectedColor(ResourcesCompat.getColor(getResources(), R.color.pagerIndicatorSelected, null));
        indicator.setUnselectedColor(ResourcesCompat.getColor(getResources(), R.color.pagerIndicatorUnselected, null));
        indicator.setAnimationType(AnimationType.WORM);
        indicator.setAnimationDuration(300);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setLayoutDimentions(mapFragment); // Set elements' heights based on screen sizes

        // Initialize FusedLocationProviderApi
        fusedLocationProvider = LocationServices.FusedLocationApi;
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

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
                    currentLocation = fusedLocationProvider.getLastLocation(googleApiClient); // Preferred
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
        checkboxParams.setMargins(getStatusBarHeight() * 3, (int) (getStatusBarHeight() * 1.5), getStatusBarHeight() * 3, 0);

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
                currentDegree = azimut - 20; // -20 is to correct the image angle
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // Start recording location data
    public void startRecording() {
        isRecording = true;
        viewPager.setPagingEnabled(false);
        indicator.setVisibility(View.INVISIBLE);
        record = new Record();
        recordedPoints = new ArrayList<>();

        // Clear Polylines on map
        for(int i = 0; i < linesOnMap.size(); i++) {
            linesOnMap.get(i).remove();
            linesOnMap.remove(i);
        }
        mMap.clear();
    }

    // Stop recording location data and store it in DB
    public void stopRecording() {
        isRecording = false;
        viewPager.setPagingEnabled(true);
        indicator.setVisibility(View.VISIBLE);

        MaterialDialog saveDialog = new MaterialDialog.Builder(this)
                .title("Save Location")
                .customView(R.layout.save_dialog_layout, false)
                .positiveText("Save")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        EditText recordName = (EditText)dialog.getView().findViewById(R.id.save_location_name);
                        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                        try {
                            if (recordedPoints.size() > 0) {
                                List<Address> addresses = geocoder.getFromLocation(recordedPoints.get(0).latitude, recordedPoints.get(0).longitude, 1);
                                if (addresses != null && addresses.size() > 0) {
                                    record.setCity(addresses.get(0).getLocality());
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        record.setName(recordName.getText().toString());
                        record.setPoints(recordedPoints);
                        dbHelper.saveRecord(db, record);
                        Toast.makeText(MapsActivity.this, "Record saved", Toast.LENGTH_SHORT).show();
                    }
                })
                .negativeText("Cancel")
                .negativeColor(ResourcesCompat.getColor(getResources(), R.color.colorNagative, null))
                .build();
        final EditText recordName = (EditText)saveDialog.getView().findViewById(R.id.save_location_name);
        final ImageButton clearButton = (ImageButton) saveDialog.getView().findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordName.setText("");
            }
        });
        recordName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals("")) {
                    clearButton.setVisibility(View.INVISIBLE);
                } else {
                    clearButton.setVisibility(View.VISIBLE);
                }
            }
        });
        recordName.setText(new Date().toString());
        saveDialog.show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProvider.requestLocationUpdates(googleApiClient, locationRequest, this);
        // Obtain currentLocation
        currentLocation = fusedLocationProvider.getLastLocation(googleApiClient);
        centerCamera();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (isRecording) {
            LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());
            moveCameraTo(newPoint);
            recordedPoints.add(newPoint);
            if (recordedPoints.size() > 1) {
                createDashedLine(recordedPoints.get(recordedPoints.size()-2), recordedPoints.get(recordedPoints.size()-1));
            }
        }
    }

    public static void createDashedLine(LatLng latLngOrig, LatLng latLngDest){
        double difLat = latLngDest.latitude - latLngOrig.latitude;
        double difLng = latLngDest.longitude - latLngOrig.longitude;

        double zoom = mMap.getCameraPosition().zoom;

        double divLat = difLat / (zoom * 2);
        double divLng = difLng / (zoom * 2);

        LatLng tmpLatOri = latLngOrig;

        for(int i = 0; i < (zoom * 2); i++){
            LatLng loopLatLng = tmpLatOri;

            if(i > 0){
                loopLatLng = new LatLng(tmpLatOri.latitude + (divLat * 0.25f), tmpLatOri.longitude + (divLng * 0.25f));
            }

            linesOnMap.add(mMap.addPolyline(new PolylineOptions()
                    .add(loopLatLng)
                    .add(new LatLng(tmpLatOri.latitude + divLat, tmpLatOri.longitude + divLng))
                    .color(accentColor)));

            tmpLatOri = new LatLng(tmpLatOri.latitude + divLat, tmpLatOri.longitude + divLng);
        }
    }
}
