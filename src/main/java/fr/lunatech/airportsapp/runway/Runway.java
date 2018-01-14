package fr.lunatech.airportsapp.runway;

public class Runway {

	private String id;
	private String airportCode;
	private String surface;
	private String airportIdent;
	

	public static Runway newInstance() {
		return new Runway();
	}

	public String getAirportCode() {
		return airportCode;
	}

	public Runway id(String formatItem) {
		this.id = formatItem;
		return this;
	}

	public Runway airportCode(String formatItem) {
		this.airportCode = formatItem;
		return this;
	}

	@Override
	public String toString() {
		return id + " " + airportCode;
	}

	public String getSurface() {
		return surface;
	}

	public void setSurface(String surface) {
		this.surface = surface;
	}

	public Runway surface(String formatItem) {
		this.surface = formatItem;
		return this;
	}

	public String getAirportIdent() {
		return airportIdent;
	}

	public Runway airportIdent(String formatItem) {
		this.airportIdent = formatItem;
		return this;
	}

}