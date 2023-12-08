package cs1302.api;

import com.google.gson.annotations.SerializedName;

/**
 * This helper class is used for the weather object in
 * the WeatherApiResponse class.
 */
public class Main {
    double temp;

    @SerializedName("feels_like")
    double feelsLike;
}
