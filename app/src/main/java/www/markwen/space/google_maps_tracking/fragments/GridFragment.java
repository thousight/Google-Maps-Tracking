package www.markwen.space.google_maps_tracking.fragments;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;

import www.markwen.space.google_maps_tracking.MapsActivity;
import www.markwen.space.google_maps_tracking.R;
import www.markwen.space.google_maps_tracking.components.DBHelper;
import www.markwen.space.google_maps_tracking.components.Record;

/**
 * Created by markw on 4/13/2017.
 */

public class GridFragment extends Fragment {

    private GridView gridView;
    private SQLiteDatabase db;
    private DBHelper dbHelper;
    private ArrayList<Record> allData = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.records_layout, container, false);
        gridView = (GridView) view.findViewById(R.id.gridview);

        db = ((MapsActivity)getActivity()).getDB();
        dbHelper = ((MapsActivity)getActivity()).getDBHelper();
        allData = dbHelper.getAllRecords(db, getContext());

        // TODO: Get all records from DB and list them in GridView
        return view;
    }
}
