import React from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext"; // Auth bilgilerini almak için
import sephoraLogo from "../assets/images/sephoraLogo.png";
import "./Profile.css";

const Profile = () => {
    const navigate = useNavigate();
    const { email, address, card, fullname, logout } = useAuth();

    return (
        <div className="profile-page">
            {/* Header */}
            <div className="header-bar">
                <button onClick={() => navigate("/")} title="Ana Sayfa" className="back-button2">
                    <i className="arrow-left2"></i>
                </button>
                <img
                    src={sephoraLogo}
                    alt="Sephora Logo"
                    className="logo2"
                    onClick={() => navigate("/")}
                />
            </div>

            {/* Kullanıcı Bilgileri */}
            <div className="profile-content">
                <h2>My Profile</h2>
                <p><strong>Full Name:</strong> {fullname || "Not set"}</p>
                <p><strong>Email:</strong> {email || "Not set"}</p>
                <p><strong>Address:</strong> {address ? address : "Not set"}</p>
                <p><strong>Card Info:</strong> {card ? `**** **** **** ${card.slice(-4)}` : "No card information"}</p>

                {/* Çıkış Butonu */}
                <button className="logout-button" onClick={logout}>Logout</button>
            </div>
        </div>
    );
};

export default Profile;
