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
    fetchProducts();
  }, [sortOrder]);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      
      // Sıralama seçeneğine göre endpoint belirle
      let url = "http://localhost:8080/products";
      if (sortOrder === "popular") {
        url = "http://localhost:8080/products/popular";
      }
      
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error("Ürünleri getirirken hata oluştu!");
      }
      
      const data = await response.json();
      
      // Fiyata göre sıralama client tarafında yap
      let sortedData = [...data];
      if (sortOrder === "asc") {
        sortedData.sort((a, b) => a.price - b.price);
      } else if (sortOrder === "desc") {
        sortedData.sort((a, b) => b.price - a.price);
      }
      
      setProducts(sortedData);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSortChange = (e) => {
    setSortOrder(e.target.value);
  };

  if (loading) return <p>Ürünler yükleniyor...</p>;
  if (error) return <p>Hata: {error}</p>;

  return (
    <>
      <div className="products-header" style={{ justifyContent: "flex-end" }}>
        <div className="sort-container">
          <label htmlFor="sort">Sırala: </label>
          <select id="sort" onChange={handleSortChange} value={sortOrder}>
            <option value="">Sıralama Seçin</option>
            <option value="asc">Artan Fiyat</option>
            <option value="desc">Azalan Fiyat</option>
            <option value="popular">Popülerliğe Göre</option>
          </select>
        </div>
      </div>

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
    </>
  );
};

export default Products;