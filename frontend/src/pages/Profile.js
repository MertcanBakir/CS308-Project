import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import sephoraLogo from "../assets/images/sephoraLogo.png";
import "./Profile.css";

const Profile = () => {
  const navigate = useNavigate();
  const { logout, token } = useAuth();

  const [profile, setProfile] = useState({});
  const [addresses, setAddresses] = useState([]);
  const [cards, setCards] = useState([]);
  const [orders, setOrders] = useState([]);

  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [passwordMessage, setPasswordMessage] = useState("");
  const [messageType, setMessageType] = useState("");

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = await fetch(`${process.env.REACT_APP_API_URL}/profile/full`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (!response.ok) throw new Error("Failed to fetch profile data");

        const data = await response.json();
        setProfile({
          id: data.id,
          passwordInfo: data.passwordInfo,
          email: data.email,
          fullName: data.fullName,
        });
        setAddresses(data.addresses || []);
        setCards(data.cards || []);
        setOrders(data.orders || []);
      } catch (err) {
        console.error("Error fetching full profile:", err);
      }
    };
    fetchProfile();
  }, [token]);

  const handlePasswordChange = async (e) => {
    e.preventDefault();

    if (newPassword !== confirmPassword) {
      setPasswordMessage("New passwords do not match.");
      setMessageType("error");
      return;
    }

    try {
      const response = await fetch(`${process.env.REACT_APP_API_URL}/profile/change-password`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ oldPassword, newPassword }),
      });

      const data = await response.json();
      if (response.ok) {
        setPasswordMessage("Password changed successfully.");
        setMessageType("success");
        setOldPassword("");
        setNewPassword("");
        setConfirmPassword("");
      } else {
        setPasswordMessage(data.message || "Failed to change password.");
        setMessageType("error");
      }
    } catch (err) {
      console.error("Password change error:", err);
      setPasswordMessage("An error occurred while changing password.");
      setMessageType("error");
    }
  };

  const handleRefundRequest = async (orderId) => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`${process.env.REACT_APP_API_URL}/orders/${orderId}/request-refund`, {
        method: "PATCH",
        headers: { Authorization: `Bearer ${token}` },
      });

      let message = "Refund request failed.";
      try {
        const data = await response.json();
        message = data.message || message;
      } catch (jsonErr) {
        console.warn("Non-JSON response received.");
      }

      if (response.ok) {
        alert("Refund request submitted.");
        window.location.reload();
      } else {
        alert(`${message}`);
      }

    } catch (error) {
      console.error("Refund request error:", error);
      alert("An error occurred while requesting a refund.");
    }
  };

  const handleCancel = async (orderId) => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`${process.env.REACT_APP_API_URL}/orders/${orderId}/cancel`, {
        method: "PATCH",
        headers: { Authorization: `Bearer ${token}` },
      });

      const data = await response.json();
      if (response.ok) {
        alert("Order cancelled.");
        window.location.reload();
      } else {
        alert(data.message || "Failed to cancel order.");
      }
    } catch (err) {
      console.error("Cancel error:", err);
      alert("An error occurred while cancelling the order.");
    }
  };

  return (
    <div className="profile-page">
      <div className="header-bar">
        <button onClick={() => navigate(-1)} title="Go Back" className="back-button2">
          <i className="arrow-left2"></i>
        </button>
        <img src={sephoraLogo} alt="Sephora Logo" className="logo2" onClick={() => navigate("/")} />
      </div>

      <div className="profile-content">
        <h2>Change Password</h2>
        <div className="change-password-container">
          <form onSubmit={handlePasswordChange} className="password-form-inline">
            <input type="password" value={oldPassword} onChange={(e) => setOldPassword(e.target.value)} placeholder="Old Password" required />
            <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} placeholder="New Password" required />
            <input type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} placeholder="Confirm New Password" required />
            <button type="submit" className="change-password-btn">Change Password</button>
            {passwordMessage && <div className={`password-message ${messageType}`}>{passwordMessage}</div>}
          </form>
        </div>

        <h2>Profile</h2>
        <div className="profile-info">
          <p><strong>Name Surname:</strong> {profile.fullName || "Not set"}</p>
          <p><strong>Email Address:</strong> {profile.email || "Not set"}</p>
          <p><strong>User ID:</strong> {profile.id || "N/A"}</p>
          <p><strong>Password:</strong> {profile.passwordInfo || "********"}</p>
        </div>

        <h3>Addresses:</h3>
        <div className="addresses-section">
          {addresses.length > 0 ? addresses.map((addr, i) => <p key={i}>{addr}</p>) : <p>No addresses found.</p>}
        </div>

        <h3>Cards:</h3>
        <div className="cards-section">
          {cards.length > 0 ? cards.map((c, i) => <p key={i}>**** **** **** {c}</p>) : <p>No cards found.</p>}
        </div>

        <h3>Past Orders:</h3>
        <div className="orders-section">
          {orders.length > 0 ? (
            orders.map((order, index) => {
              const orderDate = new Date(order.createdAt);
              const thirtyDaysAgo = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000);
              console.log("Refund status:", order.refundStatus); 

              return (
                <div key={index} className="order-item">
                  <div className="order-details">
                    <p><strong>Transaction Date:</strong> {orderDate.toLocaleString()}</p>
                    <p><strong>Order Status:</strong> {order.status}</p>
                    <p><strong>Product:</strong> {order.productName}</p>
                    <p><strong>Address:</strong> {order.addressText}</p>

                    {order.status === "PROCESSING" && (
                      <button onClick={() => handleCancel(order.id)} className="cancel-button">
                        Cancel Order
                      </button>
                    )}

{order.status === "DELIVERED" &&
  new Date(order.createdAt) >= new Date(Date.now() - 30 * 24 * 60 * 60 * 1000) && (
    <>
      {(!order.refundRequestStatus || order.refundRequestStatus === "NONE") && (
        <button
          onClick={() => handleRefundRequest(order.id)}
          className="refund-button"
        >
          Request Refund
        </button>
      )}
      {order.refundRequestStatus === "PENDING" && (
        <p className="refund-pending">Refund request pending.</p>
      )}
      {order.refundRequestStatus === "REJECTED" && (
        <p className="refund-rejected">Refund request was not approved.</p>
      )}
      {order.refundRequestStatus === "APPROVED" && (
        <p className="refund-approved">Refund approved.</p>
      )}
    </>
)}


                  </div>
                  {order.productImageUrl && (
                      <div className="order-image">
                        <img src={order.productImageUrl} alt={order.productName} className="profile-order-img" />
                      </div>
                  )}

                </div>
              );
            })
          ) : (
            <p>No orders found.</p>
          )}
        </div>

        <button className="logout-button" onClick={logout}>Logout</button>
      </div>
    </div>
  );
};

export default Profile;
