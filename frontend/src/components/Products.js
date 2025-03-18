import React, { useState, useEffect } from "react";
import "./Products.css";

const Products = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const response = await fetch("http://localhost:8080/products");
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
  }, []);

  if (loading) return <p>Ürünler yükleniyor...</p>;
  if (error) return <p>Hata: {error}</p>;

  return (
    <div className="products-grid">
      {products.map((product) => (
        <div key={product.id} className="product-card">
          <img
            src={product.imageUrl} 
            alt={product.name}
            className="product-image"
          />
          <p className="product-name">{product.name}</p>
          <p className="product-price">{product.price.toFixed(2)}₺</p>
          <button className="add-to-cart-button">Sepete Ekle</button>
        </div>
      ))}
    </div>
  );
};

export default Products;