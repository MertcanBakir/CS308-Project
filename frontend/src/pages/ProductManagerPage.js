import { useNavigate } from "react-router-dom";
import { useEffect, useState, useCallback } from "react";
import { useAuth } from "../context/AuthContext";
import sephoraLogo from "../assets/images/sephoraLogo.png";
import classNames from "classnames"; // npm install classnames
import "./ProductManagerPage.css";

const STATUS_OPTIONS = Object.freeze({
  PROCESSING: "Processing",
  SHIPPED: "Shipped",
  DELIVERED: "Delivered",
  CANCELLED: "Cancelled",
  REFUNDED: "Refunded"
});

const ProductManagerPage = () => {
  const navigate = useNavigate();
  const { fullname } = useAuth();
  const [orders, setOrders] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [showWelcome, setShowWelcome] = useState(false);
  const [activeTab, setActiveTab] = useState("orders");

  const fetchOrders = useCallback(async () => {
    try {
      setIsLoading(true);
      const token = localStorage.getItem("token");
      if (!token) throw new Error("Authentication token not found");

      const response = await fetch("http://localhost:8080/all-order", {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json"
        }
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || "Failed to fetch orders");
      }

      const data = await response.json();
      setOrders(data);
      setError("");
    } catch (err) {
      console.error("Order fetch error:", err);
      setError("Siparişler alınırken bir hata oluştu.");
    } finally {
      setIsLoading(false);
    }
  }, []);

  const handleStatusChange = async (orderId, newStatus) => {
    try {
      const token = localStorage.getItem("token");
      if (!token) throw new Error("Authentication token not found");

      const response = await fetch(`http://localhost:8080/${orderId}/status`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({ status: newStatus })
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || "Durum güncellenemedi");
      }

      setOrders(prev =>
        prev.map(order =>
          order.id === orderId ? { ...order, status: newStatus } : order
        )
      );
    } catch (err) {
      console.error("Status update error:", err);
      alert(`Durum güncellenemedi: ${err.message}`);
    }
  };

  useEffect(() => {
    fetchOrders();
    const showTimer = setTimeout(() => setShowWelcome(true), 1000);
    const hideTimer = setTimeout(() => setShowWelcome(false), 9000);
    return () => {
      clearTimeout(showTimer);
      clearTimeout(hideTimer);
    };
  }, [fetchOrders]);

  const handleNavigateHome = () => navigate("/");

  return (
    <div className="productmanager-product-manager-page">
      <header className="productmanager-manager-header">
        <div className="productmanager-back-button2" onClick={handleNavigateHome}>
          <div className="productmanager-arrow-left2" />
        </div>
        <h1 className="productmanager-manager-title">Product Manager Page</h1>
        <img
          src={sephoraLogo}
          alt="Sephora Logo"
          className="productmanager-logo2"
          onClick={handleNavigateHome}
        />
      </header>

      {showWelcome && (
        <div className="productmanager-manager-welcome">
          Welcome, Product Manager 👋
        </div>
      )}

      <main className="productmanager-manager-container">
        <h2 className="productmanager-dashboard-heading">Dashboard</h2>

        {/* Tab Buttons */}
        <div className="productmanager-tab-buttons">
          <button
            className={classNames("productmanager-tab", { active: activeTab === "orders" })}
            onClick={() => setActiveTab("orders")}
          >
            Orders
          </button>
          <button
            className={classNames("productmanager-tab", { active: activeTab === "comments" })}
            onClick={() => setActiveTab("comments")}
          >
            Comments
          </button>
        </div>

        {/* Orders Section */}
        {activeTab === "orders" && (
          <section className="productmanager-orders-section">
            <h3 className="productmanager-orders-heading">Orders</h3>
            {error && <p className="productmanager-error-message">{error}</p>}
            {isLoading ? (
              <div className="productmanager-loading-indicator">Loading orders...</div>
            ) : orders.length === 0 ? (
              <p className="productmanager-no-data-message">No orders found.</p>
            ) : (
              <div className="productmanager-order-cards-wrapper">
                {orders.map(({ id, product, status }) => (
                  <div className="productmanager-order-card" key={id}>
                    <div className="productmanager-order-info">
                      <p><strong>Order ID:</strong> #{id}</p>
                      <p><strong>Product:</strong> {product?.name || "N/A"}</p>
                      <p>
                        <strong>Status:</strong>{" "}
                        <span className={`productmanager-status-pill productmanager-${status.toLowerCase()}`}>
                          {STATUS_OPTIONS[status] || status}
                        </span>
                      </p>
                    </div>
                    <div className="productmanager-status-change">
                      <label htmlFor={`status-${id}`}>Change Status:</label>
                      <select
                        id={`status-${id}`}
                        className="productmanager-status-dropdown"
                        value={status}
                        onChange={(e) => handleStatusChange(id, e.target.value)}
                      >
                        {Object.keys(STATUS_OPTIONS).map((option) => (
                          <option key={option} value={option}>
                            {STATUS_OPTIONS[option]}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        )}

        {/* Comments Section */}
        {activeTab === "comments" && (
          <section className="productmanager-comments-section">
            <h3 className="productmanager-orders-heading">Manage Comments</h3>
            <p className="productmanager-no-data-message">No comments yet.</p>
          </section>
        )}
      </main>
    </div>
  );
};

export default ProductManagerPage;
