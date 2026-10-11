package com.example.mock_order_service.model;

public enum FailureMode {

    SUCCESS,
    HTTP_500,
    HTTP_503,
    TIMEOUT,

    CONNECTION_FAILURE
}