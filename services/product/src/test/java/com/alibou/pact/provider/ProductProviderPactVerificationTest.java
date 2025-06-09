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

@Provider("product-service")
@Consumer("order-service")
@PactBroker(host = "localhost", port = "9292", authentication = @PactBrokerAuth(username = "admin", password = "admin"))
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductProviderPactVerificationTest {
    @BeforeEach
    public void setup(PactVerificationContext context) {
        System.setProperty("pact.verifier.publishResults", "true");
        System.setProperty("pact.provider.host", "localhost");
        System.setProperty("pact.provider.port", "8080");
        System.setProperty("pact.verifier.logLevel", "DEBUG");
        if (context != null) {
            context.setTarget(new au.com.dius.pact.provider.junit5.HttpTestTarget("localhost", 8080));
        }
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    public void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("product with ID 101 exists")
    public void toProduct101ExistsState() {
        // No-op: WireMock stubs already handle this
    }

    @State("product with ID 202 exists and has zero price")
    public void toProduct202ZeroPriceState() {
        // No-op: WireMock stubs already handle this
    }

    @State("product with ID 999 does not exist")
    public void toProduct999NotFoundState() {
        // No-op: WireMock stubs already handle this
    }

    @State("product ID is invalid")
    public void toProductIdInvalidState() {
        // No-op: WireMock stubs already handle this
    }
}
