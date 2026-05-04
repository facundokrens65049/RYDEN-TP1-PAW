***REMOVED*** Repository guidance

**`CLAUDE.md`** and **`agents.md`** are identical: guidance for Claude Code, Cursor, and other coding agents.

***REMOVED******REMOVED*** Commands

```bash
***REMOVED*** Build all modules
mvn clean install

***REMOVED*** Run the web application
mvn jetty:run -pl webapp

***REMOVED*** Run all tests
mvn test

***REMOVED*** Run tests for a specific module
mvn test -pl persistence

***REMOVED*** Run a single test class
mvn test -pl persistence -Dtest=ListingJdbcDaoTest
```

The app is served at `http://localhost:8080/` with context path **`/webapp`** (see `application/application.properties`), so routes look like `http://localhost:8080/webapp/home`.

***REMOVED******REMOVED*** Building and running

***REMOVED******REMOVED******REMOVED*** Prerequisites

- Java 21
- Maven
- PostgreSQL reachable with credentials in **`webapp/src/main/resources/application/application.properties`** (`spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`). Optional overrides: `application-${spring.profiles.active}.properties` (see `@PropertySources` in `WebConfig`). Local development: use `application-local.properties` for a local PostgreSQL instance.

***REMOVED******REMOVED******REMOVED*** Key commands

```text
mvn clean install
mvn jetty:run -pl webapp
mvn test
```

Base URL depends on `server.port` and `server.servlet.context-path` in `application.properties`.

***REMOVED******REMOVED*** Architecture

This is a **car rental platform**: a Java web app on **Spring Framework 5.3**, multi-module **Maven**, strict layer separation.

***REMOVED******REMOVED******REMOVED*** Module dependency chain

```
webapp → services → persistence → models
              ↑            ↑
   service-contracts  persistence-contracts
```

***REMOVED******REMOVED******REMOVED*** Module responsibilities

- **models** — Domain POJOs (`Car`, `User`, `Listing`, `Reservation`, `Image`, `CarPicture`), DTOs such as `ListingCard` / `ListingDetail`, immutable search criteria (`ListingSearchCriteria`, `ReservationSearchCriteria`, `OwnerListingSearchCriteria`), shared utilities (wall-clock parsing, `AvailabilityPeriod` with `America/Argentina/Buenos_Aires`).
- **persistence-contracts** — DAO interfaces.
- **persistence** — JDBC with `JdbcTemplate` / `NamedParameterJdbcTemplate`, inline SQL. DAO tests run against HSQLDB.
- **service-contracts** — Service interfaces, shared exceptions, `MessageKeys` for i18n codes.
- **services** — Business logic, email, async mail tasks.
- **webapp** — Spring MVC controllers, JSPs, `application/application.properties`, static assets, Thymeleaf mail under `classpath:mail/`.

***REMOVED******REMOVED******REMOVED*** Key conventions

- **Dependency injection**: Constructor injection with `@Autowired`.
- **Persistence**: Plain SQL via `JdbcTemplate` / named parameters where used. No ORM. `RowMapper` as inline or private static fields.
- **Configuration**: Java `@Configuration` (`WebConfig`, `SpringMailConfig`, `WebAuthConfig`, `ValidationWebConfig`) plus `web.xml` for servlet bootstrap. Properties from `application.properties`; profile overrides from `application-{profile}.properties` if present.
- **Dependency versions**: Root `pom.xml` `<dependencyManagement>`; child POMs omit versions where managed.
- **Views**: JSPs in `webapp/src/main/webapp/WEB-INF/views/`, tags in `WEB-INF/tags/`, assets under `css/`, `js/`, `assets/`.
- **Component scan** (`WebConfig`): `ar.edu.itba.paw.webapp.controller`, `.advice`, `.exception`, `.util`, `.support`, `.security`, `.validation`, `.interceptor`, plus `ar.edu.itba.paw.services` and `ar.edu.itba.paw.persistence`. `webapp.form` and `webapp.dto` are not scanned; `CurrentUserArgumentResolver` in `webapp.support` is registered on the MVC adapter. Servlet listeners (e.g. `webapp.listener`) are declared in `WEB-INF/web.xml`.

