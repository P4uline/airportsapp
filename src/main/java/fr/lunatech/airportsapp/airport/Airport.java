package fr.lunatech.airportsapp.airport;

import java.util.List;

import fr.lunatech.airportsapp.runway.Runway;

public class Airport {

	private String name;
	private String municipality;
	private String countryCode;
	private List<Runway> runways;

	private String id;
	private String country;
	private String ident;

	public static Airport newInstance() {
		return new Airport();
	}
	public List<Runway> getRunways() {
		return runways;
	}
	public String getId() {
		return id;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public Airport name(String formatItem) {
		this.name = formatItem;
		return this;
	}

	public Airport municipality(String formatItem) {
		this.municipality = formatItem;
		return this;
	}

	@Override
	public String toString() {
		return name + " - " + municipality;
	}

	public Airport countryCode(String formatItem) {
		this.countryCode = formatItem;
		return this;
	}

	public Airport id(String string) {
		this.id = string;
		return this;
	}
	public String getIdent() {
		return ident;
	}
	public Airport ident(String formatItem) {
		this.ident = formatItem;
		return this;
	}
	public String getName() {
		return name;
	}

}
