

# 🍽️ Online Food Ordering System – API Documentation

This is a Spring Boot-based RESTful API for an online food ordering platform with **Admin**, **Staff**, and **Customer** roles, secured with JWT authentication.

**please use a markdown viewer for best experience**

## 🛠️ Setup Instructions

### 📦 Prerequisites
- Java 21
- MySQL 8+
- Maven
- MySql
- Postman (for testing)

### 🗄️ Database Setup
1. Create MySQL database:
   ```sql
   CREATE DATABASE food_ordering;
   ```
2. Configure `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/food_ordering
   spring.datasource.username=root
   spring.datasource.password=1234
   spring.jpa.hibernate.ddl-auto=update
   
   # JWT Configuration (For testing only - secure properly in production)
   app.jwtSecret=b4d7a9c2f1e8g3h6j5k0l2m9n7p8q4r1s3t6v5w0x9y2z8a7c4d1e5f0g3h2
   app.jwtExpirationInMs=86400000
   ```

### 🔄 Initial Data
The system auto-creates these default users on first launch:
- **Role**: `           mail                   `     / `Password`
- **Admin**: `   admin@foodordering.com       `      / `admin123`
- **Staff**:   `     restaurant@foodordering.com` / `restaurant123`
- **Customer**: `customer@foodordering.com   `/ `customer123`

Roles are initialized via `DataInitializer.java`:
```java
@Configuration
public class DataInitializer {
    // Creates ROLE_ADMIN, ROLE_RESTAURANT_STAFF, ROLE_CUSTOMER
    // And initial users with these roles
}
```

---

## 🔐 Authentication

### 🔑 Login
```http
POST /api/auth/login
```
**Request:**
```json
{
  "usernameOrEmail": "restaurant@foodordering.com",
  "password": "restaurant123"
}
```
# **Response:** JWT token (use in `Authorization: Bearer <token>` header)

### 📝 Register
```http
POST /api/auth/register
```
**Request:**
```json
{
  "name": "John Doe",
  "username": "johndoe1",
  "email": "john1@example.com",
  "password": "john@123",
  "phone": "1234567890",
  "address": "123 Main St"
}
```

---

## 📋 Menu APIs

| Endpoint | Method | Role | Description |
|----------|--------|------|-------------|
| `/api/menu` | GET | Public | Get all menu items |
| `/api/menu/{id}` | GET | Public | Get specific item |
| `/api/menu` | POST | Admin/Staff | Add new item |
| `/api/menu/{id}` | PUT | Admin/Staff | Update item |
| `/api/menu/{id}` | DELETE | Admin/Staff | Delete item |

**Categories:** `MAIN_COURSE`, `STARTERS`, `BEVERAGES`, `PIZZA` (defined in `AppConstants.java`)

**Sample Request (Add Item):**
```json
{
  "name": "Soda",
  "description": "Premium soda",
  "price": 5.00,
  "category": "BEVERAGES",
  "available": true,
  "imageUrl": "/images/beer.jpg",
  "createdBy": "admin"
}
```

---

## 🛒 Order APIs

### Place Order
```http
POST /api/orders
```
**Required Permissions:**  
🔒 Requires **customer** role (JWT token in Authorization header)

**Request:**
```json
{
  "deliveryAddress": "123 Main St",
  "items": [
    {"menuItemId": 1, "quantity": 2},
    {"menuItemId": 2, "quantity": 5}
  ],
  "paymentMethod": "CREDIT_CARD",
  "restaurantNotes": "Extra napkins"
}
```

### Order Management
| Endpoint | Method | Role | Description |
|----------|--------|------|-------------|
| `/api/orders` | GET | Customer | Order history |
| `/api/orders/all` | GET | Admin/Staff | All orders |
| `/api/orders/{id}` | GET | Customer | Order details |
| `/api/orders/{id}/status` | PUT | Admin/Staff | Update status |
| `/api/orders/{id}` | DELETE | Customer | Cancel order |

**Status Values:** `PENDING`, `PREPARING`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`

---

## 💰 Payment APIs
```http
POST /api/payments
```
**Required Permissions:**  
🔒 Requires **customer** role (JWT token in Authorization header)
**Request:**
```json
{
  "orderId": 5,
  "amount": 50,
  "paymentMethod": "CREDIT_CARD",
  "cardNumber": "4111111111111111",
  "expiryDate": "12/25",
  "cvv": "123"
}
```
```http
GET /api/payments/order/{orderId}  
```

---
### 🔄 Update Order Status

**Endpoint:**
```http
PUT /api/orders/{orderId}/status
```

**Required Permissions:**  
🔒 Requires **Admin** or **Staff** role (JWT token in Authorization header)

**Request Body:**
```json
{
  "status": "OUT_FOR_DELIVERY"
}
```

**Available Status Values:**
```markdown
| Status              | Description                          |
|---------------------|--------------------------------------|
| `PENDING`           | Order received, not yet processed    |
| `PREPARING`         | Restaurant is preparing the order    |
| `OUT_FOR_DELIVERY`  | Order is on its way to customer      |
| `DELIVERED`         | Order successfully delivered         |
| `CANCELLED`         | Order cancelled (by customer/staff)  |
```





## 📝 Feedback
```http
POST /api/orders/feedback/{orderId}
```
**Request:**
```json
{
  "rating": 5,
  "comments": "Great service!"
}
```
```http
GET /api/orders/feedback/{orderId}  # Accessible by User/Admin/Staff
```

---

## 👤 User Profile
```http
GET /api/users/me  # Get current user
PUT /api/users/me  # Update profile
```
**Update Request:**
```json
{
  "username": "updated_user",
  "name": "Updated Name",
  "email": "updated@email.com",
  "address": "New Address",
  "phone": "9876543210"
}
```

---

## 🔒 Role-Based Access

| Role | Accessible Endpoints |
|------|----------------------|
| Admin | All endpoints |
| Staff | Menu/Order management |
| Customer | Orders, Payments, Profile |

---

## 📌 Important Notes
1. **JWT Security**: Tokens are obtained via `/api/auth/login`
2. **Database**: Configure credentials in `application.properties`
3. **Payment**: Currently mocked - integrate real gateway for production
4. **Logs**: Stored in `/logs/app.log`

---

**Happy Coding!** 🚀
```

Key improvements:
1. Removed sample tokens (now instructs to use login endpoint)
2. Added database setup instructions
3. Included DataInitializer explanation
4. Organized all endpoints clearly
5. Added security notes about JWT
6. Maintained consistent Markdown formatting
7. Included all APIs from your text file

This documentation is ready to be placed in your `README.md` file.
