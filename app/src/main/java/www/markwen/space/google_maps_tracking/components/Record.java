package www.markwen.space.google_maps_tracking.components;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by markw on 4/14/2017.
 */

public class Record {
    private String name;
    private Date date;
    private String city;
    private ArrayList<LatLng> points = new ArrayList<>();

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
            result.add(new LatLng(Double.parseDouble(point[0]), Double.parseDouble(point[1])));
        }

        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDate(String date) {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        try {
            this.date = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<LatLng> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<LatLng> points) {
        this.points = points;
    }

    public void setPoints(String points) {
        this.points = pointsStringToArrayList(points);
    }
}
