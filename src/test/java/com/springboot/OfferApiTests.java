package com.springboot;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class OfferApiTests {
    @LocalServerPort
    private int port;

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    public void testAddValidFlatXOffer() {
        Map<String, Object> offer = new HashMap<>();
        offer.put("restaurant_id", 1);
        offer.put("offer_type", "FLATX");
        offer.put("offer_value", 10);
        offer.put("customer_segment", Arrays.asList("p1"));

        given()
                .contentType(ContentType.JSON)
                .body(offer)
                .when()
                .post("/api/v1/offer")
                .then()
                .statusCode(200)
                .body("response_msg", equalTo("success"));
    }

    @Test
    public void testAddValidFlatPercentOffer() {
        Map<String, Object> offer = new HashMap<>();
        offer.put("restaurant_id", 2);
        offer.put("offer_type", "FLAT%");
        offer.put("offer_value", 15);
        offer.put("customer_segment", Arrays.asList("p1"));

        given()
                .contentType(ContentType.JSON)
                .body(offer)
                .when()
                .post("/api/v1/offer")
                .then()
                .statusCode(200)
                .body("response_msg", equalTo("success"));
    }

    @Test
    public void testAddInvalidOfferType() {
        Map<String, Object> offer = new HashMap<>();
        offer.put("restaurant_id", 3);
        offer.put("offer_type", "INVALID");
        offer.put("offer_value", 10);
        offer.put("customer_segment", Arrays.asList("p1"));

        given()
                .contentType(ContentType.JSON)
                .body(offer)
                .when()
                .post("/api/v1/offer")
                .then()
                .statusCode(400)
                .body("response_msg", containsString("Unsupported offer type"));
    }

    @Test
    public void testAddOfferMissingSegments() {
        Map<String, Object> offer = new HashMap<>();
        offer.put("restaurant_id", 4);
        offer.put("offer_type", "FLATX");
        offer.put("offer_value", 10);
        offer.put("customer_segment", Arrays.asList());

        given()
                .contentType(ContentType.JSON)
                .body(offer)
                .when()
                .post("/api/v1/offer")
                .then()
                .statusCode(400)
                .body("response_msg", containsString("Customer segments cannot be null or empty"));
    }

    @Test
    public void testAddOfferMultipleSegments() {
        Map<String, Object> offer = new HashMap<>();
        offer.put("restaurant_id", 5);
        offer.put("offer_type", "FLATX");
        offer.put("offer_value", 25);
        offer.put("customer_segment", Arrays.asList("p1", "p2"));

        given()
                .contentType(ContentType.JSON)
                .body(offer)
                .when()
                .post("/api/v1/offer")
                .then()
                .statusCode(200)
                .body("response_msg", equalTo("success"));
    }
}
