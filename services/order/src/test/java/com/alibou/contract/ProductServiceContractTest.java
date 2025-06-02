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
public class ProductServiceContractTest {

    @Pact(consumer = "order-service", provider = "product-service")
    public V4Pact productFoundPact(PactDslWithProvider builder) {
        return builder
            .given("product with ID 101 exists")
            .uponReceiving("a request for product by ID")
                .path("/product/101")
                .method("GET")
            .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                    .numberValue("id", 101)
                    .stringValue("name", "iPhone 14")
                    .decimalType("price", 899.99)
                )
            .toPact(V4Pact.class);
    }

    @Pact(consumer = "order-service", provider = "product-service")
    public V4Pact productNotFoundPact(PactDslWithProvider builder) {
        return builder
            .given("product with ID 999 does not exist")
            .uponReceiving("a request for a non-existent product by ID")
                .path("/product/999")
                .method("GET")
            .willRespondWith()
                .status(404)
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                    .stringValue("error", "Product not found")
                )
            .toPact(V4Pact.class);
    }

    @Pact(consumer = "order-service", provider = "product-service")
    public V4Pact invalidProductIdPact(PactDslWithProvider builder) {
        return builder
            .given("product ID is invalid")
            .uponReceiving("a request with an invalid product ID")
                .path("/product/abc")
                .method("GET")
            .willRespondWith()
                .status(400)
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                    .stringValue("error", "Invalid product ID")
                )
            .toPact(V4Pact.class);
    }

    @Pact(consumer = "order-service", provider = "product-service")
    public V4Pact zeroPriceProductPact(PactDslWithProvider builder) {
        return builder
            .given("product with ID 202 exists and has zero price")
            .uponReceiving("a request for a product with zero price")
                .path("/product/202")
                .method("GET")
            .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                    .numberValue("id", 202)
                    .stringValue("name", "Free Sample")
                    .decimalType("price", 0.0)
                )
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "productFoundPact")
    void testProductFetch(MockServer mockServer) {
        RestAssured.baseURI = mockServer.getUrl();

        Response response = RestAssured
                .given()
                .log().all()
                .get("/product/101");

        String responseBody = response.then().extract().asString();
        System.out.println("Response body: " + responseBody);

        assertEquals(200, response.getStatusCode());
        assertEquals("iPhone 14", response.jsonPath().getString("name"));
    }

    @Test
    @PactTestFor(pactMethod = "productNotFoundPact")
    void testProductNotFound(MockServer mockServer) {
        RestAssured.baseURI = mockServer.getUrl();

        Response response = RestAssured
                .given()
                .log().all()
                .get("/product/999");

        String responseBody = response.then().extract().asString();
        System.out.println("Response body: " + responseBody);

        assertEquals(404, response.getStatusCode());
        assertEquals("Product not found", response.jsonPath().getString("error"));
    }

    @Test
    @PactTestFor(pactMethod = "invalidProductIdPact")
    void testInvalidProductId(MockServer mockServer) {
        RestAssured.baseURI = mockServer.getUrl();

        Response response = RestAssured
                .given()
                .log().all()
                .get("/product/abc");

        String responseBody = response.then().extract().asString();
        System.out.println("Response body: " + responseBody);

        assertEquals(400, response.getStatusCode());
        assertEquals("Invalid product ID", response.jsonPath().getString("error"));
    }

    @Test
    @PactTestFor(pactMethod = "zeroPriceProductPact")
    void testZeroPriceProduct(MockServer mockServer) {
        RestAssured.baseURI = mockServer.getUrl();

        Response response = RestAssured
                .given()
                .log().all()
                .get("/product/202");

        String responseBody = response.then().extract().asString();
        System.out.println("Response body: " + responseBody);

        assertEquals(200, response.getStatusCode());
        assertEquals(0.0, response.jsonPath().getDouble("price"));
        assertEquals("Free Sample", response.jsonPath().getString("name"));
    }
}