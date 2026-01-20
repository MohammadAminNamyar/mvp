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

## Requirements backlog (not yet implemented)
- Customer & wagering entities: persistent Customer, Wager, Transaction, Balance, and DataFeed models with storage and APIs.
- Payment processing & payouts: collect real payments and distribute winnings per quarter/final.
- Theme and branding configuration: add 4 selectable themes on the admin page with real-time switching.
- Return-to-pool option: per-board setting to return unclaimed squares to the pool on lock.
- Automatic purchase tool: buy N random squares or spread purchases across boards automatically.
- Visual celebration for winners: add a celebratory animation beyond the current border + badge.
- Data feed integration: pull live scores from an external provider instead of manual admin updates.
- Comprehensive database schema in the README: define how customers, wagers, transactions, balances, and feeds relate to boards/squares.

## Proposed data model (planned)
- **Customer**: `id`, `displayName`, `idmSubject`, `createdAt`.
- **Board**: `id`, `name`, `homeTeam`, `awayTeam`, `priceCents`, `housePercent`, `status`, `createdAt`.
- **Square**: `id`, `boardId`, `idx`, `status`, `ownerCustomerId`.
- **Wager**: `id`, `customerId`, `boardId`, `squareIds`, `priceCents`, `status`, `createdAt`.
- **Transaction**: `id`, `wagerId`, `customerId`, `type` (purchase/payout/refund), `amountCents`, `createdAt`.
- **Balance**: `customerId`, `availableCents`, `holdCents`, `updatedAt`.
- **DataFeed**: `id`, `boardId`, `provider`, `externalGameId`, `lastSyncedAt`.

Relationships (planned):
- Customer 1—* Wager; Customer 1—* Transaction; Customer 1—1 Balance.
- Board 1—* Square; Board 1—* Wager; Board 1—1 DataFeed.
- Wager 1—* Transaction; Wager *—* Square (via join table or stored square IDs).

## Tests
```bash
mvn test
```
