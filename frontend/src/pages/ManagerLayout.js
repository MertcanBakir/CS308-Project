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
    <div style={{ fontFamily: "'Helvetica Neue', sans-serif", backgroundColor: "#fafafa", minHeight: "100vh" }}>
      {/* Header */}
      <div
        style={{
          position: "relative",
          padding: "24px 32px",
          backgroundColor: "#fff",
          borderBottom: "1px solid #e0e0e0",
          boxShadow: "0 2px 8px rgba(0,0,0,0.05)",
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

        <h1 style={{ fontSize: "26px", fontWeight: "600", color: "#333", margin: 0 }}>
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
            padding: "10px 16px",
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
        <div style={{ padding: "20px 40px" }}>
          <h2 style={{ textAlign: "center", marginBottom: "24px" }}>Orders</h2>
          {orders.length === 0 ? (
            <p style={{ textAlign: "center" }}>No orders found.</p>
          ) : (
            <table style={{ width: "100%", borderCollapse: "collapse", textAlign: "center" }}>
              <thead>
                <tr>
                  <th style={{ padding: "12px", borderBottom: "1px solid #ddd", width: "20%" }}>Order ID</th>
                  <th style={{ padding: "12px", borderBottom: "1px solid #ddd", width: "40%" }}>Status</th>
                  <th style={{ padding: "12px", borderBottom: "1px solid #ddd", width: "40%" }}>
                    {type === "product" ? "Change Status" : ""}
                  </th>
                </tr>
              </thead>
              <tbody>
                {orders.map((order) => (
                  <tr key={order.id}>
                    <td style={{ padding: "12px", verticalAlign: "middle" }}>{order.id}</td>
                    <td style={{ padding: "12px", verticalAlign: "middle" }}>{order.deliveryStatus}</td>
                    <td style={{ padding: "12px", verticalAlign: "middle" }}>
                      {type === "product" ? (
                        <select
                          style={{
                            padding: "8px",
                            width: "100%",
                            maxWidth: "180px",
                            borderRadius: "6px",
                            border: "1px solid #ccc",
                          }}
                          value={order.deliveryStatus}
                          onChange={(e) => handleStatusChange(order.id, e.target.value)}
                        >
                          <option value="Processing">Processing</option>
                          <option value="On the Way">On the Way</option>
                          <option value="Delivered">Delivered</option>
                        </select>
                      ) : (
                        "-"
                      )}
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
