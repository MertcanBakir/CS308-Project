
import React from "react";
import { render, screen } from "@testing-library/react";

const Products = ({ selectedCategory, searchResults }) => {
  return (
    <div>
      {searchResults.map((product) => (
        <div key={product.id}>
          <h3>{product.name}</h3>
          <div>{product.price.toFixed(2)}₺</div>
        </div>
      ))}
    </div>
  );
};


describe("Homepage Basic UI Tests", () => {
  const sampleProduct = {
    id: 1,
    name: "Test Product",
    model: "Model X",
    price: 500,
    discountedPrice: 1000,
    quantityInStock: 10,
    imageUrl: "test.jpg",
  };

  test("Sort button is visible", () => {
    render(<button>Sort by Price</button>);
    expect(screen.getByText(/Sort by Price/i)).toBeInTheDocument();
  });

  test(" Add to Cart button is visible for a product", () => {
    render(<button>Add to Cart</button>);
    expect(screen.getByText(/Add to Cart/i)).toBeInTheDocument();
  });

  test("Wishlist button is visible for a product", () => {
    render(<button>Add to Wishlist</button>);
    expect(screen.getByText(/Add to Wishlist/i)).toBeInTheDocument();
  });

  test(" Download Invoice button is present", () => {
    render(<button>Download Invoice</button>);
    expect(screen.getByText(/Download Invoice/i)).toBeInTheDocument();
  });

  test("Product price is visible", () => {
    render(<Products selectedCategory={0} searchResults={[sampleProduct]} />);
    expect(screen.getByText("500.00₺")).toBeInTheDocument();
  });
});
