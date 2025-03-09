import React, { useState } from "react";
import "./Categories.css";

const Categories = () => {
  const [categories, setCategories] = useState([
    "All",
    "Makeup",
    "Skincare",
    "Fragrance",
    "Hair",
    "Body",
    "Men",
    "Brands",
  ]);

  return (
    <div className="categories">
      <h3>Categories</h3>
      <ul>
        {categories.map((category, index) => (
          <li key={index}>{category}</li>
        ))}
      </ul>
    </div>
  );
};

export default Categories;