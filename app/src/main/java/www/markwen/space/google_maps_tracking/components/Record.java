package www.markwen.space.google_maps_tracking.components;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by markw on 4/14/2017.
 */

public class Record {
    private String name;
    private Date date;
    private String city;
    private ArrayList<LatLng> points = new ArrayList<>();
    private byte[] image;

    public Record() {
        name = "";
        date = new Date();
        city = "";
    }

    public Record(String name, String city, String points) {
        this.name = name;
        date = new Date();
        this.city = city;
        this.points = pointsStringToArrayList(points);
    }

    public Record(String name, String date, String city, String points) {
        this.name = name;
        this.city = city;

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        try {
            this.date = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.points = pointsStringToArrayList(points);
    }

    private ArrayList<LatLng> pointsStringToArrayList(String points) {
        ArrayList<LatLng> result = new ArrayList<>();
        String[] lines = points.split("\n");

        for (String line : lines) {
            String[] point = line.split(",");
            if (!point[0].equals("") && !point[1].equals("")) {
                result.add(new LatLng(Float.parseFloat(point[0]), Float.parseFloat(point[1])));
            }
        }

        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String name) {
        this.city = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDate(String date, Context context) {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        try {
            this.date = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<LatLng> getPoints() {
        return points;
    }

    public String getPointsString() {
        String result = "";
        for (int i = 0; i < points.size(); i++) {
            result += (points.get(i).latitude + "," + points.get(i).longitude + "\n");
        }
        return result;
    }

    public void setPoints(ArrayList<LatLng> points) {
        this.points = points;
    }

    public void setPoints(String points) {
        this.points = pointsStringToArrayList(points);
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public byte[] getImage() {
        return image;
    }
}
