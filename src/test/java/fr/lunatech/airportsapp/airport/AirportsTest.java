package fr.lunatech.airportsapp.airport;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.Ignore;
import org.junit.Test;

import fr.lunatech.airportsapp.airport.Airport;
import fr.lunatech.airportsapp.airport.AirportService;
import fr.lunatech.airportsapp.country.Country;
import fr.lunatech.airportsapp.runway.Runway;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class AirportsTest {

	private AirportService airportsService = new AirportService();

	@Test
	public void should_get_airports_flux() {
		Flux<Airport> airportsFlux = airportsService.fluxAirport();

		assertThat(airportsFlux).isNotNull();

		airportsService.fluxAirport().doOnNext(System.out::println).take(5).delayElements(Duration.ofMillis(1000)).blockLast();
		
//		StepVerifier.create(transporterService.fluxAirport().map(a -> a.toString())) 
//			    .expectNext("foo") 
//			    .expectNext("bar")
//			    .expectComplete() 
//			    .verify(); 
	}

	
	@Test
	public void should_get_countries_flux() {
		Flux<Country> countriesFlux = airportsService.fluxCountries();

		assertThat(countriesFlux).isNotNull();

		countriesFlux.doOnNext(System.out::println).take(5).delayElements(Duration.ofMillis(1000)).blockLast();
	}

	
	@Test
	public void should_get_runways_flux() {
		Flux<Runway> runwaysFlux = airportsService.fluxRunways();

		assertThat(runwaysFlux).isNotNull();

		runwaysFlux.doOnNext(System.out::println).take(5).delayElements(Duration.ofMillis(1000)).blockLast();
	}
	
	@Test
	public void should_get_airports_from_country_code() {
		Flux<Airport> airportsFlux = airportsService.fluxAirportByCountryCode("FR");

		assertThat(airportsFlux).isNotNull();

		airportsFlux.doOnNext(System.out::println).take(5).delayElements(Duration.ofMillis(1000)).blockLast();
	}
	
	@Test
	public void should_get_countries_by_name() {
		Mono<Country> countriesFlux = airportsService.fluxCountryByName("France");
		assertThat(countriesFlux).isNotNull();

		countriesFlux.doOnNext(System.out::println).block();
	}
	
	@Test
	public void should_get_airports_from_country_name() {
		Flux<Airport> airportsFlux = airportsService.fluxAirportByCountryName("France");

		assertThat(airportsFlux).isNotNull();

		airportsFlux.doOnNext(System.out::println).take(5).delayElements(Duration.ofMillis(1000)).blockLast();
	}
	
	@Test
	public void should_get_airports_from_country_name_with_zip() {
		Flux<Airport> airportsFlux = airportsService.fluxAirportByCountryName("France");

		assertThat(airportsFlux).isNotNull();

		airportsFlux.doOnNext(System.out::println).take(5).delayElements(Duration.ofMillis(1000)).blockLast();
	}
	
	@Test
	public void should_flux_airports_and_runways_from_country_name() {
		Flux<String> airportsAndRunwaysFlux = airportsService.zipAirportAndRunwaysByCountryName("United States");
		airportsAndRunwaysFlux.doOnNext(System.out::println).delayElements(Duration.ofMillis(1000)).blockFirst();
	}
	
	@Test
	public void should_flux_airports_with_no_runways_by_country_name() {
		Flux<String> airportsAndRunwaysFluxInZimbabwe = airportsService.fluxAirportWithNoRunwaysByCountryName("Zimbabwe");
		assertThat(airportsAndRunwaysFluxInZimbabwe).isNotNull();
		airportsAndRunwaysFluxInZimbabwe.doOnNext(System.out::println).take(5).delayElements(Duration.ofMillis(1000)).blockLast();
	}


	@Test
	public void should_count_airports_by_country() {
		Mono<Map<String, Long>> airportsAndRunwaysFlux = airportsService.countAirportsByCountry();
		airportsAndRunwaysFlux.doOnNext(System.out::println).block();
	}
	
	@Test
	public void should_group_and_sort_and_keep_10_first_and_10_last() {
		airportsService.fluxTenFirstsTenLast().doOnNext(System.out::println).delayElements(Duration.ofMillis(1000)).blockFirst();
	}
	
	@Test
	public void should_compose() {
		AtomicInteger ai = new AtomicInteger();
		Function<Flux<String>, Flux<String>> filterAndMap = f -> {
			if (ai.incrementAndGet() == 1) {
				return f.filter(color -> !color.equals("orange")).map(String::toUpperCase);
			}
			return f.filter(color -> !color.equals("purple")).map(String::toUpperCase);
		};

		Flux<String> composedFlux = Flux.fromIterable(Arrays.asList("blue", "green", "orange", "purple"))
				.doOnNext(System.out::println).compose(filterAndMap);

		composedFlux.subscribe(d -> System.out.println("Subscriber 1 to Composed MapAndFilter :" + d));
		composedFlux.subscribe(d -> System.out.println("Subscriber 2 to Composed MapAndFilter: " + d));
	}
	
	@Test
	public void should_test() {
		Function<Flux<String>, Flux<String>> filterAndMap =
				f -> f.filter(color -> !color.equals("orange"))
				      .map(String::toUpperCase);

				Flux.fromIterable(Arrays.asList("blue", "green", "orange", "purple"))
				        .doOnNext(System.out::println)
				        .transform(filterAndMap)
				        .subscribe(d -> System.out.println("Subscriber to Transformed MapAndFilter: "+d));
	}
	
	@Test
	@Ignore
	public void should_context() {
		String key = "message";
		Mono<String> r = Mono.just("Hello")
		                .flatMap( s -> Mono.subscriberContext()
		                                   .map( ctx -> s + " " + ctx.get(key)))
		                .subscriberContext(ctx -> ctx.put(key, "World"));

		StepVerifier.create(r)
		            .expectNext("Hello World")
		            .verifyComplete();
	}
	
	@Test
	@Ignore
	public void should_group() {
		StepVerifier.create(
		        Flux.just(1, 3, 5, 2, 4, 6, 11, 12, 13)
		                .groupBy(i -> i % 2 == 0 ? "even" : "odd")
		                .concatMap(g -> g.defaultIfEmpty(-1) //if empty groups, show them
		                                .map(String::valueOf) //map to string
		                                .startWith(g.key())) //start with the group's key
		        )
		        .expectNext("odd", "1", "3", "5", "11", "13")
		        .expectNext("even", "2", "4", "6", "12")
		        .verifyComplete();
	}

}
