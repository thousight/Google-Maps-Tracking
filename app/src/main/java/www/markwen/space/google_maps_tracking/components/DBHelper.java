package www.markwen.space.google_maps_tracking.components;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by markw on 4/14/2017.
 */

public class DBHelper extends SQLiteOpenHelper {
    private String dbName;

    public DBHelper(Context context) {
        super(context, "LocationTrackingDB", null, 1);
        dbName = "LocationTrackingDB";
    }

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        dbName = name;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + dbName + " ( id INTEGER PRIMARY KEY, name CHAR(50), date CHAR(50), city CHAR(50), pointsStr CHAR(9999));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + dbName);
        onCreate(db);
    }
}
