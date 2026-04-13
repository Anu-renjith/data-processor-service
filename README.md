# data-processor-service
Enterprise-grade Spring Boot Data Pipeline with JWT Security, Asynchronous Processing, and Caffeine Caching

#Architecture

POST /api/data/submit
↓
Validate Request (@Valid)
↓
Save to MySQL (status: PENDING)
↓
Push to In-Memory Queue (LinkedBlockingQueue)
↓
Return 202 Accepted immediately (client doesn't wait)
↓
Scheduler triggers every 15 seconds
↓
Poll from Queue → fallback to DB if queue empty
↓
Async Thread (processAsync)
↓
POST to External HTTPS API (JSONPlaceholder)
↓
Update status → SUCCESS / FAILED

GET /api/data/{id}
↓
Check Caffeine Cache (TTL: 5 minutes)
↓
Cache Hit  → return from memory (no DB call)
Cache Miss → query MySQL → store in cache → return


#Tech Stack
Java 17
Spring Boot 3.5.13
Spring Security + JWT (jjwt 0.13.0)
Spring Data JPA + Hibernate
MySQL 8.0
Caffeine Cache
Lombok
Docker + Docker Compose
JUnit 5 + Mockito

#Project Structure
src/
├── main/
│   ├── java/com/example/datapipeline/
│   │   ├── config/          # Security, Cache configuration
│   │   ├── constants/       # DataStatus enum (PENDING, PROCESSING, SUCCESS, FAILED)
│   │   ├── controller/      # AuthController, DataController
│   │   ├── dto/             # Request/Response DTOs
│   │   ├── entity/          # DataEntity, User, Role
│   │   ├── exception/       # GlobalExceptionHandler, custom exceptions
│   │   ├── payload/         # ErrorDetails
│   │   ├── repository/      # JPA repositories
│   │   ├── scheduler/       # DataScheduler
│   │   ├── security/        # JWT filter, provider
│   │   └── service/         # DataService, AuthService, QueueService, AsyncProcessingService
│   └── resources/
│       └── application.properties
└── test/
├── java/
│   ├── repository/      # DataRepositoryTest
│   ├── service/         # DataServiceImplTest
│   └── controller/      # DataControllerTest
└── resources/
└── application.properties  # H2 in-memory DB for tests


#Api end points
Method           URL               Auth                Description
POST           /api/auth/register   No                 Register new user   
POST           /api/auth/login      No                 Login and get JWT token
POST           /api/data/submit     JWT                Submit data to queue
GET            /api/data/{id}       JWT                Get data by ID (cached)
GET             /actuator/health    No                 Health check with DB status

#Data Status Flow
PENDING → PROCESSING → SUCCESS → FAILED

#Run with Docker

Step 1 — Create .env file in project root:
envDB_USER=root
DB_PASSWORD=root
JWT_SECRET=1430434d0fd3fd395785ac50d8f9d4dfa07fbe237947f5bd479532ba40baa277

Step 2 — Build JAR:
./mvnw package -DskipTests
Step 3 — Start containers:
docker compose up --build

Step 4 — Verify:
http://localhost:8080/actuator/health
Expected response:
json{
"status": "UP",
"components": {
"db": { "status": "UP" }
}
}


1. Register
   bashPOST http://localhost:8080/api/auth/register
   Content-Type: application/json

{
"name": "Anu",
"username": "anu",
"email": "anu@gmail.com",
"password": "password123"
}

2. Login
   bashPOST http://localhost:8080/api/auth/login
   Content-Type: application/json

{
"usernameOrEmail": "anu",
"password": "password123"
}
Response:
json{ "accessToken": "eyJhbGci..." }


3. Submit Data
   bashPOST http://localhost:8080/api/data/submit
   Authorization: Bearer <token>
   Content-Type: application/json

{
"name": "test",
"email": "test@gmail.com"
}
Response: 202 Accepted — "Queued"


4. Get Data (Cached)
   bashGET http://localhost:8080/api/data/1
   Authorization: Bearer <token>

First call → fetches from MySQL
Second call within 5 min → returns from Caffeine cache


#Cache Configuration
Property          Value
Engine            Caffeine
TTL               5 minutes
Max size          1000 entries
EvictionOn        status update via @CacheEvict

#Scheduled Processing

Runs every 15 seconds
Checks in-memory queue first
Falls back to DB query for PENDING records if queue is empty
Processes asynchronously on separate thread pool
Posts to https://jsonplaceholder.typicode.com/posts (mock external API)
In production: replace with real payment gateway (Razorpay/Stripe)


#Error Handling
Scenario                HTTP Status
Resource not found         404
Validation failed          400
Wrong Content-Type         415
Malformed JSON             400
Access denied              401
Server error               500

#Docker Commands
bash# Start in background
docker compose up -d --build

#view logs
docker compose logs -f app

#stop
docker compose down

#stop and delete db data
docker compose down -v

#checking running containers
docker ps
