import React, { useEffect, useState } from "react";
import "./Wishlist.css";
import { useNavigate } from "react-router-dom";
import sephoraLogo from "../assets/images/sephoraLogo.png";

const Wishlist = () => {
    const [wishlist, setWishlist] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchWishlist = async () => {
            const token = localStorage.getItem("token");
            const res = await fetch("http://localhost:8080/wishlist", {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            const data = await res.json();
            setWishlist(data);
        };
        fetchWishlist();
    }, []);

    return (
        <div className="wishlist-page">
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

            <div className="wishlist-content">
                <h2>My Wishlist</h2>
                {wishlist.length === 0 ? (
                    <p>No items in wishlist.</p>
                ) : (
                    wishlist.map((item) => (
                        <div key={item.id} className="wishlist-item">
                            <img
                                src={item.product.imageUrl}
                                alt={item.product.name}
                                className="wishlist-image"
                            />
                            <div className="wishlist-info">
                                <h3>{item.product.name}</h3>
                                <p>{item.product.description}</p>
                                <p><strong>{item.product.price}₺</strong></p>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
};

export default Wishlist;
