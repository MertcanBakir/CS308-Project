import { FaTrashCan } from "react-icons/fa6";
import { useAuth } from "../context/AuthContext";

const TrashButton = ({ productId, onDelete }) => {
  const { isLoggedIn, token } = useAuth();

  const handleDelete = async () => {
    if (!productId) {
      alert("No product found to delete!");
      return;
    }

    console.log("✅ Starting the deletion process...");
    console.log("🛑 Product ID to be deleted:", productId);

    if (!isLoggedIn) {
      const localCart = JSON.parse(localStorage.getItem("cart")) || [];
      const updatedCart = localCart.filter((product) => product.id !== productId);
      localStorage.setItem("cart", JSON.stringify(updatedCart));
      onDelete(productId); 
      console.log("🛑 [LOCAL] The item was deleted from localStorage.");
      return;
    }

    const requestBody = { product_id: Number(productId) };
    console.log("📤 Request to be sent:", requestBody);

    try {
      const response = await fetch("http://localhost:8080/delete_from_cart", {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(requestBody),
      });

      console.log("📥 Incoming Response:", response);

      if (!response.ok) {
        throw new Error(`Server error! Status: ${response.status}`);
      }

      const data = await response.json();
      console.log("✅ API Reply:", data);

      if (data.success) {
        console.log("✅ Product successfully deleted:", productId);
        onDelete(productId);
      } else {
        console.error("❌ Product could not be deleted:", data.message);
        alert("Ürün silinemedi: " + data.message);
      }
    } catch (error) {
      console.error("🚨 Delete request failed:", error);
      alert("Deletion failed! Error:" + error.message);
    }
  };

  return (
    <FaTrashCan className="trash-icon" onClick={handleDelete} />
  );
};

export default TrashButton;