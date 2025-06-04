package com.alibou.pact.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Consumer;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@Provider("order-service")
@Consumer("payment-service")
@PactBroker(host = "localhost", port = "9292", authentication = @PactBrokerAuth(username = "admin", password = "admin"))
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrderProviderPactVerificationTest {

    @BeforeEach
    void before (PactVerificationContext context) {
        context.setTarget(new au.com.dius.pact.provider.junit5.HttpTestTarget("localhost", 8080, "/"));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerification (PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("order with ID 501 exists")
    public void orderWithId501Exists () {
        // Set up DB or mocks so that order 501 exists with amount 120.5, status PAID
    }

    @State("invalid order data")
    public void invalidOrderData () {
        // Set up DB or mocks so that invalid order data will trigger a 400 response
    }

    @State("order with ID 999 does not exist")
    public void orderWithId999DoesNotExist () {
        // Set up DB or mocks so that order 999 does not exist and returns 404
    }

    @State("valid order data")
    public void validOrderData () {
        // Set up DB or mocks so that valid order data will trigger a 201 response
    }
}
