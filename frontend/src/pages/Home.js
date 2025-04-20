import React, {useEffect, useState} from "react";
import { useNavigate } from "react-router-dom";
import SearchBar from "../components/SearchBar";
import Categories from "../components/Categories";
import TopBanner from "../components/TopBanner";
import Products from "../components/Products";
import { useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import sephoraLogo from "../assets/images/sephoraLogo.png";
import LoginImage from "../assets/images/LoginImage.png";
import CartImage from "../assets/images/cart.png";
import "./Home.css";
import { MdOutlineManageAccounts } from "react-icons/md";

const Home = () => {
  const navigate = useNavigate();
  const { isLoggedIn, email } = useAuth();
  const [selectedCategory, setSelectedCategory] = useState(0);
  const [searchResults, setSearchResults] = useState(null);
    const location = useLocation();

    useEffect(() => {
        if (location.state?.searchResults) {
            setSearchResults(location.state.searchResults);
        }
    }, [location.state]);


    const showManagementButton =
    isLoggedIn &&
    (email?.endsWith("@salesman.com") || email?.endsWith("@prodman.com"));

  const handleManagementNavigation = () => {
    if (email?.endsWith("@salesman.com")) {
      navigate("/sales-manager-page");
    } else if (email?.endsWith("@prodman.com")) {
      navigate("/product-manager-page");
    }
  };

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

        <SearchBar setProducts={setSearchResults} />

        <div className="right-tools">
          {showManagementButton && (
            <div
              className="header-management-container"
              onClick={handleManagementNavigation}
            >
              <MdOutlineManageAccounts size={45} />
              <span className="login-text">Admin</span>
            </div>
          )}

          {isLoggedIn ? (
            <div
              className="header-login-container"
              onClick={() => navigate("/profile")}
            >
              <img src={LoginImage} alt="Profile" className="logologin" />
              <span className="login-text">Profile</span>
            </div>
          ) : (
            <div
              className="header-login-container"
              onClick={() => navigate("/login")}
            >
              <span className="login-text">Login / Register</span>
            </div>
          )}

          <div className="cart-container" onClick={() => navigate("/cart")}>
            <img src={CartImage} alt="Cart" className="cart-logo" />
            <span className="cart-text">Cart</span>
          </div>
        </div>
      </div>

      <div className="main-container">
        <Categories
            setSelectedCategory={(id) => {
              setSelectedCategory(id);
              setSearchResults(null);
            }}
        />
        <div className="products-container">
          <h2>Featured Products</h2>
          <Products
            selectedCategory={selectedCategory}
            searchResults={searchResults}
          />
        </div>
      </div>
    </div>
  );
};

export default Home;
