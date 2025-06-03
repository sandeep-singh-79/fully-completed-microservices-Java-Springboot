# Fully Completed Microservices Project

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
  - Starts the Pact Broker as a service
  - Runs contract tests for both services
  - Publishes pacts to the broker using the Docker-based Pact CLI (cross-platform reliability)
  - Validates the broker UI and ensures the presence of the latest pacts
- Updated `.gitignore` to only track root-level pact files and ignore build output pacts.

### Pact File Management
- Pact files in `/pacts` are overwritten on each test run and should be reviewed before committing.
- Do not manually edit pact files; always generate them via tests.
- Provider verification is the next recommended step for full CDC workflow.

### How to Run Locally
1. Start the Pact Broker:
   ```powershell
   docker-compose up pact-broker
   ```
2. Run contract tests for both services to generate pacts:
   ```powershell
   cd services/order; mvn clean test -Dtest=ProductServiceContractTest
   cd ../payment; mvn clean test -Dtest=OrderServiceContractTest
   ```
3. Publish pacts (if Pact CLI is not working on Windows, use Docker):
   ```powershell
   docker run --rm -v ${PWD}/pacts:/pacts pactfoundation/pact-cli:latest publish /pacts --broker-base-url http://localhost:9292 --broker-username admin --broker-password admin --consumer-app-version 1.0.0
   ```
4. Visit [http://localhost:9292](http://localhost:9292) (admin/admin) to view contracts.

### CI/CD
- See `.github/workflows/pact-cdc.yml` for full automation of contract testing and publishing.

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
