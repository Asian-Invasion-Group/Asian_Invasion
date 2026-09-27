package com.example.lifestylelevellingasianlifestyle;

/**
 * Stores location information to be used with IP service
 * @param cityName name of city
 * @param latitude lat coordinate
 * @param longitude long coordinate
 */
public record LocationData(
        String cityName,
        double latitude,
        double longitude
) {}
