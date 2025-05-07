import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import sephoraLogo from "../assets/images/sephoraLogo.png";
import "./Profile.css";

const Profile = () => {
  const navigate = useNavigate();
  const { email, fullname, logout, token } = useAuth();

  const [addresses, setAddresses] = useState([]);
  const [cards, setCards] = useState([]);
  const [orders, setOrders] = useState([]);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = await fetch("http://localhost:8080/profile/full", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (!response.ok) {
          throw new Error("Failed to fetch profile data");
        }

        const data = await response.json();
        setAddresses(data.addresses || []);
        setCards(data.cards || []);
        setOrders(data.orders || []);
      } catch (err) {
        console.error("Error fetching full profile:", err);
      }
    };

    fetchProfile();
  }, [token]);

  const fetchInvoiceBlobUrl = async (orderId) => {
    const response = await fetch(`http://localhost:8080/invoices/${orderId}/download`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (!response.ok) {
      throw new Error("Failed to fetch invoice.");
    }

    const blob = await response.blob();
    return window.URL.createObjectURL(blob);
  };

  const handleDownloadInvoice = async (orderId) => {
    try {
      const url = await fetchInvoiceBlobUrl(orderId);
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", `invoice-${orderId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error("Error downloading invoice:", err);
      alert("Could not download invoice.");
    }
  };

  const handleViewInvoice = async (orderId) => {
    try {
      const url = await fetchInvoiceBlobUrl(orderId);
      window.open(url, "_blank");
    } catch (err) {
      console.error("Error viewing invoice:", err);
      alert("Could not open invoice.");
    }
  };

  const handleCancelOrder = async (orderId) => {
    try {
      const res = await fetch(`http://localhost:8080/orders/${orderId}/cancel`, {
        method: "PATCH",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      const data = await res.json();
      if (res.ok) {
        alert("Order cancelled successfully!");
        window.location.reload();
      } else {
        alert(data.message || "Cancel failed.");
      }
    } catch (err) {
      console.error("Cancel error:", err);
      alert("An error occurred during cancellation.");
    }
  };

  const handleRefundOrder = async (orderId) => {
    try {
      const res = await fetch(`http://localhost:8080/orders/${orderId}/refund`, {
        method: "PATCH",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      const data = await res.json();
      if (res.ok) {
        alert(`Refunded successfully. Amount: ${data.amount}₺`);
        window.location.reload();
      } else {
        alert(data.message || "Refund failed.");
      }
    } catch (err) {
      console.error("Refund error:", err);
      alert("An error occurred during refund.");
    }
  };

  return (
    <div className="profile-page">
      <div className="header-bar">
        <button onClick={() => navigate("/")} title="Home Page" className="back-button2">
          <i className="arrow-left2"></i>
        </button>
        <img
          src={sephoraLogo}
          alt="Sephora Logo"
          className="logo2"
          onClick={() => navigate("/")}
        />
      </div>

      <div className="profile-content">
        <h2>Profil</h2>
        <p><strong>Name Surname:</strong> {fullname || "Not set"}</p>
        <p><strong>Email Address:</strong> {email || "Not set"}</p>

        <div>
          <strong>Addresses:</strong>
          <ul>
            {addresses.length > 0 ? (
              addresses.map((addr, index) => <li key={index}>{addr}</li>)
            ) : (
              <li>No addresses found.</li>
            )}
          </ul>
        </div>

        <div>
          <strong>Cards:</strong>
          <ul>
            {cards.length > 0 ? (
              cards.map((c, index) => <li key={index}>**** **** **** {c}</li>)
            ) : (
              <li>No cards found.</li>
            )}
          </ul>
        </div>

        <div>
          <strong>Past Orders:</strong>
          <ul>
            {orders.length > 0 ? (
              orders.map((order, index) => (
                <li key={index} className="order-card">
                  <div className="order-header-horizontal">
                    <div className="order-info">
                      <p><strong>Transaction Date:</strong> {new Date(order.createdAt).toLocaleString()}</p>
                      <p><strong>Order Status:</strong> {order.status}</p>
                      <p><strong>Product:</strong> {order.productName}</p>
                      <p><strong>Quantity:</strong> {order.quantity}</p>
                      <p><strong>Address:</strong> {order.addressText}</p>
                      <p><strong>Credit Card:</strong> **** **** **** {order.cardLast4}</p>

                      {order.invoiceId ? (
                        <div className="invoice-buttons">
                          <button onClick={() => handleViewInvoice(order.id)} className="invoice-button">
                            See Invoice
                          </button>
                          <button onClick={() => handleDownloadInvoice(order.id)} className="invoice-button">
                            Download Invoice
                          </button>
                        </div>
                      ) : (
                        <p>No invoice available</p>
                      )}

                      {order.status === "PROCESSING" && (
                        <button
                          onClick={() => handleCancelOrder(order.id)}
                          className="cancel-button"
                        >
                          Cancel Order
                        </button>
                      )}

                      {order.status === "DELIVERED" &&
                        new Date(order.createdAt) >= new Date(Date.now() - 30 * 24 * 60 * 60 * 1000) && (
                          <button
                            onClick={() => handleRefundOrder(order.id)}
                            className="refund-button"
                          >
                            Refund Order
                          </button>
                      )}
                    </div>

                    <img
                      src={order.productImageUrl || "https://via.placeholder.com/100"}
                      alt={order.productName}
                      className="product-image-right"
                    />
                  </div>
                </li>
              ))
            ) : (
              <li>No orders found.</li>
            )}
          </ul>
        </div>

        <button className="logout-button" onClick={logout}>Logout</button>
      </div>
    </div>
  );
};

export default Profile;
