import React, { useEffect, useState } from "react";

const OrderDetail = ({ orderId }) => {
  const [order, setOrder] = useState(null);
  const [status, setStatus] = useState("");

  useEffect(() => {
    const fetchOrder = async () => {
      try {
        const token = localStorage.getItem("token");
        const response = await fetch(`http://localhost:3000/orders/${orderId}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (!response.ok) {
          throw new Error("Failed to fetch order");
        }

        const data = await response.json();
        setOrder(data);
        setStatus(data.deliveryStatus); // backendden gelen mevcut status
      } catch (error) {
        console.error("Error fetching order:", error);
      }
    };

    fetchOrder();
  }, [orderId]);

  const handleStatusChange = async (newStatus) => {
    try {
      const token = localStorage.getItem("token");

      const response = await fetch(`http://localhost:3000/orders/${orderId}/delivery-status`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ status: newStatus }),
      });

      if (!response.ok) {
        throw new Error("Failed to update status");
      }

      const updated = await response.json();
      setStatus(updated.deliveryStatus); // güncellenmiş statüyü göster
      alert("Teslimat durumu güncellendi!");
    } catch (error) {
      console.error("Error updating status:", error);
      alert("Güncelleme sırasında hata oluştu.");
    }
  };

  if (!order) {
    return <p>Loading order details...</p>;
  }

  return (
    <div>
      <h2>Order #{order.id}</h2>
      <p><strong>Current Status:</strong> {status}</p>

      <label htmlFor="statusSelect">Change Delivery Status:</label>
      <select
        id="statusSelect"
        value={status}
        onChange={(e) => handleStatusChange(e.target.value)}
      >
        <option value="Processing">Processing</option>
        <option value="On the Way">On the Way</option>
        <option value="Delivered">Delivered</option>
      </select>
    </div>
  );
};

export default OrderDetail;
