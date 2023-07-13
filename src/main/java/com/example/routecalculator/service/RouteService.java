package com.example.routecalculator.service;

import com.example.routecalculator.dto.CountryDto;
import com.example.routecalculator.dto.RouteResponse;
import com.example.routecalculator.exception.DataAccessException;
import com.example.routecalculator.exception.RouteCalculationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RouteService {

    @Value("${data.path.countries}")
    private String dataPath;

    private Map<String, List<String>> countries;

    /**
     * Method will be run at startup and will fetch the countries from the
     * provided URL {@link RouteService#dataPath}. The response is mapped to
     * {@link RouteService#countries} with the following format
     * country code -> list of borders.
     * <p>
     * If the response has any other status than HTTP 200 or no response body,
     * a {@link DataAccessException} is thrown.
     */
    @PostConstruct
    public void fetchCountries() throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(dataPath, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            final String errorMessage = "Error while fetching countries";
            log.error(errorMessage + " {}", response);
            throw new DataAccessException(errorMessage);
        }

        ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        CountryDto[] countryDtoArray = objectMapper.readValue(response.getBody(), CountryDto[].class);

        countries = Arrays.stream(countryDtoArray)
                .collect(Collectors.toMap(CountryDto::cca3, CountryDto::borders));
    }

    public RouteResponse getRoute(String origin, String destination) {
        validateOriginAndDestination(origin, destination);
        return new RouteResponse(calculateShortestRoute(origin, destination));
    }

    /**
     * Calculates the shortest route between two countries using Dijkstra's algorithm.
     *
     * @param origin      The origin country code.
     * @param destination The destination country code.
     * @return A list of country codes representing the shortest route from the origin to the destination.
     */
    private List<String> calculateShortestRoute(String origin, String destination) {
        // Distances map that holds the shortest known distance from the origin to each country
        Map<String, Integer> distances = new HashMap<>();

        // Previous countries map that holds the last country visited before reaching a specific country
        Map<String, String> previousCountries = new HashMap<>();

        // Priority queue to manage unvisited countries, prioritized by the current shortest known distance
        PriorityQueue<String> unvisitedCountries = new PriorityQueue<>(Comparator.comparing(distances::get));

        // Initialize distances to "infinity" and previous countries to null
        for (String countryCode : countries.keySet()) {
            distances.put(countryCode, Integer.MAX_VALUE);
            previousCountries.put(countryCode, null);
        }

        /* Set the distance from origin country to itself to 0, and we initialize the
         * unvisited countries with the origin country */
        distances.put(origin, 0);
        unvisitedCountries.add(origin);

        // Main loop of Dijkstra algorithm
        while (!unvisitedCountries.isEmpty()) {
            // Get the country with the shortest known distance that hasn't been visited yet
            String currentCountryCode = unvisitedCountries.poll();

            // Visit each neighbor of the current country and select the shortest distance
            for (String neighbor : countries.get(currentCountryCode)) {

                /* Calculate the distance to the neighbor through the current country.
                 * The weight of each border crossing is 1 */
                int currentDistance = distances.get(currentCountryCode) + 1;

                /* If the new distance is shorter than the known distance, update the distance, and
                 *  set the new previous country */
                if (currentDistance < distances.get(neighbor)) {
                    distances.put(neighbor, currentDistance);
                    previousCountries.put(neighbor, currentCountryCode);

                    // add the neighbor to the queue so that it will be visited in a future iteration
                    unvisitedCountries.add(neighbor);
                }
            }
        }

        // Build the shortest path by walking from the destination to the origin using the previous countries map
        List<String> path = new ArrayList<>();
        String countryCode = destination;
        while (countryCode != null) {
            path.add(countryCode);
            countryCode = previousCountries.get(countryCode);
        }

        // Reverse the path to start from the source country
        Collections.reverse(path);

        return path;
    }

    /**
     * Validate the origin and destination countries by checking the following cases:
     *  - origin or destination country code is not present in data set
     *  - origin equals destination, which means there is no border crossing
     *  - origin country has no neighbors, which means there is no border crossing
     */
    private void validateOriginAndDestination(String origin, String destination) {
        if (!countries.containsKey(origin) || !countries.containsKey(destination)) {
            throw new RouteCalculationException("Invalid origin or destination provided!");
        } else if (origin.equals(destination) || countries.get(origin).isEmpty()) {
            throw new RouteCalculationException("No border crossing!");
        }
    }
}
