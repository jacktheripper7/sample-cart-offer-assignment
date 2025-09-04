package com.springboot;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseOfferTest {

    @LocalServerPort
    protected int port;

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    protected void addOffer(int restaurantId, String offerType, int offerValue, String... segments) {
        Map<String, Object> offer = new HashMap<>();
        offer.put("restaurant_id", restaurantId);
        offer.put("offer_type", offerType);
        offer.put("offer_value", offerValue);
        offer.put("customer_segment", Arrays.asList(segments));

        given()
                .contentType(ContentType.JSON)
                .body(offer)
                .when()
                .post("/api/v1/offer")
                .then()
                .statusCode(200)
                .body("response_msg", equalTo("success"));
    }

    protected void applyOfferAndAssert(int userId, int restaurantId, int cartValue, int expectedValue) {
        Map<String, Object> applyRequest = new HashMap<>();
        applyRequest.put("user_id", userId);
        applyRequest.put("restaurant_id", restaurantId);
        applyRequest.put("cart_value", cartValue);

        given()
                .contentType(ContentType.JSON)
                .body(applyRequest)
                .when()
                .post("/api/v1/cart/apply_offer")
                .then()
                .statusCode(200)
                .body("cart_value", equalTo(expectedValue));
    }
}
