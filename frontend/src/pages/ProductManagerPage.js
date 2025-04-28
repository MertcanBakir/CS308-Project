import { useNavigate } from "react-router-dom";
import { useEffect, useState, useCallback } from "react";
import { useAuth } from "../context/AuthContext";
import sephoraLogo from "../assets/images/sephoraLogo.png";
import classNames from "classnames"; 
import "./ProductManagerPage.css";

const STATUS_OPTIONS = Object.freeze({
  PROCESSING: "Processing",
  INTRANSIT: "In Transit",
  DELIVERED: "Delivered",
  CANCELLED: "Cancelled",
  REFUNDED: "Refunded",
});

const ProductManagerPage = () => {
  const navigate = useNavigate();
  const { fullname } = useAuth();
  const [orders, setOrders] = useState([]);
  const [comments, setComments] = useState([]);
  const [isLoadingOrders, setIsLoadingOrders] = useState(true);
  const [isLoadingComments, setIsLoadingComments] = useState(true);
  const [error, setError] = useState("");
  const [showWelcome, setShowWelcome] = useState(false);
  const [activeTab, setActiveTab] = useState("orders");

  const fetchOrders = useCallback(async () => {
    try {
      setIsLoadingOrders(true);
      const token = localStorage.getItem("token");
      const response = await fetch("http://localhost:8080/all-order", {
        headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
      });
      if (!response.ok) throw new Error("Failed to fetch orders");
      const data = await response.json();
      setOrders(data);
    } catch (err) {
      setError("An error occurred while receiving orders.");
    } finally {
      setIsLoadingOrders(false);
    }
  }, []);

  const fetchComments = useCallback(async () => {
    try {
      setIsLoadingComments(true);
      const token = localStorage.getItem("token");
      const response = await fetch("http://localhost:8080/comments/all", {
        headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
      });
      if (!response.ok) throw new Error("Failed to fetch comments");
      const data = await response.json();
      setComments(data);
    } catch (err) {
      setError("An error occurred while receiving comments.");
    } finally {
      setIsLoadingComments(false);
    }
  }, []);

  const handleStatusChange = async (orderId, newStatus) => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`http://localhost:8080/${orderId}/status`, {
        method: "PUT",
        headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
        body: JSON.stringify({ status: newStatus }),
      });
      if (!response.ok) throw new Error("Could not update status");
      setOrders((prev) =>
        prev.map((order) => (order.id === orderId ? { ...order, status: newStatus } : order))
      );
    } catch (err) {
      alert(`Could not update status: ${err.message}`);
    }
  };

  const updateCommentApproval = async (commentId, approvedStatus) => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`http://localhost:8080/comments/approve/${commentId}`, {
        method: "PATCH",
        headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
        body: JSON.stringify({ approved: approvedStatus }),
      });
      if (!response.ok) throw new Error("Could not update comments");
      setComments((prev) =>
        prev.map((comment) =>
          comment.id === commentId ? { ...comment, approved: approvedStatus } : comment
        )
      );
    } catch (err) {
      alert(`Could not update comments: ${err.message}`);
    }
  };

  useEffect(() => {
    fetchOrders();
    fetchComments();
    const showTimer = setTimeout(() => setShowWelcome(true), 1000);
    const hideTimer = setTimeout(() => setShowWelcome(false), 9000);
    return () => {
      clearTimeout(showTimer);
      clearTimeout(hideTimer);
    };
  }, [fetchOrders, fetchComments]);

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

      {showWelcome && <div className="productmanager-manager-welcome">Welcome, Product Manager 👋</div>}

      <main className="productmanager-manager-container">
        <h2 className="productmanager-dashboard-heading">Dashboard</h2>

        {/* Tabs */}
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

        {/* Orders */}
        {activeTab === "orders" && (
          <section className="productmanager-orders-section">
            {error && <p className="productmanager-error-message">{error}</p>}
            {isLoadingOrders ? (
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
                        <span className={`productmanager-status-pill productmanager-${status?.toLowerCase()}`}>
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

        {/* Comments */}
        {activeTab === "comments" && (
          <section className="productmanager-comments-section">
            <h3 className="productmanager-orders-heading">Manage Comments</h3>
            {isLoadingComments ? (
              <div className="productmanager-loading-indicator">Loading comments...</div>
            ) : comments.length === 0 ? (
              <p className="productmanager-no-data-message">No comments found.</p>
            ) : (
              <div className="productmanager-order-cards-wrapper">
                {comments.map((comment) => (
                  <div className="productmanager-order-card" key={comment.id}>
                    <div className="productmanager-order-info">
                      <p><strong>Ürün:</strong> {comment.productName}</p>
                      <p><strong>Kullanıcı:</strong> {comment.userFullName} (ID: {comment.userId})</p>
                      <p><strong>Rating:</strong> {comment.rating} ⭐</p>
                      <p><strong>İçerik:</strong> {comment.content}</p>
                      <p><strong>Tarih:</strong> {new Date(comment.createdAt).toLocaleString()}</p>
                      <p>
                        <strong>Durum:</strong>{" "}
                        {comment.approved === true
                          ? "✅ Approved"
                          : comment.approved === false
                          ? "❌ Rejected"
                          : "⏳ Pending"}
                      </p>
                    </div>
                    {comment.approved === null && (
                      <div className="button-group">
                        <button
                          className="productmanager-approve-button"
                          onClick={() => updateCommentApproval(comment.id, true)}
                        >
                          Onayla
                        </button>
                        <button
                          className="productmanager-reject-button"
                          onClick={() => updateCommentApproval(comment.id, false)}
                        >
                          Reddet
                        </button>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </section>
        )}
      </main>
    </div>
  );
};

export default ProductManagerPage;