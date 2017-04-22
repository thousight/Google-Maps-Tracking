package www.markwen.space.google_maps_tracking.components;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

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
    private SQLiteDatabase db;
    private DBHelper helper;

    public GridViewAdapter(ArrayList<Record> records, Context context, SQLiteDatabase db, DBHelper helper) {
        this.records = records;
        this.context = context;
        this.db = db;
        this.helper = helper;
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
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.record_item, viewGroup, false);
        }

        TextView recordName = (TextView)view.findViewById(R.id.recordName);
        TextView recordDate = (TextView)view.findViewById(R.id.recordDate);
        TextView recordCity = (TextView)view.findViewById(R.id.recordCity);
        ImageView recordImage = (ImageView)view.findViewById(R.id.recordImage);
        LinearLayout layout = (LinearLayout)view.findViewById(R.id.cardLayout);

        recordName.setText(records.get(i).getName());
        recordCity.setText(records.get(i).getCity());
        SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        recordDate.setText(fmt.format(records.get(i).getDate()));
        if (records.get(i).getImage() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(records.get(i).getImage(), 0, records.get(i).getImage().length);
            recordImage.setImageBitmap(bitmap);
        }

        // Click to show points in Map
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // Long press to delete record
        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new MaterialDialog.Builder(context)
                        .title("Delete record?")
                        .content("Do you want to delete " + records.get(i).getName())
                        .positiveColor(ResourcesCompat.getColor(context.getResources(), R.color.colorNagative, null))
                        .negativeText("Cancel")
                        .positiveText("Delete")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                helper.deleteRecord(db, records.get(i).getId());
                                records.remove(i);
                                notifyDataSetChanged();
                            }
                        })
                        .build()
                        .show();
                return true;
            }
        });

        return view;
    }
}
