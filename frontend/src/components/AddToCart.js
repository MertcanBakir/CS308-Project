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
      const checkResponse = await fetch("http://localhost:8080/cart", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!checkResponse.ok) {
        throw new Error("Sepet kontrolü başarısız.");
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
          setButtonText("✓ Eklendi");
        } else {
          setButtonText("Güncelleme Hatası");
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
          setButtonText("✓ Eklendi");
        } else {
          setButtonText("Hata!");
          console.error("Add API:", addData);
        }
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