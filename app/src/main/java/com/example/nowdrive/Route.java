package com.example.nowdrive;

public class Route {
    public String routeName,encodedPolyLine, originLat, originLng, destLat, destLng, avoidHighways, avoidTolls;

    public Route(){

    }

    public Route(String routeName, String originLat, String originLng, String destLat, String destLng, String encodedPolyLine, String avoidHighways, String avoidTolls){
        this.routeName = routeName;
        this.originLat = originLat;
        this.originLng = originLng;
        this.destLat = destLat;
        this.destLng = destLng;
        this.encodedPolyLine = encodedPolyLine;
        this.avoidHighways = avoidHighways;
        this.avoidTolls = avoidTolls;
    }
}
