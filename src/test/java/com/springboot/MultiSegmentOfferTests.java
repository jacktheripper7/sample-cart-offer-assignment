package com.springboot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
public class MultiSegmentOfferTests extends BaseOfferTest {

    @Test
    public void testPositive01_MultipleUsersDifferentSegments_SameRestaurant() {
        // TC-MP1: Same restaurant, multiple users in different segments
        addOffer(105, "FLATX", 10, "p1"); // Offer for segment p1
        addOffer(105, "FLATX", 30, "p2"); // Offer for segment p2

        applyOfferAndAssert(1, 105, 200, 190); // User=1 (p1) → 200-10=190
        applyOfferAndAssert(2, 105, 200, 170); // User=2 (p2) → 200-30=170
    }

    @Test
    public void testPositive02_SameRestaurantDifferentSegmentsDifferentDiscounts() {
        // TC-MP2: Same restaurant, different segments, different discounts
        addOffer(130, "FLATX", 20, "p1"); // Offer for p1 → flat 20 off
        addOffer(130, "FLAT%", 50, "p2"); // Offer for p2 → 50% off

        applyOfferAndAssert(1, 130, 200, 180); // User=1 (p1) → 200-20=180
        applyOfferAndAssert(2, 130, 200, 100); // User=2 (p2) → 200-(200*0.5)=100
    }

    @Test
    public void testPositive03_OverlappingSegmentsAcrossRestaurants() {
        // TC-MP3: Overlapping segments across restaurants
        addOffer(131, "FLATX", 10, "p1", "p2"); // Rest 131: offer for p1+p2
        addOffer(132, "FLAT%", 25, "p2", "p3"); // Rest 132: offer for p2+p3

        applyOfferAndAssert(1, 131, 200, 190); // User=1 (p1) → 200-10=190
        applyOfferAndAssert(2, 131, 200, 190); // User=2 (p2) → 200-10=190
        applyOfferAndAssert(2, 132, 200, 150); // User=2 (p2) → 25% off
        applyOfferAndAssert(3, 132, 200, 150); // User=3 (p3) → 25% off
    }

    @Test
    public void testNegative01_UserNotInAnyOfferedSegment() {
        // TC-MN1: User not in any offered segment
        // Offer [p1,p2], user=3 (p3) → 200
        addOffer(112, "FLATX", 25, "p1", "p2");
        applyOfferAndAssert(1, 112, 200, 175); // User=1 (p1) → gets discount
        applyOfferAndAssert(2, 112, 225, 200); // User=2 (p2) → gets discount
        applyOfferAndAssert(3, 112, 200, 200); // user_id=3 → p3 segment, no discount
    }

    @Test
    public void testNegative02_MultiSegmentOffer_ZeroCartValue() {
        // TC-MN2: Multi-segment offer, zero cart edge
        addOffer(138, "FLATX", 10, "p1", "p2");

        applyOfferAndAssert(1, 138, 0, -10); // User=1 (p1) → discount makes cart negative
        applyOfferAndAssert(2, 138, 0, -10); // User=2 (p2) → same result
    }

    @Test
    public void testNegative03_ConflictingOffersForSameSegment() {
        // TC-MN3: Conflicting offers for same segment (first-stick policy)
        addOffer(134, "FLATX", 10, "p1");
        addOffer(134, "FLATX", 50, "p1");

        applyOfferAndAssert(1, 134, 200, 190);
    }


    @Test
    public void testNegative04_InvalidOfferTypeRejected() {
        // TC-MN4: Invalid offer type rejected
        Map<String, Object> offer = new HashMap<>();
        offer.put("restaurant_id", 146);
        offer.put("offer_type", "NOT_A_TYPE");
        offer.put("offer_value", 10);
        offer.put("customer_segment", Arrays.asList("p1", "p2"));

        given()
                .contentType("application/json")
                .body(offer)
                .when()
                .post("/api/v1/offer")
                .then()
                .statusCode(400)
                .body("response_msg", containsString("Unsupported offer type"));
    }

    @Test
    public void testNegative05_EmptySegmentsRejected() {
        // TC-MN5: Empty segments rejected
        Map<String, Object> offer = new HashMap<>();
        offer.put("restaurant_id", 147);
        offer.put("offer_type", "FLATX");
        offer.put("offer_value", 10);
        offer.put("customer_segment", Collections.emptyList()); // empty

        given()
                .contentType("application/json")
                .body(offer)
                .when()
                .post("/api/v1/offer")
                .then()
                .statusCode(400)
                .body("response_msg", containsString("Customer segments cannot be null or empty"));
    }
}
