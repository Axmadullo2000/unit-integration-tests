# Booking Order Tickets - Design Specification

## Overview

A Spring Boot 3.5 REST API for booking event tickets. Users can register, authenticate, and manage orders containing multiple ticket items.

## Technology Stack

- Java 17
- Spring Boot 3.5
- Spring Security with JWT (access + refresh tokens)
- PostgreSQL
- JdbcTemplate (native SQL)
- Lombok
- Jakarta Bean Validation

## Entities

### User
| Field | Type | Constraints |
|-------|------|-------------|
| id | Long | PK, auto-generated |
| email | String | unique, not null |
| password | String | hashed, not null |
| firstName | String | not null, max 50 |
| lastName | String | not null, max 50 |
| createdAt | LocalDateTime | not null |
| updatedAt | LocalDateTime | not null |

### Order
| Field | Type | Constraints |
|-------|------|-------------|
| id | Long | PK, auto-generated |
| userId | Long | FK → User, not null |
| status | OrderStatus | PENDING, CONFIRMED, CANCELLED, COMPLETED |
| totalPrice | BigDecimal | not null, default 0 |
| createdAt | LocalDateTime | not null |
| updatedAt | LocalDateTime | not null |

### OrderItem
| Field | Type | Constraints |
|-------|------|-------------|
| id | Long | PK, auto-generated |
| orderId | Long | FK → Order, not null |
| eventName | String | not null |
| eventDate | LocalDateTime | not null |
| venue | String | not null |
| seatNumber | String | not null |
| price | BigDecimal | not null |
| createdAt | LocalDateTime | not null |

### RefreshToken
| Field | Type | Constraints |
|-------|------|-------------|
| id | Long | PK, auto-generated |
| userId | Long | FK → User, not null |
| token | String | unique, not null |
| expiresAt | LocalDateTime | not null |
| createdAt | LocalDateTime | not null |

## Relationships

- User 1 ←→ N Orders (one user has many orders)
- Order 1 ←→ N OrderItems (one order has many items)
- User 1 ←→ N RefreshTokens (one user can have multiple sessions)

## Package Structure

```
interview.prep.unittests/
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   └── OrderController.java
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   ├── CreateUserRequest.java
│   │   ├── UpdateUserRequest.java
│   │   ├── CreateOrderRequest.java
│   │   ├── UpdateOrderRequest.java
│   │   ├── CreateOrderItemRequest.java
│   │   └── UpdateOrderStatusRequest.java
│   └── response/
│       ├── AuthResponse.java
│       ├── UserResponse.java
│       ├── OrderResponse.java
│       ├── OrderItemResponse.java
│       └── ErrorResponse.java
├── entity/
│   ├── User.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── RefreshToken.java
│   └── OrderStatus.java (enum)
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   └── UnauthorizedException.java
├── mapper/
│   ├── UserMapper.java
│   ├── OrderMapper.java
│   └── OrderItemMapper.java
├── repository/
│   ├── UserRepository.java (interface)
│   ├── OrderRepository.java (interface)
│   ├── OrderItemRepository.java (interface)
│   ├── RefreshTokenRepository.java (interface)
│   └── impl/
│       ├── UserRepositoryImpl.java
│       ├── OrderRepositoryImpl.java
│       ├── OrderItemRepositoryImpl.java
│       └── RefreshTokenRepositoryImpl.java
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── UserDetailsServiceImpl.java
├── service/
│   ├── UserService.java (interface)
│   ├── OrderService.java (interface)
│   ├── AuthService.java (interface)
│   └── impl/
│       ├── UserServiceImpl.java
│       ├── OrderServiceImpl.java
│       └── AuthServiceImpl.java
└── UnitTestsApplication.java
```

## API Endpoints

### Auth Endpoints (public)

