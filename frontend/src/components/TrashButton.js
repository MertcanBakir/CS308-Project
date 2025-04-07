import { FaTrashCan } from "react-icons/fa6";
import { useAuth } from "../context/AuthContext";

const TrashButton = ({ productId, onDelete }) => {
  const { isLoggedIn, token } = useAuth();

  const handleDelete = async () => {
    if (!productId) {
      alert("Silinecek ürün bulunamadı!");
      return;
    }

    console.log("✅ Silme işlemi başlatılıyor...");
    console.log("🛑 Silinecek ürün ID:", productId);

    if (!isLoggedIn) {
      const localCart = JSON.parse(localStorage.getItem("cart")) || [];
      const updatedCart = localCart.filter((product) => product.id !== productId);
      localStorage.setItem("cart", JSON.stringify(updatedCart));
      onDelete(productId); 
      console.log("🛑 [LOCAL] Ürün localStorage'dan silindi.");
      return;
    }

    const requestBody = { product_id: Number(productId) };
    console.log("📤 Gönderilecek istek:", requestBody);

    try {
      const response = await fetch("http://localhost:8080/delete_from_cart", {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(requestBody),
      });

      console.log("📥 Gelen Yanıt:", response);

      if (!response.ok) {
        throw new Error(`Sunucu hatası! Status: ${response.status}`);
      }

      const data = await response.json();
      console.log("✅ API Yanıtı:", data);

      if (data.success) {
        console.log("✅ Ürün başarıyla silindi:", productId);
        onDelete(productId);
      } else {
        console.error("❌ Ürün silinemedi:", data.message);
        alert("Ürün silinemedi: " + data.message);
      }
    } catch (error) {
      console.error("🚨 Silme isteği başarısız:", error);
      alert("Silme işlemi başarısız! Hata: " + error.message);
    }
  };

  return (
    <FaTrashCan className="trash-icon" onClick={handleDelete} />
  );
};

export default TrashButton;