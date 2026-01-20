# Squares Pool MVP

A minimal 10x10 football squares pool MVP built with Spring Boot 3, Java 17, H2, STOMP over SockJS, and Thymeleaf.

## Prerequisites
- Java 17+
- Maven 3.9+

## Run locally
```bash
mvn spring-boot:run
```

Then open:
- http://localhost:8080/ (board list)
- http://localhost:8080/admin (admin controls)

## Demo steps
1. Open the admin page at `/admin`.
2. Enter the admin token (default: `admin`).
3. Create a board (teams, price, house %, minimum squares to activate).
4. Open the board from the homepage (uses `/boards/{id}/view`).
5. On the board page, select squares and confirm purchase.
6. Open the same board in another browser tab to see realtime reservations/purchases.
7. Start the game from the admin page (requires purchased squares >= minimum).
8. Update the score and confirm quarters to see winner highlights.
9. Use reset to clear the board for another demo.

## Notes
- Reservations expire automatically every 30 seconds (configurable in `application.yml`).
- Digits are hidden until the board is full or the game starts.
- When the game starts, any empty or reserved squares convert to HOUSE.
- WebSocket topics: `/topic/boards/{id}/snapshot` and `/topic/boards/{id}/presence`.

## Tests
```bash
mvn test
```
