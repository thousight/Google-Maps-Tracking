package www.markwen.space.google_maps_tracking.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import www.markwen.space.google_maps_tracking.R;

/**
 * Created by Mark Wen on 4/22/2017.
 */

public class GridViewAdapter extends BaseAdapter {

    private ArrayList<Record> records = new ArrayList<>();
    private Context context;

    public GridViewAdapter(ArrayList<Record> records, Context context) {
        this.records = records;
        this.context = context;
    }

    @Override
    public int getCount() {
        return records.size();
    }

    @Override
    public Object getItem(int i) {
        return records.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.record_item, viewGroup, false);
        }

        TextView recordName = (TextView)view.findViewById(R.id.recordName);
        TextView recordDate = (TextView)view.findViewById(R.id.recordDate);
        TextView recordCity = (TextView)view.findViewById(R.id.recordCity);
        ImageView recordImage = (ImageView)view.findViewById(R.id.recordImage);

        recordName.setText(records.get(i).getName());
        recordCity.setText(records.get(i).getCity());
        SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        recordDate.setText(fmt.format(records.get(i).getDate()));
        if (records.get(i).getImage() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(records.get(i).getImage(), 0, records.get(i).getImage().length);
            recordImage.setImageBitmap(bitmap);
        }

        return view;
    }
}
