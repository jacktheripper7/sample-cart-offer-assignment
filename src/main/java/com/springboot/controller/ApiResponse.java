package com.springboot.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ApiResponse {

    @JsonProperty("response_msg")
    private String response_msg;

    public ApiResponse() {
    }

    public ApiResponse(String response_msg) {
        this.response_msg = response_msg;
    }

    public String getResponse_msg() {
        return response_msg;
    }

    public void setResponse_msg(String response_msg) {
        this.response_msg = response_msg;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "response_msg='" + response_msg + '\'' +
                '}';
    }
}