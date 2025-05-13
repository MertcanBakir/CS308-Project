import React, { useState, useEffect } from "react";
import "./Categories.css";

const Categories = ({ setSelectedCategory }) => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await fetch(`${process.env.REACT_APP_API_URL}/categories`);
        if (!response.ok) {
          throw new Error("Error loading categories!");
        }
        const data = await response.json();
        setCategories([{ id: 0, name: "All" }, ...data]);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchCategories();
  }, []);

  if (loading) return <p>Loading categories...</p>;
  if (error) return <p>Error: {error}</p>;

  return (
      <div className="categories">
        <h3>Categories</h3>
        <ul>
          {categories.map((category) => (
              <li key={category.id} onClick={() => setSelectedCategory(category.id)}>
                {category.name}
              </li>
          ))}
        </ul>
      </div>
  );
};

export default Categories;