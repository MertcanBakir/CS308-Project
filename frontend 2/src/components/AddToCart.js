import { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";

const AddToCart = ({ product }) => {
  const { isLoggedIn, token } = useAuth();
  const [isAdding, setIsAdding] = useState(false);
  const [buttonText, setButtonText] = useState("Add to Cart");
  const [isOutOfStock, setIsOutOfStock] = useState(false);

  useEffect(() => {
    checkStockStatus();
  }, [product]);

  const checkStockStatus = async () => {
    if (!product || !product.id) return;

    if (!isLoggedIn) {
      const localCart = JSON.parse(localStorage.getItem("cart")) || [];
      const existingItem = localCart.find((item) => item.id === product.id);
      if (existingItem && existingItem.quantity >= product.quantityInStock) {
        setButtonText("Out of Stock");
        setIsOutOfStock(true);
      }
      return;
    }

    try {
      const response = await fetch("http://localhost:8080/cart", {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        throw new Error("Failed to check cart");
      }

      const data = await response.json();
      const existingItem = (data.products || []).find((item) => item.id === product.id);

      if (existingItem && existingItem.quantity >= product.quantityInStock) {
        setButtonText("Out of Stock");
        setIsOutOfStock(true);
      }
    } catch (error) {
      console.error("Error checking stock:", error);
    }
  };

  const handleAddToCart = async () => {
    if (isOutOfStock || !product || !product.id) return;

    setIsAdding(true);
    setButtonText("Adding...");

    const quantity = 1;

    if (!isLoggedIn) {
      const localCart = JSON.parse(localStorage.getItem("cart")) || [];
      const existingItem = localCart.find((item) => item.id === product.id);

      if (existingItem) {
        existingItem.quantity = (existingItem.quantity || 1) + 1;
      } else {
        localCart.push({ ...product, quantity });
      }

      localStorage.setItem("cart", JSON.stringify(localCart));
    } else {
      const addResponse = await fetch("http://localhost:8080/add_to_cart", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          product_id: product.id,
          quantity: quantity,
        }),
      });

      if (!addResponse.ok) {
        console.error("Error adding to cart");
      }
    }

    await checkStockStatus(); 
    setButtonText(isOutOfStock ? "Out of Stock" : "✓ Added");
    setTimeout(() => {
      if (!isOutOfStock) setButtonText("Add to Cart");
    }, 2500);

    setIsAdding(false);
  };

  return (
    <button
      className="add-to-cart-button"
      onClick={handleAddToCart}
      disabled={isAdding || isOutOfStock}
    >
      {buttonText}
    </button>
  );
};

export default AddToCart;