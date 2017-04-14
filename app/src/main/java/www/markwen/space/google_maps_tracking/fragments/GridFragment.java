package www.markwen.space.google_maps_tracking.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import www.markwen.space.google_maps_tracking.R;

/**
 * Created by markw on 4/13/2017.
 */

public class GridFragment extends Fragment {

    GridView gridView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.records_layout, container, false);
        gridView = (GridView) view.findViewById(R.id.gridview);

        // TODO: Get all records from DB and list them in GridView
        return view;
    }
}
