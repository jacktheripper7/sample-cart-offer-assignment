package com.springboot.service;

import com.springboot.controller.OfferRequest;
import com.springboot.controller.ApplyOfferRequest;
import com.springboot.controller.ApplyOfferResponse;
import com.springboot.controller.SegmentResponse;
import com.springboot.repository.OfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;

@Service
public class OfferService {

    private static final Logger logger = LoggerFactory.getLogger(OfferService.class);
    private final OfferRepository offerRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public OfferService(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
        this.objectMapper = new ObjectMapper();
    }

    public boolean addOffer(OfferRequest offerRequest) {
        validateOfferRequest(offerRequest);

        boolean result = offerRepository.addOffer(offerRequest);

        if (result) {
            logger.info("Successfully added offer: restaurant_id={}, offer_type={}, offer_value={}, segments={}",
                    offerRequest.getRestaurantId(), offerRequest.getOfferValue(),
                    offerRequest.getOfferValue(), offerRequest.getCustomerSegment());
        }

        return result;
    }

    public ApplyOfferResponse applyOffer(ApplyOfferRequest applyOfferRequest) throws Exception {
        validateApplyOfferRequest(applyOfferRequest);

        int originalCartValue = applyOfferRequest.getCart_value();
        int finalCartValue = originalCartValue;

        logger.info("Applying offer for user_id={}, restaurant_id={}, cart_value={}",
                applyOfferRequest.getUser_id(), applyOfferRequest.getRestaurant_id(), originalCartValue);

        // Get user segment
        SegmentResponse segmentResponse = getUserSegment(applyOfferRequest.getUser_id());

        if (segmentResponse != null) {
            String userSegment = segmentResponse.getSegment();
            logger.info("User {} belongs to segment: {}", applyOfferRequest.getUser_id(), userSegment);

            // Find matching offer
            Optional<OfferRequest> matchingOffer = offerRepository.getOffer(
                    applyOfferRequest.getRestaurant_id(), userSegment);

            if (matchingOffer.isPresent()) {
                OfferRequest offer = matchingOffer.get();
                finalCartValue = calculateDiscount(originalCartValue, offer);

                logger.info("Offer applied: type={}, value={}, original_cart={}, final_cart={}",
                        offer.getOfferType(), offer.getOfferValue(), originalCartValue, finalCartValue);
            } else {
                logger.info("No matching offer found for restaurant_id={} and segment={}",
                        applyOfferRequest.getRestaurant_id(), userSegment);
            }
        } else {
            logger.warn("Could not determine user segment for user_id={}", applyOfferRequest.getUser_id());
        }

        return new ApplyOfferResponse(finalCartValue);
    }

    private int calculateDiscount(int cartValue, OfferRequest offer) {
        int finalValue = cartValue;

        if ("FLATX".equals(offer.getOfferType())) {
            finalValue = cartValue - offer.getOfferValue();
        } else if ("FLAT%".equals(offer.getOfferType()) || "FLATP".equals(offer.getOfferType())) {
            double discountAmount = cartValue * offer.getOfferValue() * 0.01;
            finalValue = (int) (cartValue - discountAmount);
        }

        // Ensure cart value doesn't go negative (preserve current behavior)
        // Note: Based on your logs, the app allows negative values, so we maintain that
        return finalValue;
    }

    private SegmentResponse getUserSegment(int userId) throws Exception {
        String url = "http://localhost:1080/api/v1/user_segment?user_id=" + userId;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (InputStream is = connection.getInputStream();
                     BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line.trim());
                    }
                    return objectMapper.readValue(response.toString(), SegmentResponse.class);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get user segment for user_id={}", userId, e);
        }

        return null;
    }

    private void validateOfferRequest(OfferRequest offerRequest) {
        if (offerRequest == null) {
            throw new IllegalArgumentException("Offer request cannot be null");
        }

        if (offerRequest.getRestaurantId() <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }

        if (offerRequest.getOfferType() == null || offerRequest.getOfferType().trim().isEmpty()) {
            throw new IllegalArgumentException("Offer type cannot be null or empty");
        }

        String offerType = offerRequest.getOfferType().trim();
        if (!"FLATX".equals(offerType) && !"FLAT%".equals(offerType) && !"FLATP".equals(offerType)) {
            throw new IllegalArgumentException("Unsupported offer type: " + offerType +
                    ". Supported types: FLATX, FLAT%, FLATP");
        }

        if (offerRequest.getOfferValue() < 0) {
            throw new IllegalArgumentException("Offer value cannot be negative");
        }

        if (offerRequest.getCustomerSegment() == null || offerRequest.getCustomerSegment().isEmpty()) {
            throw new IllegalArgumentException("Customer segments cannot be null or empty");
        }
    }

    private void validateApplyOfferRequest(ApplyOfferRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Apply offer request cannot be null");
        }

        if (request.getUser_id() <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }

        if (request.getRestaurant_id() <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }

        if (request.getCart_value() < 0) {
            throw new IllegalArgumentException("Cart value cannot be negative");
        }
    }

    public void clearOffers() {
        offerRepository.clearOffers();
    }
}