***REMOVED******REMOVED******REMOVED*** Domain overview

A `User` owns `Car`s. A `Car` can have a `Listing` with price and availability (`listing_availability`). Other users create `Reservation`s. `Image`s are stored as byte arrays and linked via `CarPicture`.

Key enums (on model classes): `Car.Type`, `Car.Powertrain`, `Car.Transmission`, `Listing.Status` (active/paused/finished), `Reservation.Status` (e.g. pending, accepted, started, cancelled, finished — see code for full set).

***REMOVED******REMOVED*** Technologies

- **Backend**: Java 21, Spring 5.3 (MVC, JDBC, Context, TX, Context Support for mail), Spring Security 5.7.14.
- **Database**: PostgreSQL (runtime), HSQLDB (DAO / persistence tests). Schema: `schema.sql` + **Flyway** (`V2__`, `V3__`, … under `classpath:db/migration/`).
- **Web UI**: JSP, Spring form tags, custom JSP tags under `WEB-INF/tags/`.
- **Client scripts**: Shared JS in `webapp/src/main/webapp/js/` (e.g. **Flatpickr** for date/range pickers, CDN in `header.jsp` / `footer.jsp`).
- **Email**: JavaMail, **Thymeleaf** HTML (`webapp/src/main/resources/mail/html/`), separate `ResourceBundleMessageSource` for mail copy (`mail/MailMessages` + locale variants).
- **Build**: Maven. **Runtime**: Jetty (`jetty-maven-plugin` on the `webapp` module).

***REMOVED******REMOVED*** Database

Credentials live in `webapp/src/main/resources/application/application.properties`. The schema is applied on startup from `persistence/src/main/resources/schema.sql` via `DataSourceInitializer` in `WebConfig`. Tables use `CREATE TABLE IF NOT EXISTS`, so bootstrap is safe to re-run.

Tests use **in-memory HSQLDB** per module `TestConfiguration` / `TestPersistenceConfig` — no PostgreSQL required for tests.

***REMOVED******REMOVED******REMOVED*** Flyway migrations

Bootstrap runs first (`schema.sql`), then Flyway from `classpath:db/migration/` (`webapp/src/main/resources/db/migration/`). In `WebConfig`: `baselineOnMigrate(true)`, `baselineVersion("1")`, `failOnMissingLocations(true)`.

New migrations: `V<number>__<description>.sql` (e.g. `V4__add_reviews.sql`). Examples: `V2__users_extend_profile_and_auth.sql`, `V3__email_verification_codes.sql`.

***REMOVED******REMOVED*** Internationalization (i18n)

- **UI/errors**: `ReloadableResourceBundleMessageSource` with `classpath:messages` and `classpath:exception-messages`. Default **English**; Spanish: `messages_es.properties`, `exception-messages_es.properties`.
- **Locale**: `AcceptHeaderLocaleResolver` in `WebConfig` — `Accept-Language`; supported **English** and **Spanish (`es`)**; defaults to English.
- **`LocaleMessages`** (`webapp.util`): resolves keys via `MessageSource` + `LocaleContextHolder`.
- **Mail**: `mail/MailMessages.properties` and `mail/MailMessages_es.properties`. `@Async` mail must not rely on `LocaleContextHolder` — capture locale on the request thread (e.g. `ReservationConfirmationPayload***REMOVED***getMessageLocale`).
- **Exception keys**: `exception-messages.properties`, aligned with `ar.edu.itba.paw.exception.MessageKeys`.

***REMOVED******REMOVED*** Email

- Configured in `SpringMailConfig` (`mail/emailconfig.properties`, `mail/javamail.properties`).
- HTML templates use keys such as `mail.reservationConfirmation.*`.
- Async sending uses `mailTaskExecutor` from `WebConfig`.

***REMOVED******REMOVED*** Security

Configured in **`WebAuthConfig`**: Spring Security 5.7.14, `@EnableWebSecurity`, `SecurityFilterChain`, `RydenAuthenticationProvider`, `RydenUserDetailsService`. Remember-me, session auth, CSRF. **Do not** add or replace auth configuration outside `WebAuthConfig`.

