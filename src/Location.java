/**
 * Created by jsh3571 on 12/02/2017.
 */

public class Location {
    private double latitude;
    private double longitude;
    private static final double ALTITUDE = 15.0;

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public static double getALTITUDE() {
        return ALTITUDE;
    }
}
