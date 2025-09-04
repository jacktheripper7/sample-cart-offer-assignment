package com.springboot.controller;

import com.springboot.service.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class AutowiredController {

	private static final Logger logger = LoggerFactory.getLogger(AutowiredController.class);
	private final OfferService offerService;

	@Autowired
	public AutowiredController(OfferService offerService) {
		this.offerService = offerService;
	}

	@PostMapping(path = "/api/v1/offer")
	public ResponseEntity<?> addOffer(@RequestBody OfferRequest offerRequest) {
		logger.info("Received add offer request: {}", offerRequest);

		try {
			boolean success = offerService.addOffer(offerRequest);
			return ResponseEntity.ok(new ApiResponse("success"));
		} catch (IllegalArgumentException e) {
			logger.error("Invalid offer request: {}", e.getMessage());
			return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
		} catch (Exception e) {
			logger.error("Error adding offer", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse("Internal server error"));
		}
	}

	@PostMapping(path = "/api/v1/cart/apply_offer")
	public ResponseEntity<?> applyOffer(@RequestBody ApplyOfferRequest applyOfferRequest) {
		logger.info("Received apply offer request: {}", applyOfferRequest);

		try {
			ApplyOfferResponse response = offerService.applyOffer(applyOfferRequest);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			logger.error("Invalid apply offer request: {}", e.getMessage());
			return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage()));
		} catch (Exception e) {
			logger.error("Error applying offer", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApplyOfferResponse(applyOfferRequest.getCart_value()));
		}
	}

	@PostMapping(path = "/api/v1/offer/clear")
	public ResponseEntity<ApiResponse> clearOffers() {
		logger.info("Clearing all offers");
		try {
			offerService.clearOffers();
			return ResponseEntity.ok(new ApiResponse("success"));
		} catch (Exception e) {
			logger.error("Error clearing offers", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse("Internal server error"));
		}
	}
}
