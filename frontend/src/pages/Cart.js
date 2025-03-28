import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Cart.css";
import TrashButton from "../components/TrashButton";
import { useAuth } from "../context/AuthContext";

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
            throw new Error("Sepet verisi alınamadı.");
          }

          const data = await response.json();
          let backendProducts = data.products || [];

          const localCart = JSON.parse(localStorage.getItem("cart")) || [];

          for (const localProduct of localCart) {
            const existingProduct = backendProducts.find(
              (p) => p.id === localProduct.id
            );

            if (existingProduct) {
              // 🛠 Quantity'leri topluyoruz
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
              // Yeni ürünse backend'e ekle
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

          // Son hali tekrar çek
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
        console.error("Quantity güncelleme hatası:", err);
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

  return (
    <div className="cart-page">
      <div className="back-button">
        <a href="/" title="Ana Sayfa">
          <i className="arrow-left"></i>
        </a>
      </div>

      <div className="error">
        {loading && <p>Yükleniyor...</p>}
        {error && <p style={{ color: "red" }}>{error}</p>}
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
                  <span className="productInfos"><strong>Fiyat:</strong> {product.price.toFixed(2)}₺</span>
                  <span className="productInfos"><strong>Distribütör:</strong> {product.distributorInfo}</span>
                  <span className="productInfos"><strong>Garanti:</strong> {product.warrantyStatus ? "Var" : "Yok"}</span>

                  <span className="productInfos">
                    <strong>Adet:</strong>
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
          !loading && <p>Sepetinizde ürün yok.</p>
        )}
      </div>

      <div className="totaltablo">
        <h3>Sepet Özeti</h3>
        <div className="price-list">
          {products.map((product, index) => (
            <p key={index}>{product.name} - {product.price.toFixed(2)}₺</p>
          ))}
        </div>
        <hr />
        <p><strong>Toplam Ürün:</strong> {totalItems}</p>
        <p><strong>Toplam Fiyat:</strong> {totalPrice.toFixed(2)}₺</p>
        <button
          className="checkout-button"
          onClick={() => isLoggedIn ? navigate("/checkout") : navigate("/login")}
        >
          {isLoggedIn ? "Ödemeye Geç" : "Giriş Yap"}
        </button>
      </div>
    </div>
  );
};

export default Cart;