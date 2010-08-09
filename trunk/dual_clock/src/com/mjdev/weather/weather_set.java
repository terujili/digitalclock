package com.mjdev.weather;

import java.util.ArrayList;

public class weather_set {
	private weather_current_condition myCurrentCondition = null;
	private ArrayList<weather_fforecast_condition> myForecastConditions = new ArrayList<weather_fforecast_condition>(4);
	public weather_current_condition getWeatherCurrentCondition() {
		return myCurrentCondition;
	}
	public void setWeatherCurrentCondition(
			weather_current_condition myCurrentWeather) {
		this.myCurrentCondition = myCurrentWeather;
	}
	public ArrayList<weather_fforecast_condition> getWeatherForecastConditions() {
		return this.myForecastConditions;
	}
	public weather_fforecast_condition getLastWeatherForecastCondition() {
		return this.myForecastConditions
				.get(this.myForecastConditions.size() - 1);
	}
	public static int fahrenheitToCelsius(int tFahrenheit) {
		return (int) ((5.0f / 9.0f) * (tFahrenheit - 32));
	}
	public static int celsiusToFahrenheit(int tCelsius) {
		return (int) ((9.0f / 5.0f) * tCelsius + 32);
	}
}
