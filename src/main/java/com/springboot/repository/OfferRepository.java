package com.springboot.repository;

import com.springboot.controller.OfferRequest;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class OfferRepository {

    private static final Logger logger = LoggerFactory.getLogger(OfferRepository.class);

    // Key: restaurant_id + segment, Value: OfferRequest
    private final ConcurrentHashMap<String, OfferRequest> offers = new ConcurrentHashMap<>();

    private String generateKey(int restaurantId, String segment) {
        return restaurantId + "_" + segment;
    }

    public boolean addOffer(OfferRequest offerRequest) {
        logger.info("Adding offer for restaurant {} with segments {}",
                offerRequest.getRestaurantId(), offerRequest.getCustomerSegment());

        boolean offerAdded = false;

        // Add offer for each segment if it doesn't already exist
        for (String segment : offerRequest.getCustomerSegment()) {
            String key = generateKey(offerRequest.getRestaurantId(), segment);

            // Only add if key doesn't exist (preserve immutability behavior)
            if (!offers.containsKey(key)) {
                offers.put(key, offerRequest);
                offerAdded = true;
                logger.info("Offer added for restaurant {} and segment {}",
                        offerRequest.getRestaurantId(), segment);
            } else {
                logger.info("Offer already exists for restaurant {} and segment {}, skipping",
                        offerRequest.getRestaurantId(), segment);
            }
        }

        return offerAdded;
    }

    public Optional<OfferRequest> getOffer(int restaurantId, String userSegment) {
        String key = generateKey(restaurantId, userSegment);
        return Optional.ofNullable(offers.get(key));
    }

    public List<OfferRequest> getAllOffers() {
        return offers.values().stream().distinct().collect(Collectors.toList());
    }

    public void clearOffers() {
        logger.info("Clearing all offers from repository");
        offers.clear();
    }

    public int getOfferCount() {
        return offers.size();
    }
}