***REMOVED******REMOVED*** Dates and business rules (high level)

- Listing availability and reservation datetimes use wall zone `AvailabilityPeriod.WALL_ZONE` (Argentina) when parsing server-side.
- **Publishing**: valid date order; period start not before **today** in that zone (`ListingServiceImpl`).
- **Reserving**: pickup day not before **today**; interval must fit published availability (`ReservationServiceImpl`).
- **Flatpickr**: `minDate: 'today'` in `components.js` complements server validation.

***REMOVED******REMOVED*** Development conventions

***REMOVED******REMOVED******REMOVED*** Coding style

- **DI**: Constructor `@Autowired` as in existing code.
- **Service ↔ persistence**: Each `*ServiceImpl` injects its own DAOs. Cross-aggregate access goes through peer services (`UserService`, `ReservationService`, …). Use `@Lazy` on one constructor param when two services need each other to break cycles.
- **Scheduling** (`services/.../scheduling`): `@Scheduled` beans call services only, not DAOs.
- **Validation**: `ValidationWebConfig` registers `LocalValidatorFactoryBean` as the MVC validator.
- **Javadoc**: Public contracts in `*-contracts` in **English**. Avoid HTML `<p>` in Javadoc; use extra `*` lines or `{@code}` / `{@link}`. Controllers: short **English** class summary where it helps.

***REMOVED******REMOVED******REMOVED*** Quality and security (recurring audit)

- **Spring versions**: Root `pom.xml` (`spring.version`, `spring-security.version`).
- **Controllers**: Call **services** only, not DAOs. No embedded business rules or direct mail sends; use service APIs. Prefer constructor injection and minimal visibility.
- **Exceptions & UX**: Domain failures → `RydenException` + message keys; `UnhandledExceptionHandler` must not expose raw `Throwable***REMOVED***getMessage()` (i18n keys; escape dynamic text with JSTL `c:out` when shown).
- **SQL**: Parameterized SQL (`?` or named params); never concatenate user input into SQL.
- **Logging**: Production `logback/logback-prod.xml` (typically **INFO+** for `ar.edu.itba.paw`); local may use `logback-local.xml` with **DEBUG**. Use **SLF4J** with parameterized messages; prefer **DEBUG** (optionally SLF4J 2 **fluent** `atDebug().setMessage(...).addArgument(...).setCause(...).log()`) for swallowed parse/IO fallbacks where you still need traceability.
- **Tests**: Arrange / exercise / assert outcomes, not wiring. **No `Mockito.verify`** (or call-count tricks). Skip tests that only mirror a one-line delegate.
- **Style**: `final` where appropriate, private constructors on utility classes, immutable DTOs/criteria where practical; avoid magic numbers — read limits from `application.properties` (documented JVM fallbacks only where needed, e.g. `PaginationFallbackSizes`).
- **Comments**: English only; remove obsolete chatty notes.

***REMOVED******REMOVED******REMOVED*** Dependency management

- Versions in root `pom.xml` `<dependencyManagement>`; module POMs omit repeated versions.
- Internal modules: `${project.version}` for siblings.

***REMOVED******REMOVED******REMOVED*** Testing

- **Test method names**: **`testFunctionName`** — camelCase, must **start with `test`**, then descriptive tail (e.g. `testRegistersUser()`). No `should…`, BDD prose, snake_case, or names without the **`test`** prefix.
- **Unit tests**: JUnit 5 + Mockito (`services`, `models`).
- **Persistence**: HSQLDB, DAO-style tests under `persistence/src/test`.
- **DAO integration tests** (`*JdbcDaoTest`, `DaoIntegrationTestSupport`): After a **write** (`create*`, `update*`, `delete*`), assert with **`JdbcTemplate`** or SQL in arrange — **never** verify only via another method on the **same** DAO (e.g. `createCar` + `getCarById`). For ordered reads with fixtures, optional ground-truth `ORDER BY` via `JdbcTemplate`.
- **No interaction verification**: No `verify`, `verifyNoInteractions`, `never()`, `times()`, `InOrder`, captors for call wiring.
- **Do not emulate verify**: No counters or collector lists whose sole purpose is call counts.
- **Matchers**: Do not use `Mockito.anyLong()` (or `any*()`) as the **value** inside `when(...)` — use fixed literals.
- **Strict Mockito**: Remove unused stubbings (`UnnecessaryStubbingException`).

