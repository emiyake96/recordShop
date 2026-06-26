
# EasyShop E-Commerce API

A Spring Boot REST API backend for an online e-commerce store. Built as a capstone project, this API supports user authentication, product browsing, shopping cart management, user profiles, and order checkout.

---

## Technologies Used

- Java 17
- Spring Boot 3.4.1
- Spring Security (JWT authentication)
- Spring Data JPA
- MySQL
- Maven

---

## Setup Instructions

### 1. Database
- Open MySQL Workbench
- Run the script at `database/create_database_easyshop.sql`
- This creates the database and seeds it with products and sample users

### 2. Application Properties
- Copy `src/main/resources/application.properties.example` to `application.properties`
- Fill in your MySQL username and password

### 3. Run the App
- Open the project in IntelliJ
- Run `ECommerceApplication.java`
- API will be available at `http://localhost:8080`

### Sample Users
| Username | Password | Role  |
|----------|----------|-------|
| user     | password | USER  |
| admin    | password | ADMIN |
| george   | password | USER  |

---

## API Endpoints

### Authentication
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/register` | Register a new user |
| POST | `/login` | Login and receive JWT token |

### Categories
| Method | URL | Auth |
|--------|-----|------|
| GET | `/categories` | Public |
| GET | `/categories/{id}` | Public |
| GET | `/categories/{id}/products` | Public |
| POST | `/categories` | Admin only |
| PUT | `/categories/{id}` | Admin only |
| DELETE | `/categories/{id}` | Admin only |

### Products
| Method | URL | Auth |
|--------|-----|------|
| GET | `/products` | Public |
| GET | `/products/{id}` | Public |
| POST | `/products` | Admin only |
| PUT | `/products/{id}` | Admin only |
| DELETE | `/products/{id}` | Admin only |

Product search supports query parameters: `cat`, `minPrice`, `maxPrice`, `subCategory`

### Shopping Cart
| Method | URL | Auth |
|--------|-----|------|
| GET | `/cart` | Logged in |
| POST | `/cart/products/{productId}` | Logged in |
| PUT | `/cart/products/{productId}` | Logged in |
| DELETE | `/cart` | Logged in |

### Profile
| Method | URL | Auth |
|--------|-----|------|
| GET | `/profile` | Logged in |
| PUT | `/profile` | Logged in |

### Orders
| Method | URL | Auth |
|--------|-----|------|
| POST | `/orders` | Logged in |

---

## Application Screenshots

### Home / Product Listing
<img width="1463" height="720" alt="Screenshot 2026-06-26 at 10 17 46 AM" src="https://github.com/user-attachments/assets/056bbcf1-ec4a-405c-b116-0a9713d9a518" /><img width="1462" height="704" alt="Screenshot 2026-06-26 at 10 19 13 AM" src="https://github.com/user-attachments/assets/3d14bee6-043f-4072-9d79-261302a2b5f2" />


### Shopping Cart
<img width="1462" height="704" alt="Screenshot 2026-06-26 at 10 19 13 AM" src="https://github.com/user-attachments/assets/3d14bee6-043f-4072-9d79-261302a2b5f2" />


---

## Interesting Code: Checkout Transaction

The most interesting piece of code in this project is the `OrderService.checkout()` method. The entire checkout process is wrapped in a single `@Transactional` block, meaning if any step fails — saving the order, saving a line item, or clearing the cart — the whole thing rolls back automatically. Nothing is left half-written in the database.

```java
@Transactional
public Order checkout(int userId)
{
    ShoppingCart cart = shoppingCartService.getByUserId(userId);
    Profile profile = profileService.getByUserId(userId);

    Order order = new Order();
    order.setUserId(userId);
    order.setDate(LocalDateTime.now());
    order.setAddress(profile.getAddress());
    order.setCity(profile.getCity());
    order.setState(profile.getState());
    order.setZip(profile.getZip());

    Order savedOrder = orderRepository.save(order);

    for (ShoppingCartItem item : cart.getItems().values())
    {
        OrderLineItem lineItem = new OrderLineItem();
        lineItem.setOrderId(savedOrder.getOrderId());
        lineItem.setProductId(item.getProductId());
        lineItem.setSalesPrice(item.getProduct().getPrice());
        lineItem.setQuantity(item.getQuantity());
        lineItem.setDiscount(item.getDiscountPercent());
        orderLineItemRepository.save(lineItem);
    }

    shoppingCartService.clearCart(userId);

    return savedOrder;
}
```

This ensures the order is always in a consistent state — you'll never have an order with missing line items, or a cart that was cleared without an order being created.
