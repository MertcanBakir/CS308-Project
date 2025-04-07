import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import sephoraLogo from "../assets/images/sephoraLogo.png";
import AddressSection from "../components/AddressSection";
import CardSection from "../components/CardSection";
import "./Checkout.css";

const Checkout = () => {
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [selectedAddressId, setSelectedAddressId] = useState(null);
  const [selectedCardId, setSelectedCardId] = useState(null);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    const stored = localStorage.getItem("checkoutProducts");
    if (stored) {
      setProducts(JSON.parse(stored));
    }
  }, []);

  const totalPrice = products.reduce(
    (total, p) => total + p.price * (p.quantity || 1),
    0
  );

  const totalItems = products.reduce(
    (count, p) => count + (p.quantity || 1),
    0
  );

  const handleCompletePayment = async () => {
    const token = localStorage.getItem("token");

    if (!selectedAddressId || !selectedCardId) {
      setErrorMessage("Lütfen bir adres ve kart seçin.");
      setTimeout(() => setErrorMessage(""), 2000);
      return;
    }

    try {
      setErrorMessage("");

      for (const product of products) {
        const response = await fetch("http://localhost:8080/add-order", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({
            wishlist_id: product.wishlistId,
            card_id: selectedCardId,
            address_id: selectedAddressId,
          }),
        });

        const data = await response.json();

        if (!data.success) {
          setErrorMessage(`Sipariş başarısız: ${data.message}`);
          return;
        }
      }

      alert("Tüm siparişler başarıyla oluşturuldu.");
      localStorage.removeItem("checkoutProducts");
      navigate("/");
    } catch (error) {
      console.error("Hata:", error);
      setErrorMessage("Bir hata oluştu. Lütfen tekrar deneyin.");
    }
  };

  return (
    <div className="checkout-container">
      <div className="header-bar">
        <button onClick={() => navigate("/cart")} className="back-button2">
          <i className="arrow-left2"></i>
        </button>
        <img
          src={sephoraLogo}
          alt="Sephora Logo"
          className="logo2"
          onClick={() => navigate("/")}
        />
      </div>

      <div className="checkout-page">
        <div className="checkout-content">
          <div className="boxes">
            <AddressSection onSelectAddress={setSelectedAddressId} />
            <CardSection onSelectCard={setSelectedCardId} />
          </div>

          <div className="totaltablo">
            <h3>Sepet Özeti</h3>
            <div className="price-list">
              {products.map((product, index) => (
                <p key={index}>
                  {product.name} - {product.price.toFixed(2)}₺
                </p>
              ))}
            </div>
            <hr />
            <p><strong>Toplam Ürün:</strong> {totalItems}</p>
            <p><strong>Toplam Fiyat:</strong> {totalPrice.toFixed(2)}₺</p>

            {errorMessage && (
              <div className="checkout-error-box">
                <span className="checkout-error-text">{errorMessage}</span>
              </div>
            )}

            <button
              className="checkout-button"
              onClick={handleCompletePayment}
              disabled={products.length === 0}
            >
              Ödemeyi Onayla
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Checkout;