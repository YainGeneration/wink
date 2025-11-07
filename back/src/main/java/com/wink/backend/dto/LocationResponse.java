package com.wink.backend.dto;
import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class LocationResponse {
    private String placeName;
    private double lat;
    private double lng;
    private String address;
}