import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./Products.css";

const Products = () => {
  const [products, setProducts] = useState([]);
  const [sortOrder, setSortOrder] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

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

  const handleSortChange = (e) => {
    setSortOrder(e.target.value);
  };

  const sortedProducts = [...products];
  if (sortOrder === "asc") {
    sortedProducts.sort((a, b) => a.price - b.price);
  } else if (sortOrder === "desc") {
    sortedProducts.sort((a, b) => b.price - a.price);
  }

  if (loading) return <p>Ürünler yükleniyor...</p>;
  if (error) return <p>Hata: {error}</p>;

  return (
    <>
      <div className="products-header" style={{ justifyContent: "flex-end" }}>
        <div className="sort-container">
          <label htmlFor="sort">Sırala: </label>
          <select id="sort" onChange={handleSortChange} value={sortOrder}>
            <option value="">Fiyata Göre</option>
            <option value="asc">Artan Fiyat</option>
            <option value="desc">Azalan Fiyat</option>
          </select>
        </div>
      </div>

      <div className="products-grid">
        {sortedProducts.map((product) => (
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
    </>
  );
};

export default Products;
