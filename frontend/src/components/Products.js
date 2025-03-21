import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./Products.css";

const Products = ({ selectedCategory }) => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        let url = "http://localhost:8080/products";
        if (selectedCategory && selectedCategory !== 0) {
          url = `http://localhost:8080/products/category/${selectedCategory}`;
        }

        const response = await fetch(url);
        if (!response.ok) {
          throw new Error("Ürünleri getirirken hata oluştu!");
        }
        const data = await response.json();
        setProducts(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, [selectedCategory]);

  if (loading) return <p>Ürünler yükleniyor...</p>;
  if (error) return <p>Hata: {error}</p>;
""
  return (
      <div className="products-grid">
        {products.map((product) => (
            <div
                key={product.id}
                className="product-card"
                onClick={() => navigate(`/product/${product.id}`)}
                style={{ cursor: "pointer" }}
            >
              <img
                  src={product.imageUrl}
                  alt={product.name}
                  className="product-image"
                  style={{ maxWidth: "150px", height: "auto" }}
              />
              <p className="product-name">{product.name}</p>
              <p className="product-price">{product.price.toFixed(2)}₺</p>
              <button className="add-to-cart-button">Ürün Detayları</button>
            </div>
        ))}
      </div>
  );
};

export default Products;
