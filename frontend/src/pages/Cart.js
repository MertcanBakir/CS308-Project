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
            throw new Error("Ürünleri getirirken hata oluştu!");
          }
  
          const data = await response.json();
          let backendProducts = data.products || []; 
  
          const localCart = JSON.parse(localStorage.getItem("cart")) || [];
          if (localCart.length > 0) {
            for (const product of localCart) {
              await fetch("http://localhost:8080/add_to_cart", {
                method: "POST",
                headers: {
                  "Content-Type": "application/json",
                  Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({ product_id: product.id }),
              });
            }
            localStorage.removeItem("cart");
          }
  
          setProducts([...backendProducts, ...localCart]);
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
      setProducts((prevProducts) =>
        prevProducts.filter((product) => product.id !== productId)
      );
    } else {
      const updatedCart = products.filter((product) => product.id !== productId);
      localStorage.setItem("cart", JSON.stringify(updatedCart));
      setProducts(updatedCart);
    }
  };

  const handleQuantityChange = async (index, selectedQuantity) => {
    const updatedQuantity = Number(selectedQuantity);
  
    // Önce local state ve localStorage güncelle
    setProducts((prevProducts) => {
      const updated = [...prevProducts];
      updated[index].quantity = updatedQuantity;
  
      if (!isLoggedIn) {
        localStorage.setItem("cart", JSON.stringify(updated));
      }
  
      return updated;
    });
  
    // Giriş yapılmışsa backend'e yeni quantity'i gönder
    if (isLoggedIn) {
      try {
        const response = await fetch("http://localhost:8080/change_quantity", {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({
            product_id: products[index].id,
            quantity: updatedQuantity,
          }),
        });
  
        const data = await response.json();
        if (!response.ok || !data.message) {
          console.error("Quantity güncelleme başarısız:", data.message);
        } else {
          console.log("Quantity başarıyla güncellendi.");
        }
      } catch (error) {
        console.error("Backend'e quantity güncellerken hata:", error);
      }
    }
  };

  const totalPrice = products.reduce(
    (total, product) => total + product.price * (product.quantity || 1),
    0
  );
  
  const totalItems = products.reduce(
    (count, product) => count + (product.quantity || 1),
    0
  );

  return (
    <div className="cart-page">  
      <div className="back-button">
        <a href="/" title="Back to Home">
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
                <img src={product.imageUrl} alt="Ürün Resmi" className="productImage" />
              </div>

              <div className="productInfoWrapper">
                <div className="productName">
                  <span >{product.name}</span>
                </div>
                
                <div className="productPriceDist">
                  <span className="productInfos"><strong>Model:</strong> {product.model}</span>
                  <span className="productInfos"><strong>Price: </strong>{product.price.toFixed(2)}₺</span>
                  <span className="productInfos"><strong>Distributor:</strong>{product.distributorInfo}</span>
                  <span className="productInfos"><strong>Guarantee:</strong>
                    {product.warrantyStatus ? " Var" : " Yok"}
                  </span>
                  
                  <span className="productInfos">           
                    <strong>Adet:</strong>
                    <select
                      value={product.quantity}
                      onChange={(e) => handleQuantityChange(index, e.target.value)}
                      className="quantity-select"
                    >
                      {Array.from({ length: product.quantityInStock }, (_, i) => i + 1).map((q) => (
                        <option key={q} value={q}>
                          {q}
                        </option>
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
          !loading && <p>Sepetinizde ürün bulunmamaktadır.</p>
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
        <p><strong>Toplam Ürün:</strong> {totalItems} adet</p>
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