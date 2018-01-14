package fr.lunatech.airportsapp.airport;

import static fr.lunatech.airportsapp.FactoryCsv.fromPath;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import fr.lunatech.airportsapp.country.Country;
import fr.lunatech.airportsapp.runway.Runway;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public class AirportsService {

	private static final String RUNWAYS_CSV = "./runways.csv";
	private static final String COUNTRIES_CSV = "./countries.csv";
	private static final String AIRPORTS_CSV = "./airports.csv";
	
	public static String formatItem(String item) {
		if (item != null && item.trim().length() > 0) {
			item = item.replaceAll("\"", "");
		}
		return item;
	}
	
	public static Function<String, Airport> mapToAirport = (line) -> {
		String[] lineTab = line.split(",");
		return Airport.newInstance().id(formatItem(lineTab[0])).ident(formatItem(lineTab[1])).name(formatItem(lineTab[3]))
				.countryCode(formatItem(lineTab[8])).municipality(formatItem(lineTab[10]));
	};

	public static Function<String, Runway> mapToRunway = (line) -> {
		String[] lineTab = line.split(",");
		return Runway.newInstance().id(formatItem(lineTab[0])).airportCode(formatItem(lineTab[1])).airportIdent(formatItem(lineTab[2])).surface(formatItem(lineTab[5]));
	};

	public static Function<String, Country> mapToCountry  = (line) -> {
		String[] lineTab = line.split(",");
		return Country.newInstance().countryCode(formatItem(lineTab[1])).name(formatItem(lineTab[2]));
	};

	public Flux<Airport> fluxAirport() {
		return fromPath(AIRPORTS_CSV).map(mapToAirport);
	}
	
	public Flux<Airport> fluxAirportByCountryCode(String countryCode) {
		return fromPath(AIRPORTS_CSV).map(mapToAirport).filter(a -> a.getCountryCode().equals(countryCode));
	}

	public Flux<Airport> fluxAirportByCountryName(String countryName) {
		String countryCode = fluxCountryByName(countryName).block().getCountryCode();
		return fromPath(AIRPORTS_CSV).map(mapToAirport).filter(a -> {
			return countryCode.equals(a.getCountryCode());
		});
	}

	public Flux<Country> fluxCountries() {
		return fromPath(COUNTRIES_CSV).map(mapToCountry);
	}
	
	public Mono<Country> fluxCountryByName(String countryName) {
		return fromPath(COUNTRIES_CSV).map(mapToCountry).filter(c -> c.getName().equals(countryName)).next();
	}

	public Flux<Runway> fluxRunways() {
		return fromPath(RUNWAYS_CSV).map(mapToRunway);
	}

	public Flux<String> zipAirportAndRunwaysByCountryName(String countryName) {
		Function<Tuple2<Runway, Airport>, String> function = tuple -> {
			StringBuilder sb = new StringBuilder();
			String airport = sb.append(tuple.getT2()).append(" - ").append(tuple.getT2().getIdent()).toString();
			String runway = new StringBuilder().append(tuple.getT1().getAirportIdent()).append(" - ").append(tuple.getT1()).toString();
			return airport + " ***************************** " + runway;
		};
		return fluxRunways().skip(1).zipWith(fluxAirportByCountryName(countryName)).map(function); // no need to filter
	}
	
	public Flux<String> fluxAirportWithNoRunwaysByCountryName(String countryName) {
		Function<Airport, String> function = airport -> {
			StringBuilder sb = new StringBuilder();
			String result = sb.append(airport.getName()).append(" - ").append(airport.getCountryCode()).append(" - ").append(airport.getIdent()).toString();
			return result + " ***************************** ";
		};
		return fluxAirportByCountryName(countryName).map(function);
	}
	
	public Mono<Map<String, Long>> countAirportsByCountry() {
		return fluxAirport().collect(Collectors.groupingBy(Airport::getCountryCode, Collectors.counting()));
	}
	
	public Flux<String> fluxTenFirstsTenLast() {
		Map<String, Long> sortedMap = new LinkedHashMap<>();

		// Sort a map and add to finalMap
		countAirportsByCountry().block().entrySet().stream()
				.sorted(Map.Entry.<String, Long>comparingByValue().reversed())
				.forEachOrdered(e -> sortedMap.put(e.getKey(), e.getValue()));

		Flux<String> tenFirst = Flux.fromIterable(sortedMap.entrySet()).sort((x, y) -> y.getValue().compareTo(x.getValue())).map(e -> e.getValue() + " " + e.getKey() )
		.take(10);
		
		Flux<String> tenLast = Flux.fromIterable(sortedMap.entrySet()).sort((x, y) -> y.getValue().compareTo(x.getValue())).map(e -> e.getValue() + " " + e.getKey() )
		.takeLast(10);
		
		return Flux.concat(tenFirst, tenLast).doOnNext(System.out::println).delayElements(Duration.ofMillis(1000));

	}
}
