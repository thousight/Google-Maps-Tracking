package www.markwen.space.google_maps_tracking.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import www.markwen.space.google_maps_tracking.MapsActivity;
import www.markwen.space.google_maps_tracking.R;

/**
 * Created by markw on 4/13/2017.
 */

public class RecordFragment extends Fragment {
    Button recordBtn, stopBtn;
    MapsActivity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MapsActivity)getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.record_action_layout, container, false);

        recordBtn = (Button)view.findViewById(R.id.recordButton);
        stopBtn = (Button)view.findViewById(R.id.stopButton);
        stopBtn.setVisibility(View.GONE);
        stopBtn.setAlpha(0f);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cross fade animation
                recordBtn.setVisibility(View.GONE);
                recordBtn.animate()
                        .alpha(0f)
                        .setDuration(300);
                stopBtn.setVisibility(View.VISIBLE);
                stopBtn.animate()
                        .alpha(1f)
                        .setDuration(300);
                Snackbar.make(v, "Start recording location, press stop to save it", Snackbar.LENGTH_LONG).show();

                // Start recording location data
                activity.startRecording();
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cross fade animation
                stopBtn.setVisibility(View.GONE);
                stopBtn.animate()
                        .alpha(0f)
                        .setDuration(300);
                recordBtn.setVisibility(View.VISIBLE);
                recordBtn.animate()
                        .alpha(1f)
                        .setDuration(300);

                // Stop recording location data and store it in DB
                activity.stopRecording();
            }
        });

        return view;
    }
}
