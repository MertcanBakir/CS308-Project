import React, { useState, useEffect } from "react";
import { FaSearch } from "react-icons/fa";
import "./SearchBar.css";

const SearchBar = ({ setProducts }) => {
  const [query, setQuery] = useState("");
  const [searchResults, setSearchResults] = useState([]);

  useEffect(() => {
    if (!query.trim()) {
      setSearchResults([]);
      return;
    }

    const fetchProducts = async () => {
      try {
        const response = await fetch(`http://localhost:8080/products/search?query=${query}`);
        if (!response.ok) {
          throw new Error("Arama başarısız!");
        }

        const results = await response.json();
        setSearchResults(results);
      } catch (error) {
        console.error("Arama hatası:", error);
      }
    };

    const delaySearch = setTimeout(fetchProducts, 300);
    return () => clearTimeout(delaySearch);
  }, [query]);

  const handleSelectProduct = (product) => {
    setQuery(product.name);
    setSearchResults([]);
    setProducts([product]);
  };

  return (
    <div className="search-bar-container">
      <form className="search-bar">
        <input
          type="text"
          placeholder="Search for products, brands, categories..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <button type="button">
          <FaSearch />
        </button>
      </form>

      {searchResults.length > 0 && (
  <ul className="search-results">
    {searchResults.map((product) => (
      <li key={product.id} onClick={() => handleSelectProduct(product)} className="search-result-item">
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
