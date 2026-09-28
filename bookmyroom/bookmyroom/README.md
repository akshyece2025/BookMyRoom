# BookMyRoom – Guest House Room Booking System

Spring Boot backend that lets guest-house staff check room availability by date range and confirm bookings **without double-booking**. Includes a simple web dashboard, a global error handler, tests, and a Postman collection.

## Tech stack
Spring Boot 4.1.1 · Spring Web MVC · Spring Data JPA (Hibernate) · Bean Validation · H2 (default) / MySQL · Java 21 or newer

## How to run
You only need a JDK (21+). Maven is downloaded automatically by the wrapper.

**Windows (Command Prompt, inside the project folder):**
```
mvnw.cmd spring-boot:run
```
**Mac / Linux:**
```
./mvnw spring-boot:run
```
Or in Eclipse/STS/IntelliJ: *Import → Existing Maven Project*, then run `BookMyRoomApplication.java`.

Then open:
- **http://localhost:8080** – web dashboard (rooms, availability, booking, cancel, monthly report)
- REST API at `http://localhost:8080/api/...` (see table below)

The app starts with 6 sample rooms and 2 sample bookings (turn off with `app.seed-data=false`).
Data is in memory (H2) and resets on every restart.

### Using MySQL instead
1. In MySQL: `CREATE DATABASE bookmyroom;`
2. Put your MySQL username/password in `src/main/resources/application-mysql.properties`
3. Run: `mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql`

### Run the tests
```
mvnw.cmd test
```

## Project structure
```
com.app.guesthouse.bookmyroom
├── entity       Room, Guest, Booking, RoomType, BookingStatus
├── repository   RoomRepository, GuestRepository, BookingRepository (JpaRepository)
├── dto          request/response records with @NotNull, @Positive, @Email ... validation
├── service      RoomService, BookingService, GuestService, ReportService  ← business rules
├── controller   RoomController, BookingController, GuestController, ReportController
├── exception    GlobalExceptionHandler (@RestControllerAdvice) + custom exceptions
└── config       DataLoader (sample data)
```

## Database design
| Entity | Key fields | Relationships |
|---|---|---|
| **Room** | id, roomNumber (unique), type, capacity, ratePerNight | One Room → many Bookings (`@OneToMany`) |
| **Guest** | id, fullName, email (unique), phone | One Guest → many Bookings (`@OneToMany`) |
| **Booking** | id, checkInDate, checkOutDate, numberOfGuests, nights, ratePerNight, totalCost, status, createdAt, cancelledAt | Many Bookings → one Room, many → one Guest (`@ManyToOne`) |

## API endpoints
| # | Feature | Method & URL | Success | Errors |
|---|---|---|---|---|
| 1 | Add room | `POST /api/rooms` | 201 | 400 invalid, 409 duplicate number |
| | List / filter rooms | `GET /api/rooms?type=DOUBLE&minCapacity=2&maxRate=3000` | 200 | |
| | Get / update / delete room | `GET` · `PUT` · `DELETE /api/rooms/{id}` | 200 / 200 / 204 | 404, 409 (room has bookings) |
| 2 | Check availability | `GET /api/rooms/available?checkIn=2026-12-10&checkOut=2026-12-13&guests=2` | 200 | 400 bad dates |
| 3 | Create booking | `POST /api/bookings` | 201 | 400, 404 room, **409 overlap** |
| | List / search bookings | `GET /api/bookings?status=CONFIRMED&roomId=3&guestEmail=ravi&from=...&to=...` | 200 | |
| | Get booking | `GET /api/bookings/{id}` | 200 | 404 |
| 4 | Cancel booking (frees room) | `DELETE /api/bookings/{id}` (or `PUT /api/bookings/{id}/cancel`) | 200 | 404, 409 already cancelled |
| 5 | Occupancy report | `GET /api/reports/occupancy?month=2026-12` | 200 | 400 bad month |
| + | Dashboard counts | `GET /api/dashboard` | 200 | |
| + | Guests | `POST /api/guests`, `GET /api/guests?search=`, `GET /api/guests/{id}/bookings` | 201 / 200 | 409 duplicate e-mail |

Sample booking body:
```json
{
  "roomId": 3,
  "guestName": "Anitha R",
  "guestEmail": "anitha@example.com",
  "guestPhone": "9876501234",
  "checkInDate": "2026-12-10",
  "checkOutDate": "2026-12-13",
  "numberOfGuests": 2
}
```
→ 3 nights × Rs 2000 = `"totalCost": 6000.00`

## Business rules (enforced in the service layer, before saving)
1. **No overlapping bookings for the same room.** Two ranges overlap when `existing.checkIn < new.checkOut AND existing.checkOut > new.checkIn`. Check-out day is exclusive, so a new guest may check in on the day the previous guest checks out. The room row is locked (`PESSIMISTIC_WRITE`) while booking, so two staff members can't book the same room at the same moment.
2. **Total cost = nights stayed × room rate.** The rate is stored on the booking, so later price changes don't change old bookings.
3. Check-out must be after check-in; check-in cannot be in the past; max 60 nights.
4. Number of guests cannot exceed room capacity.
5. Cancelled bookings no longer block the room, so cancelling frees the dates immediately.

Every error returns clean JSON, never a stack trace:
```json
{
  "timestamp": "2026-09-28T10:15:30",
  "status": 409,
  "error": "Conflict",
  "message": "Room 201 is not available from 2026-12-12 to 2026-12-15. It overlaps with booking #3 (2026-12-10 to 2026-12-13).",
  "path": "/api/bookings",
  "fieldErrors": null
}
```

## Testing with Postman
Import `BookMyRoom.postman_collection.json` (Postman → Import). Start the app fresh, then run the folders in order (1 → 5). Each request has a test that checks the expected status code, covering both normal cases and rule violations (overlap 409, bad dates 400, missing room 404, etc.).
