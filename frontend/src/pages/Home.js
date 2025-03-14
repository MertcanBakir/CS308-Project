import React from "react";
import { useNavigate } from "react-router-dom";
import SearchBar from "../components/SearchBar";
import Categories from "../components/Categories";
import TopBanner from "../components/TopBanner";
import sephoraLogo from "../assets/images/sephoraLogo.png"; 
import LoginImage from "../assets/images/LoginImage.png"; 
import CartImage from "../assets/images/cart.png"; 
import Products from "../components/Products";

import "./Home.css";

const Home = () => {
  const navigate = useNavigate();
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
          <div className="header-login-container" onClick={() => navigate("/login")}>
            <img src={LoginImage} alt="Login" className="logologin" />
            <span className="login-text">Login / Register</span>
          </div>

          <div className="cart-container" onClick={() => navigate("/cart")}>
            <img src={CartImage} alt="Cart" className="cart-logo" />
            <span className="cart-text">Cart</span>
          </div>
        </div>
      </div>

      <div className="main-container">
        <Categories />
        <div className="products-container">
          <h2>Featured Products</h2>
          <Products />
        </div>
      </div>
    </div>
  );
};

export default Home;