import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import TopBanner from "../components/TopBanner";
import SearchBar from "../components/SearchBar";
import sephoraLogo from "../assets/images/sephoraLogo.png";
import LoginImage from "../assets/images/LoginImage.png";
import CartImage from "../assets/images/cart.png";
import "./ProductDetail.css"; 
import AddToCart from "../components/AddToCart";
import { useAuth } from "../context/AuthContext"; 

const ProductDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { isLoggedIn, logout } = useAuth(); 
    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchProduct = async () => {
            try {
                const response = await fetch(`http://localhost:8080/products/${id}`);
                if (!response.ok) {
                    throw new Error("Ürün bilgileri getirilemedi!");
                }
                const data = await response.json();
                setProduct(data);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchProduct();
    }, [id]);

    if (loading) return <p>Ürün yükleniyor...</p>;
    if (error) return <p>Hata: {error}</p>;

    return (
        <div className="home">
            <TopBanner />
            <div className="tools">
                <img 
                    src={sephoraLogo} 
                    alt="Sephora Logo" 
                    className="logo" 
                    onClick={() => navigate("/")}
                />
                <SearchBar />
                <div className="right-tools">
                    {isLoggedIn ? ( 
                        <div className="header-login-container" onClick={logout}>
                            <img src={LoginImage} alt="Logout" className="logologin" />
                            <span className="login-text">Logout</span>
                        </div>
                    ) : (
                        <div className="header-login-container" onClick={() => navigate("/login")}>
                            <img src={LoginImage} alt="Login" className="logologin" />
                            <span className="login-text">Login / Register</span>
                        </div>
                    )}
                    <div className="cart-container" onClick={() => navigate("/cart")}>
                        <img src={CartImage} alt="Cart" className="cart-logo" />
                        <span className="cart-text">Cart</span>
                    </div>
                </div>
            </div>
            
            <div className="product-detail-container">
                <div className="back-button">
                    <a href="/" title="Back to Home">
                        <i className="arrow-left"></i>
                    </a>
                </div>
                <div className="productimage-container">
                    <img src={product.imageUrl} alt={product.name} className="product-image" />
                </div>
                <div className="product-info-container">
                    <h1 className="product-title">{product.name}</h1>
                    <p className="product-description">{product.description}</p>
                    <p><strong>Price:</strong> {product.price}₺</p>
                    <p><strong>Model:</strong> {product.model}</p>
                    <p><strong>Stock:</strong> {product.quantityInStock}</p>
                    <p><strong>Guarantee:</strong> {product.warrantyStatus ? "Var" : "Yok"}</p>
                    <p><strong>Distributor:</strong> {product.distributorInfo}</p>
                    
                    {/* Conditionally render "Out of Stock" message */}
                    {product.quantityInStock === 0 ? (
                        <button className="out-of-stock-button" disabled>
                            Stokta Yok
                        </button>
                    ) : (
                        <AddToCart product={product} />
                    )}
                </div>
            </div>
        </div>
    );
};

export default ProductDetail;
