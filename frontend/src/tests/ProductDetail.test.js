import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import ProductDetail from "../pages/ProductDetail";

jest.mock("../context/AuthContext", () => ({
  useAuth: () => ({
    isLoggedIn: true
  })
}));

beforeEach(() => {
  global.fetch = jest.fn((url) => {
    if (url.includes("/comments/")) {
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ comments: [] }),
      });
    }
    if (url.includes("/products/")) {
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({
          id: 1,
          title: "Test Product",
          description: "Test Description",
          price: 100,
          model: "TP100",
          stock: 10,
          distributor: "Test Distributor",
          guarantee: "Yok",
        }),
      });
    }
    return Promise.reject(new Error("Unhandled fetch url"));
  });
});

const renderWithRouter = (ui) => {
  return render(<MemoryRouter>{ui}</MemoryRouter>);
};

describe("ProductDetail Page", () => {
  test("shows comments section title", async () => {
    renderWithRouter(<ProductDetail />);
    await waitFor(() => {
      expect(screen.getByRole('heading', { name: /comments/i })).toBeInTheDocument();
    });
  });

  test("displays 'No comments yet.' message initially", async () => {
    renderWithRouter(<ProductDetail />);
    await waitFor(() => {
      expect(screen.getByText(/no comments yet\./i)).toBeInTheDocument();
    });
  });
});