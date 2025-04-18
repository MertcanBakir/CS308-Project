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
        fetch(`http://localhost:8080/profile?email=${email}`, {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        })
            .then(res => res.json())
            .then(data => {
                setAddresses(data.addresses || []);
                setCards(data.cards || []);
            })
            .catch(err => console.error("Profile information could not be retrieved:", err));

        fetch(`http://localhost:8080/profile/orders?email=${email}`, {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        })
            .then(res => res.json())
            .then(data => {
                setOrders(data || []);
            })
            .catch(err => console.error("Orders could not be received:", err));
    }, [email, token]);

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
                            cards.map((c, index) => (
                                <li key={index}>**** **** **** {c}</li>
                            ))
                        ) : (
                            <li>No cards found.</li>
                        )}
                    </ul>
                </div>

                <div>
                    <div>
                        <div>
                            <strong>Past Orders:</strong>
                            <ul>
                                {orders.length > 0 ? (
                                    orders.map((order, index) => (
                                        <li key={index} className="order-card">
                                            <div className="order-header-horizontal">
                                                <div className="order-info">
                                                    <p>
                                                        <strong>Transaction Date:</strong> {new Date(order.createdAt).toLocaleString()}
                                                    </p>
                                                    <p><strong>Order Status:</strong> {order.status}</p>
                                                    <p><strong>Product:</strong> {order.productName}</p>
                                                    <p><strong>Quantity:</strong> {order.quantity}</p>
                                                    <p><strong>Address:</strong> {order.addressText}</p>
                                                    <p><strong>Credit Card:</strong> **** **** **** {order.cardLast4}</p>
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

                    </div>
                </div>

                <button className="logout-button" onClick={logout}>Logout</button>
            </div>
        </div>
    );
};

export default Profile;
