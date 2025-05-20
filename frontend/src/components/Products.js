import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./Products.css";

const Products = ({ selectedCategory, searchResults }) => {
  const [products, setProducts] = useState([]);
  const [sortOrder, setSortOrder] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        setLoading(true);

        let url = `${process.env.REACT_APP_API_URL}/products`;

        if (selectedCategory && selectedCategory !== 0) {
          url = `${process.env.REACT_APP_API_URL}/products/category/${selectedCategory}`;
          if (sortOrder === "popular") {
            url += "?sort=popular";
          }
        } else {
          if (sortOrder === "popular") {
            url = `${process.env.REACT_APP_API_URL}/products/popular`;
          }
        }

        const response = await fetch(url);
        if (!response.ok) {
          throw new Error("An error occurred while fetching the products!");
        }

        const data = await response.json();

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

    if (searchResults === null) {
      fetchProducts();
    }
  }, [sortOrder, selectedCategory, searchResults]);

  const handleSortChange = (e) => {
    setSortOrder(e.target.value);
  };

  if (loading) return <p>Loading products...</p>;
  if (error) return <p>Error: {error}</p>;

  const productList = searchResults !== null ? searchResults : products;

  return (
    <>
      <div className="products-header" style={{ justifyContent: "flex-end" }}>
        <div className="sort-container">
          <label htmlFor="sort">Sort: </label>
          <select id="sort" onChange={handleSortChange} value={sortOrder}>
            <option value="">Select Sort</option>
            <option value="asc">Increasing Price</option>
            <option value="desc">Decreasing Price</option>
            <option value="popular">By Popularity</option>
          </select>
        </div>
      </div>

      <div className="products-grid">
        {productList.length > 0 ? (
          productList.map((product) => (
            <div
              key={product.id}
              className="product-card"
              onClick={() => navigate(`/product/${product.id}`)}
              style={{ cursor: "pointer" }}
            >
              <div className="product-image-container">
                <img
                  src={product.imageUrl}
                  alt={product.name}
                  className="product-image"
                />
              </div>
              <div className="product-info">
                <h3 className="product-name">{product.name}</h3>
                <p className="product-model">{product.model}</p>

                {product.discountedPrice && product.discountedPrice > product.price ? (
                  <div className="price-section">
                    <div className="discount-badge">
                      %{Math.round(
                        ((product.discountedPrice - product.price) / product.discountedPrice) * 100
                      )} OFF
                    </div>
                    <p className="product-price">
                      <s className="old-price">
                        {product.discountedPrice.toFixed(2)}₺
                      </s>{" "}
                      <span className="new-price">{product.price.toFixed(2)}₺</span>
                    </p>
                  </div>
                ) : (
                  <p className="product-price">{product.price.toFixed(2)}₺</p>
                )}

                {product.quantityInStock <= 0 && (
                  <span className="out-of-stock">Out of Stock</span>
                )}

                <button className="product-details-button">Product Details</button>
              </div>
            </div>
          ))
        ) : (
          <p>There are no products in this category.</p>
        )}
      </div>
    </>
  );
};

export default Products;
