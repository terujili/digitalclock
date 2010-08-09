package com.mjdev.dual_clock;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.mjdev.dual_clock.address_builder;
import com.mjdev.dual_clock.address_builder.addressInfo;
import com.mjdev.weather.*;

public class weather implements LocationListener{
	private Context                 context          = null;
	private LocationManager         locationManager  = null;
	private Location                lastLocation     = null;
	private Geocoder                geocoder         = null;
	private Address                 lastAddress      = null;
	public  Locale                  localise         = null;
	public  String                  address          = "";
	public  weather_current_condition wc               = null;
	@SuppressWarnings("static-access")
	public          weather(Context context){
		try{
			this.context = context;
			locationManager = (LocationManager)this.context.getSystemService(context.LOCATION_SERVICE);
			if(locationManager !=null) {
				geocoder = new Geocoder(this.context);
				localise = Locale.getDefault();
				lastLocation = locationManager.getLastKnownLocation(getMyBestProvider());
				updateLocation();
			}
		} catch (Exception e){ this.address = "unavailable"; }
	}
	public Location getLastLocation() { return lastLocation; }
	public void     updateLocation()      { 
		this.lastLocation=null;	
		this.locationManager.removeUpdates(this);
		this.locationManager.requestLocationUpdates(getMyBestProvider(), 0, 0, this);
	}
	public String   getLastAddress()    { 
		return getAddress(address_builder.DEFAULT_ADDRESS_CLOCK); 
	}
	@Override
	public void     onLocationChanged(Location location) {
		this.lastLocation = location;
		this.lastAddress  = null;
		this.address      = getAddress(address_builder.DEFAULT_ADDRESS_CLOCK);
	}
	public String   getAddress (addressInfo[] template) {
		if(this.lastLocation==null) return null;
		if(this.lastAddress==null){
			try {
				List<Address> addresses = this.geocoder.getFromLocation(this.lastLocation.getLatitude(), this.lastLocation.getLongitude(), 1);
				if(addresses.isEmpty()) return null;
				this.lastAddress = addresses.get(0);
			} catch (IOException e) { return null; }
		}
		return new address_builder().build(this.lastAddress, template);
	}
	@SuppressWarnings("static-access")
	public String   getMyBestProvider () {
		String bp = null;
		Criteria criteria=new Criteria();
		criteria.setAltitudeRequired(false);
		criteria.setSpeedRequired(false);
		criteria.setBearingRequired(false);
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		bp = this.locationManager.getBestProvider(criteria, true);
		if(bp==null) bp = locationManager.NETWORK_PROVIDER;
		return bp;
	}
	public String   getTemperature() { 
		try { 
			updateForecasts(); 
		} catch (Exception e) { }
		if(wc!=null) {
			if(localise.equals(Locale.US)) return wc.getTempFahrenheit()+"°F"; 
			else return wc.getTempCelcius()+"°C"; 
		} else {
			if(localise.equals(Locale.US)) return "?°F"; 
			else return "?°C"; 
		}
	}
	@Override
	public void     onProviderDisabled(String arg0) { }
	@Override
	public void     onProviderEnabled(String arg0) { }
	@Override
	public void     onStatusChanged(String arg0, int arg1, Bundle arg2) { }
	public void     updateForecasts() throws Exception {
		String cityParamString = getLastAddress();
		if(cityParamString!=null) {
			URL url = new URL(("http://www.google.com/ig/api?weather="+cityParamString).replace(" ", "%20"));
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			google_weather_handler gwh = new google_weather_handler();
			xr.setContentHandler(gwh);
			xr.parse(new InputSource(url.openStream()));
			weather_set ws = gwh.getWeatherSet();
			if(ws!=null) wc = ws.getWeatherCurrentCondition();
		}
    }
	public Bitmap   getRemoteImage(String aURL) {
		try {
			URL url = new URL(("http://www.google.com/"+aURL).replace(" ", "%20"));
			URLConnection conn = url.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			Bitmap bm = BitmapFactory.decodeStream(bis);
			bis.close();
			is.close();
			return(bm);
		} catch (IOException e) {return null;}
	}
}