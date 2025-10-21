
# GitHub Search Application

A modern, Spring Boot WebFlux application for searching GitHub repositories with advanced filtering, custom popularity scoring, and robust error handling. This project intends to follow best practices in reactive programming, configuration, validation, and test coverage.

---

## Features

- **Reactive API**: Built with Spring WebFlux for non-blocking, asynchronous operations.
- **Popularity Score**: Calculates a custom popularity score for each repository based on stars, forks, and recency.
- **Advanced Filtering**: Filter by language, creation date, and page. ALL PARAMS ARE OPTIONAL. Page Number defaulted to 1.
- **Pagination**: Supports page number as part of User Request to fetch results in paginated fashion. Default per-page is 30, configured in Application yaml. Can be upto 99 (Permissible by GitHub)
- **Rate Limit Awareness**: Handles GitHub API rate limits gracefully.
- **Validation**: Jakarta Bean Validation for all incoming requests.
- **Centralized Error Handling**: Consistent, structured error responses for all error scenarios.
- **Test Coverage**: Close to 100% line coverage with unit and integration tests.

---

## API Request Handling

This application will successfully process requests with the following payloads:

- `{ "language": null, "earliestCreatedDate": null, "pageNumber": null }`
- `{}` (an empty object)

Any extra fields provided in the request, other than `language`, `earliestCreatedDate`, and `pageNumber`, will be ignored and will not affect processing.

### Field Validation and Defaults

- The fields `language`, `earliestCreatedDate`, and `pageNumber` are validated **only if the user provides a value**.
- If these fields are omitted or set to `null`, default values or queries will be used to fetch data from the API.

This ensures robust and flexible request handling for a variety of client payloads.


## How the Popularity Score is Calculated

The popularity score for each GitHub repository is calculated using a simple weighted formula:

- **60%** weight for the number of stars
- **30%** weight for the number of forks
- **10%** weight for how recently the repository was updated

In layman's terms:

> **Popularity Score = (Stars × 0.6) + (Forks × 0.3) + (Recency Factor × 10)**

Where:
- **Recency Factor** is higher for recently updated repositories and lower for older ones. It is calculated as `1 / (1 + days since last update)`, so a repository updated today gets the maximum boost, and the boost decreases as the last update gets older.

This means:
- Repositories with more stars and forks are considered more popular.
- Recently updated repositories get a small extra boost.

The formula is implemented in the `PopularityScoreCalculator` helper class.

---


## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- **GitHub Personal Access Token (PAT)**: Required for authenticated API requests. [Create a PAT here](https://github.com/settings/tokens) with `public_repo` scope.

### Setup
1. **Clone the repository:**
  ```sh
  git clone <your-repo-url>
  cd githubsearch
  ```
2. **Configure your GitHub PAT:**
- Set your PAT in `src/main/resources/application.yml` under `github.token`:
  ```yaml
  github:
   token: <your-github-pat>
  ```
- **Never commit your PAT to version control.**
3. **Build the project:**
  ```sh
  ./mvnw clean install
  ```
4. **Run the application:**
  ```sh
  ./mvnw spring-boot:run
  ```
The app will start on [http://localhost:8081](http://localhost:8081)

---


## API Usage

### Endpoint
- **POST** `/api/gitrepo/search`
- **Content-Type:** `application/json`

#### Request Example
```json
{
  "language": "java",
  "earliestCreatedDate": "2020-01-01T00:00:00Z",
  "pageNumber": 1
}
```
- `language`: Programming language (optional)
- `earliestCreatedDate`: ISO date string (optional)
- `pageNumber`: Page number (default: 1)

#### Response Example
```json
{
  "totalCount": 12345,
  "incompleteResults": false,
  "hasNextPage": true,
  "pageNumber": 1,
  "nextPageNumber": 2,
  "items": [
    {
      "id": 123,
      "name": "spring-boot",
      "description": "Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications.",
      "language": "Java",
      "stargazerCount": 65000,
      "forksCount": 20000,
      "htmlUrl": "https://github.com/spring-projects/spring-boot",
      "updatedAt": "2025-10-15T12:34:56Z",
      "createdAt": "2014-01-01T00:00:00Z",
      "popularityScore": 98.7
    }
  ]
}
```

#### Error Responses
- **400 Bad Request**: Validation or malformed input
- **429 Too Many Requests**: GitHub API rate limit exceeded
- **500 Internal Server Error**: Unexpected server error

---

spring:  port: 8081
github:    web-application-type: reactive

## Configuration

All configuration is in `src/main/resources/application.yml`:
```yaml
server:
  port: 8081
spring:
  application:
    name: github-search
  main:
    web-application-type: reactive
github:
  token: <your-github-pat> <Please generate before Testing via POSTMan or similar apps>
  api:
    base-url: https://api.github.com
    default-query: "Q"
    default-per-page: 30
    default-page: 1
    retry:
      attempts: 3
      backoff-seconds: 1
logging:
  level:
    root: INFO
    "com.example.githubsearch": DEBUG
resilience4j.ratelimiter:
  instances:
    githubApiLimiter:
      limit-for-period: 5000
      limit-refresh-period: 1h
      timeout-duration: 0
```

---


## Testing

- **Run all tests:**
  ```sh
  ./mvnw test
  ```
- Close to 100% line coverage is enforced for all business logic, helpers, mappers, models, configuration, and integration layers.
- See `src/test/java/com/example/githubsearch/` for all test classes.

---


## Project Structure
- `controller/` — REST API endpoints
- `service/` — Business logic and GitHub API client
- `model/` — Internal domain models
- `dto/` — Data transfer objects (API contracts)
- `mapper/` — MapStruct mappers
- `config/` — Configuration classes
- `exception/` — Custom exceptions and global error handling
- `service/impl/helper/` — Helper utilities (query builder, score calculator, response handler)
- `src/test/` — Unit and integration tests

---

## Security Notice
- **A GitHub Personal Access Token (PAT) is required** for all API requests. Without a valid PAT, requests to the GitHub API will fail or be severely rate-limited.