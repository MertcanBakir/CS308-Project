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

        const productsWithRank = await Promise.all(
          sortedData.map(async (product) => {
            try {
              const commentRes = await fetch(
                `${process.env.REACT_APP_API_URL}/comments/product/${product.id}`
              );
              if (commentRes.ok) {
                const commentData = await commentRes.json();
                if (commentData.comments.length > 0) {
                  const total = commentData.comments.reduce(
                    (sum, c) => sum + c.rating,
                    0
                  );
                  product.rank = total / commentData.comments.length;
                } else {
                  product.rank = null;
                }
              } else {
                product.rank = null;
              }
            } catch {
              product.rank = null;
            }
            return product;
          })
        );

        setProducts(productsWithRank);
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

                <p
                  className="product-stock"
                  style={{
                    color:
                      product.quantityInStock && product.quantityInStock > 0
                        ? "green"
                        : "red",
                  }}
                >
                  {product.quantityInStock && product.quantityInStock > 0
                    ? "In Stock"
                    : "Out of Stock"}
                </p>

                <p className="product-rank">
                  Rank:{" "}
                  {product.rank !== undefined && product.rank !== null
                    ? product.rank.toFixed(1) + " / 5"
                    : "Not rated yet"}
                </p>

                <div className="button-container">
                  <button className="product-details-button">Product Details</button>
                </div>
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