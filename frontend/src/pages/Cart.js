import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Cart.css";
import TrashButton from "../components/TrashButton";
import { useAuth } from "../context/AuthContext";
import sephoraLogo from "../assets/images/sephoraLogo.png";

const Cart = () => {
  const { isLoggedIn, token } = useAuth();
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const getCartProducts = async () => {
      if (isLoggedIn) {
        try {
          const response = await fetch("http://localhost:8080/cart", {
            method: "GET",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
          });

          if (!response.ok) {
            throw new Error("Could not get cart data.");
          }

          const data = await response.json();
          let backendProducts = data.products || [];

          const localCart = JSON.parse(localStorage.getItem("cart")) || [];

          for (const localProduct of localCart) {
            const existingProduct = backendProducts.find(
              (p) => p.id === localProduct.id
            );

            if (existingProduct) {
              const totalQuantity = (existingProduct.quantity || 0) + (localProduct.quantity || 0);

              await fetch("http://localhost:8080/change_quantity", {
                method: "PUT",
                headers: {
                  "Content-Type": "application/json",
                  Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                  product_id: localProduct.id,
                  quantity: totalQuantity,
                }),
              });
            } else {

              await fetch("http://localhost:8080/add_to_cart", {
                method: "POST",
                headers: {
                  "Content-Type": "application/json",
                  Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                  product_id: localProduct.id,
                  quantity: localProduct.quantity,
                }),
              });
            }
          }

          localStorage.removeItem("cart");

          const updatedResponse = await fetch("http://localhost:8080/cart", {
            method: "GET",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
          });

          const updatedData = await updatedResponse.json();
          setProducts(updatedData.products || []);
        } catch (err) {
          setError(err.message);
        } finally {
          setLoading(false);
        }
      } else {
        const localCart = JSON.parse(localStorage.getItem("cart")) || [];
        setProducts(localCart);
        setLoading(false);
      }
    };

    getCartProducts();
  }, [isLoggedIn, token]);

  const handleDeleteProduct = (productId) => {
    if (isLoggedIn) {
      setProducts((prev) => prev.filter((p) => p.id !== productId));
    } else {
      const updated = products.filter((p) => p.id !== productId);
      localStorage.setItem("cart", JSON.stringify(updated));
      setProducts(updated);
    }
  };

  const handleQuantityChange = async (index, newQty) => {
    const updatedQty = Number(newQty);

    setProducts((prev) => {
      const updated = [...prev];
      updated[index].quantity = updatedQty;
      if (!isLoggedIn) {
        localStorage.setItem("cart", JSON.stringify(updated));
      }
      return updated;
    });

    if (isLoggedIn) {
      try {
        await fetch("http://localhost:8080/change_quantity", {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({
            product_id: products[index].id,
            quantity: updatedQty,
          }),
        });
      } catch (err) {
        console.error("Quantity update error:", err);
      }
    }
  };

  const totalPrice = products.reduce(
    (total, p) => total + p.price * (p.quantity || 1),
    0
  );

  const totalItems = products.reduce(
    (count, p) => count + (p.quantity || 1),
    0
  );

  const goCheckout = () => {
    localStorage.setItem("checkoutProducts", JSON.stringify(products));
    navigate("/checkout");
  };

  return (
    <div className="cart-page">

      <div className="header-bar">
        <div onClick={() => navigate("/")} title="Home Page" className="cart-back-button2">
          <i className="arrow-left2"></i>
        </div>
        <img
            src={sephoraLogo}
            alt="Sephora Logo"
            className="logo2"
            onClick={() => navigate("/")}
        />
      </div>

      <div className="error">
        {loading && <p>Loading...</p>}
        {error && <p style={{color: "red"}}>{error}</p>}
      </div>

      <div className="productsGrid">
        {products.length > 0 ? (
          products.map((product, index) => (
            <div key={index} className="productContainer">
              <div className="productImg">
                <img src={product.imageUrl} alt={product.name} className="productImage" />
              </div>

              <div className="productInfoWrapper">
                <div className="productName">
                  <span>{product.name}</span>
                </div>

                <div className="productPriceDist">
                  <span className="productInfos"><strong>Model:</strong> {product.model}</span>
                  <span className="productInfos"><strong>Price:</strong> {product.price.toFixed(2)}₺</span>
                  <span className="productInfos"><strong>Distributor:</strong> {product.distributorInfo}</span>
                  <span className="productInfos"><strong>Guarantee:</strong> {product.warrantyStatus ? "Yes" : "No"}</span>

                  <span className="productInfos">
                    <strong>Quantity:</strong>
                    <select
                      value={product.quantity}
                      onChange={(e) => handleQuantityChange(index, e.target.value)}
                      className="quantity-select"
                    >
                      {Array.from({ length: product.quantityInStock }, (_, i) => i + 1).map((q) => (
                        <option key={q} value={q}>{q}</option>
                      ))}
                    </select>
                  </span>
                </div>

                <div className="productDesc">{product.description}</div>
              </div>

              <div className="deleteContainer">
                <TrashButton
                  productId={product.id}
                  token={token}
                  onDelete={handleDeleteProduct}
                />
              </div>
            </div>
          ))
        ) : (
          !loading && <p>There are no items in your cart.</p>
        )}
      </div>

      <div className="totaltablo">
        <h3>Cart Summary</h3>
        <div className="price-list">
          {products.map((product, index) => (
            <p key={index}> {product.name} - {product.price.toFixed(2)}₺</p>
          ))}
        </div>
        <hr />
        <p><strong>Total Items:</strong> {totalItems}</p>
        <p><strong>Total Price:</strong> {totalPrice.toFixed(2)}₺</p>
        <button
          className="checkout-button"
          disabled={isLoggedIn && products.length === 0}
          onClick={() => isLoggedIn ? goCheckout() : navigate("/login")}
        >
          {isLoggedIn ? "Proceed to Payment" : "Login"}
        </button>
      </div>
    </div>
  );
};

export default Cart;