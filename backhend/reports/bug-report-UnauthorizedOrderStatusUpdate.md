# Bug Report: Unauthorized Order Status Update Bypass

## Description
Order delivery status updates are allowed without enforcing role-based restrictions. This allows **any user** to change the delivery status of an order, even if they are not a `ProductManager`, which can lead to serious data integrity and security issues.

## Steps to Reproduce
1. Create a new user with role `User` (non-manager).
2. Create and place a new order using this user.
3. Use any endpoint or repository method (e.g., `order.setStatus(...)` followed by `orderRepo.save(order)`) to update the delivery status to `SHIPPED`, `DELIVERED`, etc.
4. Observe that the status changes successfully without any validation or exception.

## Expected Result
Only users with the `ProductManager` role should be allowed to update the delivery status of orders. Unauthorized users attempting this action should receive a `403 Forbidden` error or similar.

## Actual Result
The system allows users with any role (including basic `User`) to change the delivery status without any checks.

## Root Cause
No access control or role-based authorization check is enforced in the service or controller layer when updating the order status.

## Suggested Fix
Implement a role-based authorization check (e.g., using Spring Security `@PreAuthorize` or manual checks) to ensure that only `ProductManager` users can perform status updates. Example:
```java
@PreAuthorize("hasRole('ProductManager')")
public void updateOrderStatus(Long orderId, OrderStatus status) {
}
```

## Impact
- Security risk due to unauthorized access.
- Incorrect order tracking by customers.
- Breakdown of trust in the delivery system.

## Status
**Fixed**

## Reported On
April 24, 2025

## Reported By
Defne Koçulu

