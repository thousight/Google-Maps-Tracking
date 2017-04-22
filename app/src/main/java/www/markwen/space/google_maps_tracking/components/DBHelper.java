package www.markwen.space.google_maps_tracking.components;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

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
        db.execSQL("CREATE TABLE " + dbName + " (id INTEGER PRIMARY KEY, name CHAR(50), date CHAR(50), city CHAR(50), pointsStr CHAR(9999));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + dbName);
        onCreate(db);
    }

    public void saveRecord(SQLiteDatabase db, Record record) {
        db.execSQL("INSERT INTO " + dbName + " VALUES(NULL, '" + record.getName() + "', '" + record.getDate().toString() + "', '" + record.getCity() + "', '" + record.getPointsString() + "');");
    }

    public ArrayList<Record> getAllRecords(SQLiteDatabase db, Context context) {
        ArrayList<Record> results = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from " + dbName, null);

        if (cursor .moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String date = cursor.getString(cursor.getColumnIndex("date"));
                String city = cursor.getString(cursor.getColumnIndex("city"));
                String pointsStr = cursor.getString(cursor.getColumnIndex("pointsStr"));

                Record newRecord = new Record();
                newRecord.setName(name);
                newRecord.setDate(date, context);
                newRecord.setCity(city);
                newRecord.setPoints(pointsStr);

                results.add(newRecord);
                cursor.moveToNext();
            }
        }

        return results;
    }
}
