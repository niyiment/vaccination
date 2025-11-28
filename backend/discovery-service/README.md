# Discovery Service (Eureka Server)
Service discovery and registration server enabling microservices to discover each other and register themselves.

## Quick Start

### Local Development
```bash
mvn clean package
java -jar target/discovery-service-0.0.1-SNAPSHOT.jar
```
Access dashboard: http://localhost:8761 (eurekauser/eurekapass123)

### Docker 
```bash
docker build -t discovery-service .
docker-compose up -d
```
## Endpoints
- `/eureka/apps` All registered services
- `/eureka/apps/{applicationName}` Registered services of a particular application
- `/actuator/health` Health check
- `/actuator/info` Application info
- `/actuator/prometheus` Prometheus metrics