| Method | Path | Request | Response |
|--------|------|---------|----------|
| POST | /api/auth/register | RegisterRequest | AuthResponse |
| POST | /api/auth/login | LoginRequest | AuthResponse |
| POST | /api/auth/refresh | RefreshTokenRequest | AuthResponse |
| POST | /api/auth/logout | - (header token) | 200 OK |

### User Endpoints (authenticated)

| Method | Path | Request | Response |
|--------|------|---------|----------|
| GET | /api/users | - | List\<UserResponse\> |
| GET | /api/users/{id} | - | UserResponse |
| PUT | /api/users/{id} | UpdateUserRequest | UserResponse |
| DELETE | /api/users/{id} | - | 204 No Content |

### Order Endpoints (authenticated)

| Method | Path | Request | Response |
|--------|------|---------|----------|
| POST | /api/orders | CreateOrderRequest | OrderResponse |
| GET | /api/orders | - | List\<OrderResponse\> (user's orders) |
| GET | /api/orders/{id} | - | OrderResponse |
| PUT | /api/orders/{id} | UpdateOrderRequest | OrderResponse |
| PATCH | /api/orders/{id}/status | UpdateOrderStatusRequest | OrderResponse |
| DELETE | /api/orders/{id} | - | 204 No Content |

## Security Design

### Token Strategy
- Access token: 15 minutes lifetime, JWT format
- Refresh token: 7 days lifetime, stored in database

### JWT Claims
```json
{
  "sub": "user@example.com",
  "userId": 123,
  "iat": 1234567890,
  "exp": 1234568790
}
```

### Authentication Flow
1. User registers/logs in → receives access + refresh tokens
2. Client sends access token in `Authorization: Bearer <token>` header
3. `JwtAuthenticationFilter` validates token on each request
4. When access token expires, client calls `/api/auth/refresh` with refresh token
5. On logout, refresh token is deleted from database

### Password Handling
- BCrypt hashing via Spring Security's PasswordEncoder

## Error Handling

### Exception Classes
- `ResourceNotFoundException` → 404
- `DuplicateResourceException` → 409
- `UnauthorizedException` → 401

### Error Response Format
```json
{
  "timestamp": "2026-06-06T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "User with id 123 not found",
  "path": "/api/users/123"
}
```

### Validation Error Response (400)
```json
{
  "timestamp": "2026-06-06T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": {
    "email": "must not be blank",
    "firstName": "size must be between 1 and 50"
  },
  "path": "/api/users"
}
```

## Database Schema

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    event_name VARCHAR(255) NOT NULL,
    event_date TIMESTAMP NOT NULL,
    venue VARCHAR(255) NOT NULL,
    seat_number VARCHAR(20) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
```

## Mapper Design

All mappers are Spring `@Component` beans with instance methods for testability.

### UserMapper
```java
User toEntity(CreateUserRequest request);
User toEntity(RegisterRequest request, String encodedPassword);
UserResponse toResponse(User user);
void updateEntity(User user, UpdateUserRequest request);
```

### OrderMapper
```java
Order toEntity(CreateOrderRequest request, Long userId);
OrderResponse toResponse(Order order, List<OrderItem> items);
void updateEntity(Order order, UpdateOrderRequest request);
```

### OrderItemMapper
```java
OrderItem toEntity(CreateOrderItemRequest request, Long orderId);
OrderItemResponse toResponse(OrderItem item);
List<OrderItem> toEntityList(List<CreateOrderItemRequest> requests, Long orderId);
List<OrderItemResponse> toResponseList(List<OrderItem> items);
```

## Testability

The architecture ensures testability through:

1. **Interface-based design**: All services and repositories have interfaces, making mocking straightforward
2. **Constructor injection**: All dependencies injected via constructors
3. **Mapper components**: Mappers are beans that can be mocked or replaced
4. **Clean separation**: Controllers handle HTTP, services handle logic, repositories handle data access
5. **No static methods**: All methods are instance methods for easy mocking

## Dependencies to Add

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
```
