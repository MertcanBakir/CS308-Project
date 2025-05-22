# Bug Report

## Title  
**Removing an item from the cart also removes it from the wishlist**

## Environment  
- **Operating System:** Windows 10  
- **Browser:** Chrome 123.0.0  
- **URL:** `localhost:8080`, `localhost:3000`

## Preconditions  
- The system uses a shared `wishlist` table to represent both cart and wishlist items.  
- A product must be added to the cart (which is backed by the `wishlist` table).  
- Same product must also exist in the user's wishlist.

## Steps to Reproduce  
1. Add a product to the cart.  
2. Add the same product to the wishlist (or ensure it already exists there).  
3. Remove the product from the cart.

## Expected Result  
- Only the cart entry should be removed.  
- The wishlist entry should remain visible.

## Actual Result  
- Removing the item from the cart also deletes the wishlist entry.  
- The product disappears from the wishlist even though the user didn’t remove it manually.

## Severity  
- **High**

## Root Cause  
- Both cart and wishlist features shared the same `wishlist` table.  
- The `/delete_from_cart` endpoint directly deleted the item from the `wishlist` table using `wishListRepo.delete(...)`.  
- Since there was no distinction between a "wishlist item" and a "cart item", the system couldn't differentiate between the two.

## Fix  
- Created a new table called `real_wishlist` for true wishlist functionality.  
- Separated the logic for cart and wishlist:
  - The existing `wishlist` table is now exclusively used for cart items.
  - The new `real_wishlist` table manages user wishlists.

### Fixed Code Snippet

#### Model: `RealWishlist.java`
```java
@Entity
@Table(name = "real_wishlist", uniqueConstraints = {
  @UniqueConstraint(columnNames = {"user_id", "product_id"})
})
public class RealWishlist {
  @ManyToOne
  private User user;

  @ManyToOne
  private Product product;
}
```

### Frontend Code Changes

#### ✅ `ProductDetail.js` → `Add to Wishlist` işlemi güncellendi
```js
const handleAddToWishlist = async () => {
  const token = localStorage.getItem("token");
  try {
    const response = await fetch(`http://localhost:8080/real-wishlist/add/${id}`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (!response.ok) throw new Error("Failed to add to real wishlist");

    alert("Ürün wishlist'e eklendi!");
  } catch (err) {
    console.error("RealWishlist error:", err);
    alert("Wishlist'e eklenirken bir hata oluştu.");
  }
};
```

#### ✅ `Wishlist.js` → Listeleme ve silme artık `real-wishlist` üzerinden yapılıyor
```js
useEffect(() => {
  const fetchWishlist = async () => {
    const token = localStorage.getItem("token");
    const res = await fetch("http://localhost:8080/real-wishlist", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await res.json();
    setWishlist(data);
  };
  fetchWishlist();
}, []);
```

#### ✅ Ürün silme işlemi için:
```js
const handleRemove = async (productId) => {
  const token = localStorage.getItem("token");
  await fetch(`http://localhost:8080/real-wishlist/remove/${productId}`, {
    method: "DELETE",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  setWishlist((prev) => prev.filter((item) => item.product.id !== productId));
};
```

#### ✅ UI Butonlar:
```jsx
<button className="wishlist-button" onClick={handleAddToWishlist}>
  ❤ Add to Wishlist
</button>
```

```jsx
<button className="remove-btn" onClick={() => handleRemove(item.product.id)}>
  Remove
</button>
```