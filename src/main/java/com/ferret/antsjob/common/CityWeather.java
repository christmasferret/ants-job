package com.ferret.antsjob.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CityWeather {

    private String city;
    private long unixEpochTime;
    private double temperature;
    private String weatherDescription;
}
