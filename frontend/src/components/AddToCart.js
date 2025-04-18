import { useState } from "react";
import { useAuth } from "../context/AuthContext";

const AddToCart = ({ product }) => {
  const { isLoggedIn, token } = useAuth();
  const [isAdding, setIsAdding] = useState(false);
  const [buttonText, setButtonText] = useState("Add to Cart");

  const handleAddToCart = async () => {
    if (!product || !product.id) {
      setButtonText("Product Not Found!");
      setTimeout(() => setButtonText("Add to cart"), 2500);
      return;
    }

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
      setButtonText("✓ Added");
      setTimeout(() => setButtonText("Add to Cart"), 2500);
      setIsAdding(false);
      return;
    }

    try {
      const checkResponse = await fetch("http://localhost:8080/cart", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!checkResponse.ok) {
        throw new Error("Cart control failed.");
      }

      const checkData = await checkResponse.json();
      const cartItems = checkData.products || [];

      const existingItem = cartItems.find((item) => item.id === product.id);

      if (existingItem) {
        const newQuantity = (existingItem.quantity || 1) + 1;

        const quantityResponse = await fetch("http://localhost:8080/change_quantity", {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({
            product_id: product.id,
            quantity: newQuantity,
          }),
        });

        const quantityData = await quantityResponse.json();

        if (quantityResponse.ok && quantityData.message) {
          setButtonText("✓ Added");
        } else {
          setButtonText("Update Error");
          console.error("Quantity API:", quantityData);
        }
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

        const addData = await addResponse.json();

        if (addResponse.ok && addData.success) {
          setButtonText("✓ Added");
        } else {
          setButtonText("Error!");
          console.error("Add API:", addData);
        }
      }
    } catch (error) {
      console.error("Add to cart error:", error);
      setButtonText("Connection Error!");
    }

    setTimeout(() => setButtonText("Add to Cart"), 2500);
    setIsAdding(false);
  };

  return (
    <button
      className="add-to-cart-button"
      onClick={handleAddToCart}
      disabled={isAdding}
    >
      {buttonText}
    </button>
  );
};

export default AddToCart;