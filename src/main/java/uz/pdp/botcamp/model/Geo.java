package uz.pdp.botcamp.model;

import lombok.Data;

@Data
public class Geo {
    private String latitude;
    private String longitude;

    public Geo(String  latitude, String  longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
