import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import sephoraLogo from "../assets/images/sephoraLogo.png";
import "./ManagerPage.css";

const ManagerLayout = ({ type }) => {
  const navigate = useNavigate();
  const { fullname } = useAuth();
  const [showWelcome, setShowWelcome] = useState(false);
  const [orders, setOrders] = useState([]);
  const roleText = type === "product" ? "Product Manager" : "Sales Manager";

  useEffect(() => {
    const showTimer = setTimeout(() => setShowWelcome(true), 1000);
    const hideTimer = setTimeout(() => setShowWelcome(false), 9000);
    return () => {
      clearTimeout(showTimer);
      clearTimeout(hideTimer);
    };
  }, []);

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const token = localStorage.getItem("token");
        const response = await fetch("http://localhost:8080/orders", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (!response.ok) throw new Error(`HTTP error! Status: ${response.status}`);
        const data = await response.json();
        setOrders(data);
      } catch (error) {
        console.error("Sipariş çekilirken hata:", error);
      }
    };

    if (type === "product" || type === "order") {
      fetchOrders();
    }
  }, [type]);

  const handleStatusChange = async (orderId, newStatus) => {
    try {
      const token = localStorage.getItem("token");

      const response = await fetch(`http://localhost:8080/orders/${orderId}/status`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ status: newStatus }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error("🚨 Backend Hatası:", errorText);
        throw new Error("Failed to update status");
      }

      const updated = await response.json();
      setOrders((prevOrders) =>
        prevOrders.map((order) =>
          order.id === orderId ? { ...order, deliveryStatus: updated.deliveryStatus } : order
        )
      );
      alert("Teslimat durumu güncellendi!");
    } catch (error) {
      console.error("Update error:", error);
      alert("Teslimat durumu güncellenemedi.");
    }
  };

  return (
    <div style={{ fontFamily: "'Helvetica Neue', sans-serif", backgroundColor: "#f9fafb", minHeight: "100vh" }}>
      {/* Header */}
      <div
        style={{
          position: "relative",
          padding: "24px 32px",
          backgroundColor: "#fff",
          borderBottom: "1px solid #e5e7eb",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        <div
          style={{ position: "absolute", left: "24px", marginTop: "6px", cursor: "pointer" }}
          onClick={() => navigate("/")}
        >
          <div className="arrow-left2" />
        </div>

        <h1 style={{ fontSize: "26px", fontWeight: "700", color: "#111827", margin: 0 }}>
          {roleText} Page
        </h1>

        <img
          src={sephoraLogo}
          alt="Sephora Logo"
          style={{ position: "absolute", right: "24px", height: "32px", cursor: "pointer" }}
          onClick={() => navigate("/")}
        />
      </div>

      {/* Welcome Toast */}
      {showWelcome && (
        <div
          style={{
            position: "fixed",
            bottom: "20px",
            left: "20px",
            fontSize: "16px",
            backgroundColor: "#DB1F6E",
            color: "#fff",
            padding: "12px 20px",
            borderRadius: "12px",
            boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
            opacity: 0.95,
            transition: "opacity 0.5s ease",
            zIndex: 1000,
          }}
        >
          Welcome, {roleText} 👋
        </div>
      )}

      {/* Order Table */}
      {(type === "product" || type === "order") && (
        <div style={{ padding: "40px 60px" }}>
          <h2 style={{ fontSize: "22px", fontWeight: "600", marginBottom: "24px", color: "#1f2937" }}>Orders</h2>
          {orders.length === 0 ? (
            <p style={{ textAlign: "center", color: "#6b7280" }}>No orders found.</p>
          ) : (
            <table style={{ width: "100%", borderCollapse: "collapse", backgroundColor: "#fff", borderRadius: "12px", overflow: "hidden", boxShadow: "0 4px 12px rgba(0,0,0,0.05)" }}>
              <thead style={{ backgroundColor: "#f3f4f6" }}>
                <tr>
                  <th style={{ padding: "16px", fontSize: "12px", textAlign: "left", color: "#6b7280", fontWeight: 600 }}>Order ID</th>
                  <th style={{ padding: "16px", fontSize: "12px", textAlign: "left", color: "#6b7280", fontWeight: 600 }}>Product</th>
                  <th style={{ padding: "16px", fontSize: "12px", textAlign: "left", color: "#6b7280", fontWeight: 600 }}>Status</th>
                  <th style={{ padding: "16px", fontSize: "12px", textAlign: "left", color: "#6b7280", fontWeight: 600 }}>Change Status</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((order) => (
                  <tr key={order.id} style={{ borderTop: "1px solid #e5e7eb" }}>
                    <td style={{ padding: "16px", fontSize: "14px", color: "#374151" }}>#{order.id}</td>
                    <td style={{ padding: "16px", fontSize: "14px", color: "#374151" }}>{order.product?.name || "N/A"}</td>
                    <td style={{ padding: "16px" }}>
                      <span style={{
                        padding: "6px 12px",
                        borderRadius: "9999px",
                        fontSize: "12px",
                        fontWeight: 500,
                        backgroundColor:
                          order.deliveryStatus === "Delivered"
                            ? "#d1fae5"
                            : order.deliveryStatus === "Processing"
                            ? "#fef3c7"
                            : order.deliveryStatus === "On the Way"
                            ? "#bfdbfe"
                            : "#fcdcdc",
                        color:
                          order.deliveryStatus === "Delivered"
                            ? "#065f46"
                            : order.deliveryStatus === "Processing"
                            ? "#92400e"
                            : order.deliveryStatus === "On the Way"
                            ? "#1e40af"
                            : "#991b1b",
                      }}>
                        {order.deliveryStatus}
                      </span>
                    </td>
                    <td style={{ padding: "16px" }}>
                      <select
                        style={{
                          padding: "8px 12px",
                          border: "1px solid #d1d5db",
                          borderRadius: "8px",
                          backgroundColor: "#fff",
                          fontSize: "14px",
                          width: "100%",
                          maxWidth: "180px",
                        }}
                        value={order.deliveryStatus}
                        onChange={(e) => handleStatusChange(order.id, e.target.value)}
                      >
                        <option value="Processing">Processing</option>
                        <option value="On the Way">On the Way</option>
                        <option value="Delivered">Delivered</option>
                      </select>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  );
};

export default ManagerLayout;
