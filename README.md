# Fully Completed Microservices Project

[![CodeQL](https://github.com/sandeep-singh-79/fully-completed-microservices-Java-Springboot/actions/workflows/github-code-scanning/codeql/badge.svg)](https://github.com/sandeep-singh-79/fully-completed-microservices-Java-Springboot/actions/workflows/github-code-scanning/codeql)
[![Pact CDC CI](https://github.com/sandeep-singh-79/fully-completed-microservices-Java-Springboot/actions/workflows/pact-cdc.yml/badge.svg)](https://github.com/sandeep-singh-79/fully-completed-microservices-Java-Springboot/actions/workflows/pact-cdc.yml)

## Overview

This repository contains a collection of fully completed microservices built with Spring Boot version 3.2.5 and Java 17. The project utilizes Spring Cloud version 2023.0.1 for implementing various distributed system patterns and features.

## Microservices Description

1. **Config Server**
   - Provides centralized configuration for all microservices.
   - Uses Spring Cloud Config Server.

2. **Customer Service**
   - Manages customer data and operations.
   - Integrated with Eureka Discovery.

3. **Discovery Service**
   - Service registry using Netflix Eureka.
   - Enables service discovery for other microservices.

4. **Gateway Service**
   - API Gateway for routing requests to appropriate microservices.
   - Uses Spring Cloud Gateway.
   - Includes distributed tracing and circuit breaker.

5. **Notification Service**
   - Handles notifications and alerts.
   - Uses Kafka for messaging.

6. **Order Service**
   - Manages orders and their statuses.
   - Integrated with Eureka Discovery.

7. **Payment Service**
   - Processes payments.
   - Uses Eureka Discovery and Zipkin for tracing.

8. **Product Service**
   - Manages product information.
   - Integrated with Eureka Discovery.

## Features

- **Service Discovery**: All microservices register with the Eureka server for easy discovery.
- **Centralized Configuration**: Configurations are managed centrally using the Spring Cloud Config Server.
- **API Gateway**: Spring Cloud Gateway is used for routing and handling cross-cutting concerns like security, monitoring, and resilience.
- **Distributed Tracing**: Zipkin is used for tracing requests across microservices.
- **Circuit Breaker**: Circuit breaking capabilities provided by Spring Cloud Circuit Breaker.
- **Messaging**: Kafka is used for asynchronous communication between microservices.

## Prerequisites

- Java 17 or later
- Maven or Gradle
- Docker (optional, for containerized deployment)

## Running the Microservices

1. **Clone the repository**
   ```sh
   git clone https://github.com/PramithaMJ/fully-completed-microservices.git
   cd fully-completed-microservices
   ```

2. **Start Config Server**
   ```sh
   cd config-server
   mvn spring-boot:run
   ```

3. **Start Discovery Service**
   ```sh
   cd discovery
   mvn spring-boot:run
   ```

4. **Start Other Microservices**
   Start the remaining microservices in any order. Ensure they are configured to register with the Discovery Service.

   ```sh
   cd <microservice-name>
   mvn spring-boot:run
   ```

## Configuration

Each microservice has its configuration properties defined in the `application.yml` or `application.properties` file. The Config Server properties should be specified in a central configuration repository.

## Deployment

To deploy the microservices using Docker, use the Dockerfile available in each microservice directory. You can build and run the Docker images as follows:

```sh
cd <microservice-name>
docker build -t <microservice-name>:latest .
docker run -d -p <port>:<container-port> <microservice-name>:latest
```

## Pact Contract Testing & CI/CD Integration

### Recent Changes

- Added consumer-driven contract tests for Order and Payment services using Pact V4 DSL and JUnit5.
- Configured Maven to output pact files to the root `pacts/` directory for both services.
- Set up a Pact Broker using Docker Compose (`pact-broker` service on port 9292).
- Added a robust GitHub Actions workflow (`.github/workflows/pact-cdc.yml`) that:
  - Starts the Pact Broker and WireMock using **docker-compose** (not the GitHub Actions `services:` block)
  - Waits for both services to be healthy before running tests
  - Runs contract tests for both services
  - Publishes pacts to the broker using the Docker-based Pact CLI (cross-platform reliability)
  - Validates the broker UI and ensures the presence of the latest pacts
  - Runs provider verification for both product and order services
  - Publishes provider verification results to the Pact Broker and validates their presence
  - Cleans up containers at the end of the workflow
- Updated `.gitignore` to only track root-level pact files and ignore build output pacts.
- Provider verification is performed using WireMock to simulate provider APIs, ensuring CDC tests are isolated and repeatable.

> **Note:** If you encounter container startup issues in CI, using `docker-compose` for service orchestration is more reliable than the `services:` block in GitHub Actions.

### Pact File Management

- Pact files in `/pacts` are overwritten on each test run and should be reviewed before committing.
- Do not manually edit pact files; always generate them via tests.
- Provider verification is the next recommended step for full CDC workflow.

### Simulating Provider Services with WireMock

- Provider APIs are simulated using WireMock, with stub mappings organized under `wiremock-stubs/mappings/`.
- Each contract scenario has a dedicated stub file (e.g., `order-by-id-get-501.json`, `product-by-id-get-101.json`).
- Stubs are maintained to match the latest consumer contracts and are used for provider verification in both local and CI environments.
- To update or add new scenarios, edit or add the relevant stub mapping files and ensure they match the contract in the `pacts/` folder.

## Consumer-Side Contract Generation & Publishing

1. **Run Consumer Contract Tests**
   - Order Service (as consumer of Product Service):
     ```powershell
     cd services/order
     mvn clean test -Dtest=ProductServiceContractTest
     ```
   - Payment Service (as consumer of Order Service):
     ```powershell
     cd ../payment
     mvn clean test -Dtest=OrderServiceContractTest
     ```
2. **Publish Pact Contracts to Pact Broker**
   - (Recommended: Use Docker for cross-platform reliability)
     ```powershell
     docker run --rm -v ${PWD}/pacts:/pacts pactfoundation/pact-cli:latest publish /pacts --broker-base-url http://localhost:9292 --broker-username admin --broker-password admin --consumer-app-version 1.0.0 --tag "${GITHUB_REF##*/}" --tag "dev"
     ```

### Provider-Side Contract Verification & Publishing

1. **Run Provider Verification Tests (and Publish Results)**
   - Product Service (as provider for Order Service):

     ```powershell
     cd services/product
     mvn test -Dtest=com.alibou.pact.provider.ProductProviderPactVerificationTest -Dpact.verifier.publishResults=true -Dpact.provider.tag=dev -Dpact.provider.version=<provider-version>
     ```

   - Order Service (as provider for Payment Service):

     ```powershell
     cd ../order
     mvn test -Dtest=com.alibou.pact.provider.OrderProviderPactVerificationTest -Dpact.verifier.publishResults=true -Dpact.provider.tag=dev -Dpact.provider.version=<provider-version>
     ```

   - Replace `<provider-version>` with the version string you want to use (e.g., the same as the consumer app version or a git SHA).

2. **Validate Provider Verification Results in the Pact Broker**
   - Use the Pact CLI to check if a version is safe to deploy (as in the GitHub Actions workflow):

     ```powershell
     docker run --rm --network=host pactfoundation/pact-cli:latest pact-broker can-i-deploy \
       --pacticipant <provider-service> --version <provider-version> \
       --broker-base-url http://localhost:9292 --broker-username admin --broker-password admin \
       --to-environment dev
     ```

   - This will confirm that all required contracts for the given version and environment have been verified and are deployable.

### Running All CDC Tests Locally (Recommended Flow)

1. Start the Pact Broker and WireMock:

   ```powershell
   docker-compose up -d pact-broker wiremock
   ```

2. Ensure the 'dev' environment exists in the Pact Broker (required for environment tagging and can-i-deploy):

   ```powershell
   docker run --rm --network=host pactfoundation/pact-cli:latest broker create-environment `
     --broker-base-url http://localhost:9292 --broker-username admin --broker-password admin `
     --name dev --display-name "Development" --production false
   ```
   > If the environment already exists, this command will return an error, which can be safely ignored.

3. Run all consumer contract tests to generate pact files:

   ```powershell
   cd services/order
   mvn clean test -Dtest=ProductServiceContractTest
   cd ../payment
   mvn clean test -Dtest=OrderServiceContractTest
   ```

4. Publish all generated pacts to the broker:

   ```powershell
   docker run --rm -v ${PWD}/pacts:/pacts pactfoundation/pact-cli:latest publish /pacts `
     --broker-base-url http://localhost:9292 --broker-username admin --broker-password admin `
     --consumer-app-version <consumer-version> --tag dev
   ```

5. Run provider verification tests (which will also publish verification results):

   ```powershell
   cd ../product
   mvn test -Dtest=com.alibou.pact.provider.ProductProviderPactVerificationTest -Dpact.verifier.publishResults=true -Dpact.provider.tag=dev -Dpact.provider.version=<provider-version>
   cd ../order
   mvn test -Dtest=com.alibou.pact.provider.OrderProviderPactVerificationTest -Dpact.verifier.publishResults=true -Dpact.provider.tag=dev -Dpact.provider.version=<provider-version>
   ```

6. Validate provider verification results in the Pact Broker:

   ```powershell
   docker run --rm --network=host pactfoundation/pact-cli:latest pact-broker can-i-deploy `
     --pacticipant <provider-service> --version <provider-version> `
     --broker-base-url http://localhost:9292 --broker-username admin --broker-password admin `
     --to-environment dev
   ```

7. Visit [http://localhost:9292](http://localhost:9292) (admin/admin) to view contracts and verification status.

8. When finished, stop containers:

   ```powershell
   docker-compose down
   ```

### CI/CD: GitHub Actions Pipeline

- The `.github/workflows/pact-cdc.yml` workflow automates:
  - Starting Pact Broker and WireMock using docker-compose
  - Waiting for service readiness
  - Ensuring the target environment exists in the broker (auto-creation if needed)
  - Running all consumer contract tests and generating pact files
  - Publishing pacts to the broker with version and environment tags
  - Running provider verification for both product and order services, publishing results with version and environment tags
  - Validating the presence and verification status of all contracts in the broker
  - Running `can-i-deploy` checks to ensure provider verification results are present and contracts are deployable for the target environment
  - Uploading pact files and provider logs as workflow artifacts
  - Cleaning up containers at the end
- The workflow runs on every push and pull request to `main`.
- See the workflow file for details and step-by-step automation.

---
For more details, see the comments in the workflow and contract test files.

## Contributing

1. Fork the repository.
2. Create your feature branch (`git checkout -b feature/your-feature`).
3. Commit your changes (`git commit -m 'Add some feature'`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Open a pull request.

## License

This project is licensed under the MIT License.

## Contact

For any questions or feedback, please open an issue in the repository or contact:

Sandeep Singh
- Blog & Site: [https://ssandeep79.wixsite.com/experimentsintesting](https://ssandeep79.wixsite.com/experimentsintesting)
- GitHub: [https://github.com/sandeep-singh-79/fully-completed-microservices-Java-Springboot](https://github.com/sandeep-singh-79/fully-completed-microservices-Java-Springboot)

---

### Attribution

This repository is a fork of the original project by Pramitha Jayasooriya:
- [https://github.com/PramithaMJ/fully-completed-microservices](https://github.com/PramithaMJ/fully-completed-microservices)
