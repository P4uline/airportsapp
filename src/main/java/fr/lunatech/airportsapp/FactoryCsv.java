package fr.lunatech.airportsapp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lunatech.airportsapp.airport.Airport;
import fr.lunatech.airportsapp.country.Country;
import fr.lunatech.airportsapp.runway.Runway;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;

public class FactoryCsv {

	private static final Logger LOGGER = LoggerFactory.getLogger(FactoryCsv.class);

	private static Logger logger = LoggerFactory.getLogger(ApplicationConfiguration.class);

	public static Function<String, Airport> mapToAirport = (line) -> {
		String[] lineTab = line.split(",");
		return Airport.newInstance().id(formatItem(lineTab[0])).ident(formatItem(lineTab[1])).name(formatItem(lineTab[3]))
				.countryCode(formatItem(lineTab[8])).municipality(formatItem(lineTab[10]));
	};

	

	public static String formatItem(String item) {
		if (item != null && item.trim().length() > 0) {
			item = item.replaceAll("\"", "");
		}
		return item;
	}

	public static Function<String, Runway> mapToRunway = (line) -> {
		String[] lineTab = line.split(",");
		return Runway.newInstance().id(formatItem(lineTab[0])).airportCode(formatItem(lineTab[1])).airportIdent(formatItem(lineTab[2])).surface(formatItem(lineTab[5]));
	};

	public static Function<String, Country> mapToCountry  = (line) -> {
		String[] lineTab = line.split(",");
		return Country.newInstance().countryCode(formatItem(lineTab[1])).name(formatItem(lineTab[2]));
	};


	

	private static Flux<String> airportsFlux(String path, String countryCode) {
		return fromPath(path).map(mapToAirport).filter(a -> a.getCountryCode().equals(countryCode))
				.map(a -> a.toString());
	}

	private static Flux<String> runwaysStringFlux(String path, List<String> airportsCode) {
		return fromPath(path).map(mapToRunway).filter(r -> airportsCode.contains(r.getAirportCode()))
				.map(a -> a.toString());
	}
	
	

	private static Flux<String> airportsCodeFlux(String path, String countryCode) {
		return fromPath(path).map(mapToAirport).filter(a -> a.getCountryCode().equals(countryCode)).map(a -> a.getId());
	}
	
	

	public static Flux<String> fromPath(String filename) {
		return Flux.using(() -> Files.lines(Paths.get(filename)), Flux::fromStream, BaseStream::close);
	}

	
	
	

	
	public static void main(String[] args) {
		
		
		airportsFlux("./airports.csv", "FR").doOnNext(System.out::println).take(5)
				.delayElements(Duration.ofMillis(1000)).blockLast();
		System.out.println(airportsFlux("./airports.csv", "FR").collectList().block());


		airportsCodeFlux("./airports.csv", "FR").doOnNext(System.out::println).take(5).delayElements(Duration.ofMillis(1000)).blockLast();

//		2.1 Query Option will ask the user for the country name or code and print the airports & runways at each airport. The input can be country code or country name. For bonus points make the test partial/fuzzy. e.g. entering zimb will result in Zimbabwe :)
		runwaysStringFlux("./runways.csv", airportsCodeFlux("./airports.csv", "FR").collectList().block()).take(5).doOnNext(System.out::println)
				.delayElements(Duration.ofMillis(1000)).blockLast();


		/*
		2.2 Choosing Reports will print the following:

		    10 countries with highest number of airports (with count) and countries with lowest number of airports.
		    Type of runways (as indicated in "surface" column) per country
		    Bonus: Print the top 10 most common runway identifications (indicated in "le_ident" column)
		 */
		
		Map<String, Long> numberAirportsByCountry = fromPath("./airports.csv").skip(1).map(mapToAirport)
		.collect(Collectors.groupingBy(Airport::getCountryCode, Collectors.counting()))
		.doOnNext(System.out::println).block();
		
		Map<String, Long> sortedMap = new LinkedHashMap<>();

        //Sort a map and add to finalMap
		numberAirportsByCountry.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue()
                        .reversed()).forEachOrdered(e -> sortedMap.put(e.getKey(), e.getValue()));

		System.out.println("MAP TRIEE");
		Flux.just(sortedMap.entrySet()).doOnNext(System.out::println)
			.delayElements(Duration.ofMillis(1000)).blockLast();
		
		System.out.println("10 FIRST : " + sortedMap.entrySet().stream().skip(0).limit(10).collect(Collectors.toList()));
		System.out.println("10 LAST : " + sortedMap.entrySet().stream().skip(sortedMap.size() - 10).limit(sortedMap.size()).collect(Collectors.toList()));
        System.out.println(sortedMap);

		
//		fromPath("./airports.csv").map(mapToAirport)
//			.groupBy(Airport::getCountry, Collectors.counting())
//			.doOnNext(System.out::println).delayElements(Duration.ofMillis(1000)).blockLast();
        
//        Map<String, List<Airport>> airportsByCountry = fromPath("./airports.csv").skip(1).map(mapToAirport)
//        		.collect(Collectors.groupingBy(Airport::getCountryCode, Collectors.toList()))
//        		.doOnNext(System.out::println).block();
        
        // FIXME
        /*
        Map<String, List<String>> result = airportsByCountry.entrySet().stream().map(e -> {
        	
        	List<Airport> airports = e.getValue();
//        	List<String> airportCodes = Flux.fromIterable(airports).map(a -> a.getId()).collectList().block();
//        	runwaysFlux("./runways.csv", airportCodes).collectList().block();
//        	
//        	runwaysFlux("./runways.csv", airportCodes).map(r -> r.getSurface());
        	List<String> surfaces = airports.stream().
        		map(a -> fromPath("./runways.csv").map(mapToRunway).filter(r -> r.getAirportCode().equals(a.getId())).blockLast().getSurface()).
        		collect(Collectors.toList());
        	
       		return new AbstractMap.SimpleEntry<String, List<String>>(e.getKey(), surfaces);
        }).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        
        /// Surafces par countries !!!!
        System.out.println("SURFACES BY COUNTRY : " + result);
        */
//        Flux.just(airportsByCountry);
        
//        runwaysStringFlux("./runways.csv", Flux.just(airportsByCountry).collectList().block()).take(5).doOnNext(System.out::println)
//		.delayElements(Duration.ofMillis(1000)).blockLast();
		System.out.println("FINNNNNN");

		// fromPath("./airports.csv").delayElements(Duration.ofMillis(1000)).map(String::toUpperCase).take(3).subscribe(System.out::println);

	}
}
