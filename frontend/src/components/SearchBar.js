import React, { useState, useEffect } from "react";
import { FaSearch } from "react-icons/fa";
import { useNavigate, useLocation } from "react-router-dom";
import "./SearchBar.css";

const SearchBar = ({ setProducts }) => {
  const [query, setQuery] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    if (!query.trim()) {
      setSearchResults([]);
      return;
    }

    const fetchProducts = async () => {
      try {
        const response = await fetch(
            `http://localhost:8080/products/search?query=${query}`
        );
        if (!response.ok) {
          throw new Error("Search failed!");
        }

        const results = await response.json();
        setSearchResults(results);
      } catch (error) {
        console.error("Search error!", error);
      }
    };

    const delaySearch = setTimeout(fetchProducts, 300);
    return () => clearTimeout(delaySearch);
  }, [query]);

  const handleSelectProduct = (product) => {
    setQuery(product.name);
    setSearchResults([]);

    if (setProducts) {
      setProducts([product]); // Home sayfası
    } else {
      navigate(`/product/${product.id}`); // ProductDetail sayfası
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!query.trim()) return;

    try {
      const response = await fetch(
          `http://localhost:8080/products/search?query=${query}`
      );
      if (!response.ok) throw new Error("Search failed!");

      const results = await response.json();
      setSearchResults([]);

      if (setProducts) {
        setProducts(results);
      } else {
        // Home sayfasına arama sonucuyla yönlendir
        navigate("/", { state: { searchResults: results } });
      }
    } catch (err) {
      console.error("Search error!", err);
    }
  };

  return (
      <div className="search-bar-container">
        <form
            className="search-bar"
            onSubmit={handleSubmit}
        >
          <input
              type="text"
              placeholder="Search for products, brands, categories..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
          />
          <button type="submit">
            <FaSearch />
          </button>
        </form>

        {searchResults.length > 0 && (
            <ul className="search-results">
              {searchResults.map((product) => (
                  <li
                      key={product.id}
                      onClick={() => handleSelectProduct(product)}
                      className="search-result-item"
                  >
                    <img src={product.imageUrl} alt={product.name} />
                    <span>{product.name}</span>
                  </li>
              ))}
            </ul>
        )}
      </div>
  );
};

export default SearchBar;
