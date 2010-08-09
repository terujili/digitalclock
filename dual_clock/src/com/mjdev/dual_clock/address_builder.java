package com.mjdev.dual_clock;

import android.location.Address;

public class address_builder {
	public final static addressInfo[] DEFAULT_ADDRESS_TEMPLATE_WITHOUT_COUNTRY=new addressInfo[]{
		addressInfo.FEATURE_NAME,
		addressInfo.COMMA_CHAR,
		addressInfo.THOROUGHFARE,
		addressInfo.BACKSPACE_CHAR,
		addressInfo.POSTAL_CODE,
		addressInfo.COMMA_CHAR,
		addressInfo.LOCALITY};
	public final static addressInfo[] DEFAULT_ADDRESS_TEMPLATE=new addressInfo[]{
		addressInfo.FEATURE_NAME,
		addressInfo.COMMA_CHAR,
		addressInfo.THOROUGHFARE,
		addressInfo.BACKSPACE_CHAR,
		addressInfo.POSTAL_CODE,
		addressInfo.COMMA_CHAR,
		addressInfo.LOCALITY,
		addressInfo.COMMA_CHAR,
		addressInfo.COUNTRY_NAME};
	public final static addressInfo[] DEFAULT_ADDRESS_CLOCK=new addressInfo[]{ addressInfo.LOCALITY };
	public String build (Address address, addressInfo[] template){
		StringBuilder builder=new StringBuilder();
		String lastAddressValue=null;
		addressInfo lastAddressInfo=null;
		for(addressInfo addressInfo:template){
			String value=addressInfo.getValue(address);
			if(value!=null && value.equals(lastAddressValue)==false){
				if(com.mjdev.dual_clock.address_builder.addressInfo.isSpecialChar(addressInfo)==false || com.mjdev.dual_clock.address_builder.addressInfo.isSpecialChar(lastAddressInfo)==false)
					builder.append(value);
				if(com.mjdev.dual_clock.address_builder.addressInfo.isSpecialChar(addressInfo)==false)
					lastAddressValue=value;
				lastAddressInfo=addressInfo;
			}
		}
		return builder.toString();
	}
	public enum addressInfo {
		FEATURE_NAME{
			@Override public String getValue(Address address) { return address.getFeatureName(); }
		},
		THOROUGHFARE{
			@Override public String getValue(Address address) { return address.getThoroughfare(); }
		},
		POSTAL_CODE{
			@Override public String getValue(Address address) { return address.getPostalCode(); }
		},
		LOCALITY{
			@Override public String getValue(Address address) { return address.getLocality(); }
		},
		COUNTRY_NAME{
			@Override public String getValue(Address address) { return address.getCountryName(); }
		},
		BACKSPACE_CHAR{
			@Override public String getValue(Address address) { return "\n"; }
		},
		COMMA_CHAR{
			@Override public String getValue(Address address) { return ", "; }
		};
		public abstract String getValue (Address address); 
		public static boolean isSpecialChar (addressInfo addressInfo){
			return addressInfo==BACKSPACE_CHAR || addressInfo==COMMA_CHAR;
		}
	}
}