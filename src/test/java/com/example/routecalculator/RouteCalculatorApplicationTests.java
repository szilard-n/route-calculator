package com.example.routecalculator;

import com.example.routecalculator.dto.ApiErrorResponse;
import com.example.routecalculator.dto.RouteResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class RouteCalculatorApplicationTests {

    @Test
    void contextLoads() {
        assertTrue(true);
    }

    @Test
    @DisplayName("Get Route - Should return HTTP 200 and correct route")
    void getRoute() {
        RouteResponse response = given()
                .get("/routing/CZE/ITA")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(RouteResponse.class);

        assertEquals(List.of("CZE", "AUT", "ITA"), response.route());
    }

    @Test
    @DisplayName("Get Route - Should return HTTP 400 because of no border crossing")
    void getRoute_noBorderCrosing() {
        ApiErrorResponse response = given()
                .get("/routing/ITA/ITA")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract()
                .as(ApiErrorResponse.class);

        assertEquals("No border crossing!", response.getReason().toString());
    }

    @Test
    @DisplayName("Get Route - Should return HTTP 400 because of no border crossing as there are no neighbors")
    void getRoute_noBorderCrosing_noNeighbors() {
        ApiErrorResponse response = given()
                .get("/routing/ABW/HUN")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract()
                .as(ApiErrorResponse.class);

        assertEquals("No border crossing!", response.getReason().toString());
    }

    @Test
    @DisplayName("Get Route - Should return HTTP 400 because of invalid country code")
    void getRoute_invalidPathVariables() {
        ApiErrorResponse response = given()
                .get("/routing/QQQ/WWW")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract()
                .as(ApiErrorResponse.class);

        assertEquals("Invalid origin or destination provided!", response.getReason().toString());
    }

    @Test
    @DisplayName("Get Route - Should return HTTP 400 because of incorrect country code format")
    void getRoute_incorrectCountryCodeFormat() {
        given()
                .get("/routing/123/WWWW")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

}
