package fr.lunatech.airportsapp.country;

public class Country {

	public static Country newInstance() {
		return new Country();
	}

	private String countryCode;
	private String name;

	public Country countryCode(String formatItem) {
		this.countryCode = formatItem;
		return this;
	}

	public Country name(String formatItem) {
		this.name = formatItem;
		return this;
	}
	
	@Override
	public String toString() {
		return countryCode + " " + name;
	}

	public String getName() {
		return name;
	}

	public String getCountryCode() {
		return countryCode;
	}

}
