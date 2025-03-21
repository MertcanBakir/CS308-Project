import { useState } from "react";
import { useAuth } from "../context/AuthContext";

const AddToCart = ({ product }) => {
  const { isLoggedIn, token } = useAuth();
  const [isAdding, setIsAdding] = useState(false);
  const [buttonText, setButtonText] = useState("Sepete Ekle");

  const handleAddToCart = async () => {
    if (!product || !product.id) {
      setButtonText("Ürün Bulunamadı!");
      setTimeout(() => setButtonText("Sepete Ekle"), 2500);
      return;
    }

    setIsAdding(true);
    setButtonText("Ekleniyor...");

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
      setButtonText("✓ Eklendi");
      setTimeout(() => setButtonText("Sepete Ekle"), 2500);
      setIsAdding(false);
      return;
    }

    try {
      const response = await fetch("http://localhost:8080/add_to_cart", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ product_id: product.id, quantity }),
      });

      const data = await response.json();
      if (response.ok && data.success) {
        setButtonText("✓ Eklendi");
      } else {
        setButtonText("Zaten Sepette!");
        console.error("API Yanıtı:", data);
      }
    } catch (error) {
      console.error("Sepete ekleme hatası:", error);
      setButtonText("Bağlantı Hatası!");
    }

    setTimeout(() => setButtonText("Sepete Ekle"), 2500);
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