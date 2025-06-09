package com.alibou.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.RestAssured;
import io.restassured.response.Response;

@PactConsumerTest
public class OrderServiceContractTest {

    @Pact(consumer = "payment-service", provider = "order-service")
    public V4Pact orderFoundPact(PactDslWithProvider builder) {
        return builder
            .given("order with ID 501 exists")
            .uponReceiving("a request for order by ID")
                .path("/order/501")
                .method("GET")
            .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                    .numberValue("id", 501)
                    .stringValue("status", "PAID")
                    .decimalType("amount", 120.50)
                )
            .toPact(V4Pact.class);
    }

    @Pact(consumer = "payment-service", provider = "order-service")
    public V4Pact orderNotFoundPact(PactDslWithProvider builder) {
        return builder
            .given("order with ID 999 does not exist")
            .uponReceiving("a request for a non-existent order by ID")
                .path("/order/999")
                .method("GET")
            .willRespondWith()
                .status(404)
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                    .stringValue("error", "Order not found")
                )
            .toPact(V4Pact.class);
    }

    @Pact(consumer = "payment-service", provider = "order-service")
    public V4Pact createOrderPact(PactDslWithProvider builder) {
        return builder
            .given("valid order data")
            .uponReceiving("a request to create a new order")
                .path("/order")
                .method("POST")
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                    .stringValue("status", "PENDING")
                    .decimalType("amount", 99.99)
                )
            .willRespondWith()
                .status(201)
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                    .numberValue("id", 502)
                    .stringValue("status", "PENDING")
                    .decimalType("amount", 99.99)
                )
            .toPact(V4Pact.class);
    }

    @Pact(consumer = "payment-service", provider = "order-service")
    public V4Pact createOrderInvalidPact(PactDslWithProvider builder) {
        return builder
            .given("invalid order data")
            .uponReceiving("a request to create a new order with invalid data")
                .path("/order")
                .method("POST")
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                    .stringValue("status", "")
                    .decimalType("amount", -1.0)
                )
            .willRespondWith()
                .status(400)
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                    .stringValue("error", "Invalid order data")
                )
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "orderFoundPact")
    void testOrderFetch(MockServer mockServer) {
        RestAssured.baseURI = mockServer.getUrl();
        Response response = RestAssured
                .given()
                .log().all()
                .get("/order/501");
        String responseBody = response.then().extract().asString();
        System.out.println("Response body: " + responseBody);
        assertEquals(200, response.getStatusCode());
        assertEquals("PAID", response.jsonPath().getString("status"));
    }

    @Test
    @PactTestFor(pactMethod = "orderNotFoundPact")
    void testOrderNotFound(MockServer mockServer) {
        RestAssured.baseURI = mockServer.getUrl();
        Response response = RestAssured
                .given()
                .log().all()
                .get("/order/999");
        String responseBody = response.then().extract().asString();
        System.out.println("Response body: " + responseBody);
        assertEquals(404, response.getStatusCode());
        assertEquals("Order not found", response.jsonPath().getString("error"));
    }

    @Test
    @PactTestFor(pactMethod = "createOrderPact")
    void testCreateOrder(MockServer mockServer) {
        RestAssured.baseURI = mockServer.getUrl();
        Response response = RestAssured
                .given()
                .log().all()
                .contentType("application/json")
                .body("{\"status\":\"PENDING\",\"amount\":99.99}")
                .post("/order");
        String responseBody = response.then().extract().asString();
        System.out.println("Response body: " + responseBody);
        assertEquals(201, response.getStatusCode());
        assertEquals("PENDING", response.jsonPath().getString("status"));
    }

    @Test
    @PactTestFor(pactMethod = "createOrderInvalidPact")
    void testCreateOrderInvalid(MockServer mockServer) {
        RestAssured.baseURI = mockServer.getUrl();
        Response response = RestAssured
                .given()
                .log().all()
                .contentType("application/json")
                .body("{\"status\":\"\",\"amount\":-1.0}")
                .post("/order");
        String responseBody = response.then().extract().asString();
        System.out.println("Response body: " + responseBody);
        assertEquals(400, response.getStatusCode());
        assertEquals("Invalid order data", response.jsonPath().getString("error"));
    }
}
