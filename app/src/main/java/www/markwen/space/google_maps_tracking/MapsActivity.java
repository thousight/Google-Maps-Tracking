package www.markwen.space.google_maps_tracking;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATIONS_GRANTED = 1;
    Button recordBtn, stopBtn;
    LinearLayout btnWrapper;

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

        // Initialize Buttons
        initializeButtons();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setLayoutDimentions(mapFragment, btnWrapper); // Set elements' heights based on screen sizes

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
        updateMapUI(true);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATIONS_GRANTED: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateMapUI(true);
                }
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMapUI(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateMapUI(false);
    }

    private void initializeButtons() {
        btnWrapper = (LinearLayout)findViewById(R.id.buttonWrapper);
        recordBtn = (Button)findViewById(R.id.recordButton);
        stopBtn = (Button)findViewById(R.id.stopButton);
        stopBtn.setVisibility(View.GONE);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordBtn.setVisibility(View.GONE);
                stopBtn.setVisibility(View.VISIBLE);

            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordBtn.setVisibility(View.VISIBLE);
                stopBtn.setVisibility(View.GONE);

            }
        });
    }

    private void setLayoutDimentions(SupportMapFragment mapFragment, LinearLayout btnLayout) {
        // Get device display dimentions
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        // Set Map height to 3/4 of the display
        ViewGroup.LayoutParams mapParams = mapFragment.getView().getLayoutParams();
        // Removing heights of notification bar and bottom navigation
        mapParams.height = displayMetrics.heightPixels * 3 / 4 - 70;

        // Set Button Wrapper height to 1/4 of the display
        ViewGroup.LayoutParams wrapperParams = btnLayout.getLayoutParams();
        wrapperParams.height = displayMetrics.heightPixels / 4;
    }

    private void updateMapUI(boolean isLocationEnabled) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(isLocationEnabled);
            mMap.getUiSettings().setCompassEnabled(isLocationEnabled);
            mMap.getUiSettings().setMyLocationButtonEnabled(isLocationEnabled);
        }
    }
}
