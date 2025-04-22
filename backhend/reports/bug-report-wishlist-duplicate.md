#  Bug Report: Same product can be added multiple times to cart

##  Description
Users are able to add the same product to the cart multiple times. Each time, a new row is created in the `wishlist` table for the same user-product combination. This causes data inconsistency and makes it difficult to properly update the product quantity.

##  Steps to Reproduce
1. Log into the system.
2. Call the `/add_to_cart` endpoint with a product (e.g., product_id=5, quantity=1).
3. Call the same endpoint again with the same product.
4. When `/cart` is called, the same product appears multiple times in the response.

##  Expected Result
If the product already exists in the cart, the quantity should be increased instead of adding a duplicate entry. The `wishlist` table should only have **one row per user-product pair**.

##  Actual Result
Each request creates a new wishlist row for the same user and product. As a result:
- The product appears multiple times in the cart response,
- Quantity updates can conflict,
- Deletion and update operations may fail or behave unexpectedly.

##  Root Cause
The `wishlist` table did not have a unique constraint on the user-product pair. Therefore, it allowed inserting the same combination multiple times.

##  Fix
Added the following `@UniqueConstraint` to the Wishlist model:

```java
@Table(
    name = "wishlist",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "product_id"})
    }
)
```

Now, only one wishlist entry can exist for the same user-product combination. Repeated additions either increase the quantity or return an error, depending on implementation.

##  Impact
- Data consistency restored 
- `/cart` response fixed 
- Update & delete operations are now safer 

##  Status
**Fixed**



## Reported On
April 16, 2025

##  Reported By
Mertcan Bakır