***REMOVED******REMOVED******REMOVED*** Message keys

Domain / validation exception copy: **`exception-messages.properties`** (+ `_es`), consistent with **`MessageKeys`**.

***REMOVED******REMOVED******REMOVED*** Application properties

- **Main**: `webapp/src/main/resources/application/application.properties` — port, context path (`/webapp`), uploads, validation, pagination, reservation timing, `app.scheduler.*` crons/zones.
- **Profiles**: `application-local.properties`, `application-deployed.properties` (examples in folder); secrets not committed.
- **Mail**: `mail/emailconfig.properties`, `mail/javamail.properties` under `webapp/src/main/resources/mail/`.

***REMOVED******REMOVED******REMOVED*** Directory structure (per module)

- `src/main/java`, `src/main/resources`, `src/test/java` as usual.
- `webapp/src/main/webapp`: JSPs, CSS, JS, `WEB-INF/web.xml`.

***REMOVED******REMOVED******REMOVED*** Key services and DAOs (orientation)

- **User**: `UserService` / `UserDao`.
- **Car & listing**: `CarService` / `CarDao`, `ListingService` / `ListingDao`.
- **Reservation**: `ReservationService` / `ReservationDao`.
- **Email & verification**: `EmailService`, `EmailVerificationService`, `PasswordResetService` and related DAOs.
- **Images**: `ImageService` / `ImageDao`, `CarPictureService` / `CarPictureDao`.
- **Session**: e.g. `PublishCarStashSessionListener` for publish-form stash cleanup.

***REMOVED******REMOVED*** Logging

Use SLF4J (not Log4j or `java.util.logging`):

```java
private static final Logger LOGGER = LoggerFactory.getLogger(ClassName.class);
```

Use **parameterized** logging:

```java
LOGGER.debug("Creating user with email {}", email); // correct
LOGGER.debug("Creating user with email " + email);  // wrong
```

***REMOVED******REMOVED*** Transactions

All service methods need `@Transactional` (`org.springframework.transaction.annotation.Transactional`). Read-only methods: `@Transactional(readOnly = true)` (pool hint for primary/replica).

**Proxy limitation**: Internal `this.someMethod()` calls skip the proxy and ignore `@Transactional`. Only external calls through the proxy apply. Annotate public service methods individually; do not rely on class-level + internal delegation.

***REMOVED******REMOVED******REMOVED*** PR checks (transactionality)

- Every **public** method in `services/.../*ServiceImpl.java` must have explicit `@Transactional`.
- Pure reads / normalization: `readOnly = true`.
- Writes, side effects, mail orchestration: `@Transactional` without `readOnly = true`.
- Private helpers are not transactional entry points.
- Any public method **without** `@Transactional` needs an explicit PR justification.

Quick checks:

```text
rg "public .*\\(" services/src/main/java/ar/edu/itba/paw/services | rg "ServiceImpl"
rg "@Transactional\\(readOnly\\s*=\\s*true\\)|@Transactional" services/src/main/java/ar/edu/itba/paw/services
rg "this\\.[a-zA-Z0-9_]+\\(" services/src/main/java/ar/edu/itba/paw/services
```

***REMOVED******REMOVED*** Controller cross-cutting concerns

***REMOVED******REMOVED******REMOVED*** Shared model attributes

Use `@ControllerAdvice` to expose `@ModelAttribute` beans across controllers.

***REMOVED******REMOVED*** Spring AOP (enabled in `WebConfig`)

- **`@Async`**: fire-and-forget (e.g. mail). Pass locale/user as arguments; **do not** use `LocaleContextHolder` or `SecurityContextHolder` inside `@Async` (different thread).
- **`@Scheduled`**: recurring jobs.
- **`@Cacheable`**: cache expensive work.

Same proxy rule as `@Transactional`: only applies to **public** methods invoked from outside the bean.
