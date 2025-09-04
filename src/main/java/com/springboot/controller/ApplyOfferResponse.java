package com.springboot.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ApplyOfferResponse {

    @JsonProperty("cart_value")
    private int cart_value;

    public ApplyOfferResponse() {
    }

    public ApplyOfferResponse(int cart_value) {
        this.cart_value = cart_value;
    }

    public int getCart_value() {
        return cart_value;
    }

    public void setCart_value(int cart_value) {
        this.cart_value = cart_value;
    }

    @Override
    public String toString() {
        return "ApplyOfferResponse{" +
                "cart_value=" + cart_value +
                '}';
    }
}
