# Secure Bookshop · RBS 2024/2025

![Java](https://img.shields.io/badge/Java-8-3F6B57?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7-3F6B57?style=flat-square&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-5.7-5D4E7E?style=flat-square&logo=springsecurity&logoColor=white)
![OWASP](https://img.shields.io/badge/OWASP-Top%2010-5D4E7E?style=flat-square&logo=owasp&logoColor=white)

Course project for **Secure Software Development** (*Razvoj bezbednog softvera*, RBS) at the University of Belgrade, school year 2024/2025.

## About the project

The starting point was an **intentionally vulnerable web application**: a small online bookshop
(it literally shipped as *"Not very secure Bookshop"*) where users browse, rate, comment on and buy books,
and managers maintain the catalogue, users and discount vouchers.

The goal of the project was to **find the vulnerabilities, exploit them, and fix the application**
so that it is no longer exploitable. The work covered:

- **SQL injection**: every query built from user input
- **Cross-site scripting (XSS)**: stored and DOM-based
- **Cross-site request forgery (CSRF)**
- **Access control**: roles, permissions and per-object ownership rules
- **Logging and auditing**: who changed what, without leaking secrets
- **Authentication**: password storage and brute-force protection

The application was also analysed with **OWASP ZAP** and **SonarQube**, the tools used in the course.

## Architecture

```mermaid
flowchart LR
    B["Browser<br/>Thymeleaf pages + JS"] -->|"HTTP request<br/>session cookie + X-XSRF-TOKEN"| F

    subgraph APP["Spring Boot application"]
        direction LR
        F["Spring Security filter chain<br/>CSRF · login · session · CSP headers"]
        A["DatabaseAuthenticationProvider<br/>BCrypt · login throttling"]
        C["Controllers<br/>@PreAuthorize (permissions + ownership)"]
        R["Repositories<br/>JdbcTemplate, bind parameters only"]
        L["AuditLogger"]
        F -->|"/perform-login"| A
        F --> C
        C --> R
        A -.-> L
        R -.-> L
    end

    R --> DB[("H2 database<br/>users · roles · permissions<br/>books · ratings · comments · vouchers")]
    L --> LOG[/"logs/audit.log"/]
    A --> DB
```

### Role-based access control

Users get **roles**, roles get **permissions**, and every endpoint checks a permission
(`@PreAuthorize("hasAuthority('...')")`). Profile endpoints additionally check **ownership**,
so a buyer can edit only their own profile.

```mermaid
flowchart LR
    U1(["book"]) --> ADMIN
    U2(["tom"]) --> MANAGER
    U3(["bruce"]) --> BUYER
    U4(["peter"]) --> BUYER

    ADMIN --> P1["all permissions"]
    MANAGER --> P2["VIEW_BOOK_LIST · CREATE_BOOK · ADD_COMMENT<br/>VIEW_PERSONS_LIST · UPDATE_PERSON · VIEW_MY_PROFILE<br/>BUY_BOOK · NEW_VOUCHER"]
    BUYER --> P3["VIEW_BOOK_LIST · ADD_COMMENT · RATE_BOOK<br/>BUY_BOOK · VIEW_MY_PROFILE<br/>UPDATE_PERSON (own profile only)"]
```

| Permission | Admin | Manager | Buyer |
|---|:---:|:---:|:---:|
| View and search books | ✓ | ✓ | ✓ |
| Add a book | ✓ | ✓ | |
| Comment | ✓ | ✓ | ✓ |
| Rate a book | ✓ | | ✓ |
| Buy a book | ✓ | ✓ | ✓ |
| View the user list | ✓ | ✓ | |
| View another user's profile | ✓ | | |
| Edit / delete a profile | any | any | own |
| Create vouchers | ✓ | ✓ | |

## Vulnerabilities and fixes

| Vulnerability | Where it was | Fix |
|---|---|---|
| **SQL injection** | Login, book and user search, ratings, comments, permissions: SQL built by string concatenation | All repositories use `JdbcTemplate` with bind parameters. `LIKE` wildcards are escaped. Voucher ownership is an exact match instead of `LIKE '%username%'` |
| **Stored XSS** | Comments rendered as raw HTML (`th:utext`) | Output encoding (`th:text`), comments stored as typed |
| **DOM XSS** | Search results, the search term and the user table written with `innerHTML` | `textContent` and `encodeURIComponent` |
| **XSS defence in depth** | Inline scripts everywhere | All scripts and styles moved to static files, strict **Content-Security-Policy** without `unsafe-inline` |
| **CSRF** | CSRF protection disabled, CORS allowed credentialed requests from another origin | Spring Security CSRF tokens (`X-XSRF-TOKEN` header for `fetch`), `SameSite=Strict` session cookie, CORS filter removed. See [`csrf-exploit/`](csrf-exploit) |
| **Broken access control** | Any signed-in user could call any endpoint | Users → roles → permissions, `@PreAuthorize` on every endpoint, ownership checks for profiles |
| **Plain-text passwords** | Passwords stored and compared as plain text, and written into the failed-login message | BCrypt (cost 12), identical error for unknown user and wrong password, a BCrypt check runs even for unknown usernames (no timing difference) |
| **Brute force** | Unlimited login attempts | Account locked for 15 minutes after 5 failed attempts |
| **Session security** | Session cookie readable from JavaScript, no rotation | `HttpOnly`, `SameSite=Strict`, new session id after login, 30 min timeout |
| **Insufficient logging** | No audit trail, and the logged user id was cached from the first request | Audit records (`userId` + action, before/after values for changes) in a separate rotating `logs/audit.log`. CR/LF stripped against log forging |
| **Business logic** | A voucher could be redeemed twice by parallel requests | A voucher is checked and spent in a single atomic `DELETE` |

## Running locally

Requirements: **JDK 8** or newer. Maven is included through the wrapper.

```bash
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

Open http://localhost:8080. The in-memory database is recreated from `schema.sql` and `data.sql` on every start.

| Username | Password | Role |
|---|---|---|
| `book` | `work` | Admin |
| `tom` | `guessmeifyoucan` | Manager |
| `bruce` | `wayne` | Buyer |
| `peter` | `security_rules` | Buyer |

Static analysis with SonarQube (a local SonarQube server, e.g. the `sonarqube` Docker image):

```bash
./mvnw sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=<token>
```

## Project structure


<!-- project-tree:start -->
<p>
<a href="https://github.com/LazarDavidovic98/rbs2024-2025" title="rbs2024-2025"><img src=".github/readme-tree/00.svg" alt="rbs2024-2025" align="top"></a><br>
<a href="https://github.com/LazarDavidovic98/rbs2024-2025/tree/main/src/main/java/com/zuehlke/securesoftwaredevelopment" title="src/main/java/com/zuehlke/securesoftwaredevelopment/"><img src=".github/readme-tree/01.svg" alt="src/main/java/com/zuehlke/securesoftwaredevelopment/" align="top"></a><br>
<a href="https://github.com/LazarDavidovic98/rbs2024-2025/blob/main/src/main/java/com/zuehlke/securesoftwaredevelopment/SecureSoftwareDevelopmentApplication.java" title="src/main/java/com/zuehlke/securesoftwaredevelopment/SecureSoftwareDevelopmentApplication.java"><img src=".github/readme-tree/02.svg" alt="src/main/java/com/zuehlke/securesoftwaredevelopment/SecureSoftwareDevelopmentApplication.java — entry point" align="top"></a><br>
<a href="https://github.com/LazarDavidovic98/rbs2024-2025/tree/main/src/main/java/com/zuehlke/securesoftwaredevelopment/config" title="src/main/java/com/zuehlke/securesoftwaredevelopment/config/"><img src=".github/readme-tree/03.svg" alt="src/main/java/com/zuehlke/securesoftwaredevelopment/config/ — SecurityConfig, auth provider, AuditLogger" align="top"></a><br>
<a href="https://github.com/LazarDavidovic98/rbs2024-2025/tree/main/src/main/java/com/zuehlke/securesoftwaredevelopment/controller" title="src/main/java/com/zuehlke/securesoftwaredevelopment/controller/"><img src=".github/readme-tree/04.svg" alt="src/main/java/com/zuehlke/securesoftwaredevelopment/controller/ — books, comments, ratings, persons, vouchers" align="top"></a><br>
<a href="https://github.com/LazarDavidovic98/rbs2024-2025/tree/main/src/main/java/com/zuehlke/securesoftwaredevelopment/domain" title="src/main/java/com/zuehlke/securesoftwaredevelopment/domain/"><img src=".github/readme-tree/05.svg" alt="src/main/java/com/zuehlke/securesoftwaredevelopment/domain/ — plain domain objects" align="top"></a><br>
<a href="https://github.com/LazarDavidovic98/rbs2024-2025/tree/main/src/main/java/com/zuehlke/securesoftwaredevelopment/repository" title="src/main/java/com/zuehlke/securesoftwaredevelopment/repository/"><img src=".github/readme-tree/06.svg" alt="src/main/java/com/zuehlke/securesoftwaredevelopment/repository/ — JdbcTemplate, parameterized SQL only" align="top"></a><br>
<a href="https://github.com/LazarDavidovic98/rbs2024-2025/tree/main/src/main/java/com/zuehlke/securesoftwaredevelopment/service" title="src/main/java/com/zuehlke/securesoftwaredevelopment/service/"><img src=".github/readme-tree/07.svg" alt="src/main/java/com/zuehlke/securesoftwaredevelopment/service/ — permission resolution, login throttling" align="top"></a><br>
<a href="https://github.com/LazarDavidovic98/rbs2024-2025/tree/main/src/main/resources" title="src/main/resources/"><img src=".github/readme-tree/08.svg" alt="src/main/resources/" align="top"></a><br>
<a href="https://github.com/LazarDavidovic98/rbs2024-2025/blob/main/src/main/resources/schema.sql" title="src/main/resources/schema.sql"><img src=".github/readme-tree/09.svg" alt="src/main/resources/schema.sql — database schema" align="top"></a><br>
<a href="https://github.com/LazarDavidovic98/rbs2024-2025/blob/main/src/main/resources/data.sql" title="src/main/resources/data.sql"><img src=".github/readme-tree/10.svg" alt="src/main/resources/data.sql — demo data" align="top"></a><br>
<a href="https://github.com/LazarDavidovic98/rbs2024-2025/blob/main/src/main/resources/logback-spring.xml" title="src/main/resources/logback-spring.xml"><img src=".github/readme-tree/11.svg" alt="src/main/resources/logback-spring.xml — console + separate audit log" align="top"></a><br>
<a href="https://github.com/LazarDavidovic98/rbs2024-2025/tree/main/src/main/resources/templates" title="src/main/resources/templates/"><img src=".github/readme-tree/12.svg" alt="src/main/resources/templates/ — Thymeleaf pages" align="top"></a><br>
<a href="https://github.com/LazarDavidovic98/rbs2024-2025/tree/main/src/main/resources/static" title="src/main/resources/static/"><img src=".github/readme-tree/13.svg" alt="src/main/resources/static/ — JavaScript, CSS, images (no inline scripts)" align="top"></a><br>
<a href="https://github.com/LazarDavidovic98/rbs2024-2025/blob/main/src/main/resources/application.properties" title="src/main/resources/application.properties"><img src=".github/readme-tree/14.svg" alt="src/main/resources/application.properties — app settings" align="top"></a><br>
<a href="https://github.com/LazarDavidovic98/rbs2024-2025/tree/main/src/test/java" title="src/test/java/"><img src=".github/readme-tree/15.svg" alt="src/test/java/ — Spring context test" align="top"></a><br>
<a href="https://github.com/LazarDavidovic98/rbs2024-2025/tree/main/csrf-exploit" title="csrf-exploit/"><img src=".github/readme-tree/16.svg" alt="csrf-exploit/ — attacker page for the CSRF demo" align="top"></a><br>
<a href="https://github.com/LazarDavidovic98/rbs2024-2025/blob/main/pom.xml" title="pom.xml"><img src=".github/readme-tree/17.svg" alt="pom.xml — Maven build" align="top"></a><br>
</p>

<sub>Every row is a link to that file or folder on GitHub. Purple: folders, green: source code, grey: configuration, data and docs.</sub>
<!-- project-tree:end -->

## Tech stack

Java 8 · Spring Boot 2.7 · Spring Security 5.7 · Spring JDBC · Thymeleaf · H2 · Bootstrap 4 · Maven · OWASP ZAP · SonarQube

The base application was provided by the course (Zühlke Engineering).
