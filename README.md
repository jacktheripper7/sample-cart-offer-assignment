# Cart Offer Service

## 📌 Prerequisites
- **JDK 11**  
- **Docker**  

---

## 🚀 How to bring the mock service up
```bash
cd mockserver  
docker compose up
````

The mock server will start at **port 1080**.

---

## 🛠️ How to bring the service up

```bash
./mvnw clean install -DskipTests  
java -jar target/simple-springboot-app-0.0.1-SNAPSHOT.jar
```

The Spring Boot server will start at **port 9001**.

---

## 🧪 How to run the tests

```bash
./mvnw test
```

---

## 🔄 What Changed

### 1. AutowiredController

* **Old version**:

    * Controller directly held offers in a `List`.
    * Contained both business logic and external API calls (segment lookup).

* **New version**:

    * Slimmed down to handle only HTTP request/response mapping.
    * Delegates core logic to `OfferService` and `OfferRepository`.
    * Achieves clear separation of concerns: **Controller → Service → Repository**.

---

### 2. API Responses

Introduced structured response objects for consistency:

* **ApiResponse** →

  ```json
  { "response_msg": "success" | "error" }
  ```
* **ApplyOfferResponse** →

  ```json
  { "cart_value": <final_value> }
  ```

This makes test validation easier with Rest Assured.

---

### 3. Dependencies (`pom.xml`)

* Added **Rest Assured (5.3.0)** → for clean, fluent API testing.
* Replaced old `HttpURLConnection`-based testing.
* Other dependencies unchanged.

---

### 4. `initializerJson.json` (MockServer setup)

* Added mappings for **custom segments** (`gold`, `silver`).
* Enables new test scenarios for user segments beyond the default **p1/p2/p3**.

---

## 🆕 New Files

### 1. Core Logic

* **`OfferRepository`** → In-memory store for offers, enforces *"first offer wins"* policy.
* **`OfferService`** → Handles validations, applies discounts, orchestrates business rules.

👉 Together, they improve readability, maintainability, and follow real-world layered architecture.

### 2. Test Support

* **`BaseOfferTest`** → Provides common Rest Assured setup (`@Before` with base URI/port).
* Avoids duplication across multiple test classes.

---

## 📋 Tests & Scenarios

These are new testcases added for the assignment, the old testcases are not modified.

### ✅ `OfferApiTests`

* Valid **FlatX** and **Flat%** offers.
* Invalid offer type rejection.
* Validation for empty customer segments.
* Multi-segment support.

---

### ✅ `CartApplyOfferTests`

* **Positive**: FlatX, Flat%, multiple restaurants, multiple segments, etc.
* **Negative**: No offers, wrong segment/restaurant, invalid type, user not in any segment.
* **Edge**: Zero cart value, discount > cart, zero discount, conflicting offers, precision checks, large values.
* **Custom**: Gold/silver segment cases.
* **Utility**: Clear offers endpoint, alternative syntax (`FLATP`).

---

### ✅ `MultiSegmentOfferTests`

Focused scenarios for **multi-segment offers**:

* Same restaurant, different discounts per segment.
* Overlapping multi-segment offers.
* Invalid/empty multi-segment offers.
* Zero cart value edge cases.

---

## 🏗️ Architecture Flow

```mermaid
flowchart LR
    A[Client & Tests (Rest Assured)] --> B[Controller: AutowiredController]
    B --> C[Service: OfferService]
    C --> D[Repository: OfferRepository]
    C --> E[MockServer: User Segment API]

    D -->|Stores & Retrieves Offers| C
    E -->|Provides Segment Info| C
````

* **Client & Tests** → Rest Assured test cases trigger HTTP requests.
* **Controller** → Maps the request (`/offer`, `/cart/apply_offer`).
* **Service** → Validates, applies discount logic, decides response.
* **Repository** → Stores offers (in-memory, first-offer-wins).
* **MockServer** → Simulates external user segment API.



## 🤖 Use of AI

AI was used **minimally** for:

* Resolving **dependency compatibility** (Rest Assured with JDK 11).
* Adding **inline comments** for easier readability.
* Suggesting **test case structure and coverage ideas**.

⚡ **Core logic, design decisions, and the majority of implementation were done by me.**

---
