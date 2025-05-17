import { useNavigate } from "react-router-dom";
import { useEffect, useState, useCallback } from "react";
import { useAuth } from "../context/AuthContext";
import sephoraLogo from "../assets/images/sephoraLogo.png";
import classNames from "classnames";
import "./ProductManagerPage.css";

const STATUS_OPTIONS = Object.freeze({
  PROCESSING: "Processing",
  INTRANSIT: "In Transit",
  DELIVERED: "Delivered",
  CANCELLED: "Cancelled",
  REFUNDED: "Refunded",
});

const ProductManagerPage = () => {
  const navigate = useNavigate();
  const { fullname } = useAuth();
  const [orders, setOrders] = useState([]);
  const [comments, setComments] = useState([]);
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [isLoadingOrders, setIsLoadingOrders] = useState(true);
  const [isLoadingComments, setIsLoadingComments] = useState(true);
  const [isLoadingProducts, setIsLoadingProducts] = useState(true);
  const [isLoadingCategories, setIsLoadingCategories] = useState(true);
  const [error, setError] = useState("");
  const [showWelcome, setShowWelcome] = useState(false);
  const [activeTab, setActiveTab] = useState("orders");
  const [showAddForm, setShowAddForm] = useState(false);
  const [showAddCategoryForm, setShowAddCategoryForm] = useState(false);
  const [newProduct, setNewProduct] = useState({
    name: "",
    quantityInStock: 0,
    description: "",
    imageUrl: "",
    warrantyStatus: false,
    serialNumber: "",
    model: "",
    distributorInfo: "",
    categoryIds: []
  });
  const [newCategory, setNewCategory] = useState({
    name: ""
  });
  const [editingCategory, setEditingCategory] = useState(null);

  const fetchOrders = useCallback(async () => {
    try {
      setIsLoadingOrders(true);
      const token = localStorage.getItem("token");
      const response = await fetch("http://localhost:8080/all-order", {
        headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
      });
      if (!response.ok) throw new Error("Failed to fetch orders");
      const data = await response.json();
      setOrders(data);
    } catch (err) {
      setError("An error occurred while receiving orders.");
    } finally {
      setIsLoadingOrders(false);
    }
  }, []);

  const fetchComments = useCallback(async () => {
    try {
      setIsLoadingComments(true);
      const token = localStorage.getItem("token");
      const response = await fetch("http://localhost:8080/comments/all", {
        headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
      });
      if (!response.ok) throw new Error("Failed to fetch comments");
      const data = await response.json();
      setComments(data);
    } catch (err) {
      setError("An error occurred while receiving comments.");
    } finally {
      setIsLoadingComments(false);
    }
  }, []);

  const fetchProducts = useCallback(async () => {
    try {
      setIsLoadingProducts(true);
      const token = localStorage.getItem("token");
      const response = await fetch("http://localhost:8080/products/all", {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!response.ok) throw new Error("Failed to fetch products");
      const data = await response.json();
      setProducts(data);
    } catch (err) {
      setError("Failed to fetch products");
    } finally {
      setIsLoadingProducts(false);
    }
  }, []);

  const fetchCategories = useCallback(async () => {
    try {
      setIsLoadingCategories(true);
      const token = localStorage.getItem("token");
      const response = await fetch("http://localhost:8080/categories", {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!response.ok) throw new Error("Failed to fetch categories");
      const data = await response.json();
      setCategories(data);
    } catch (err) {
      setError("Failed to fetch categories");
    } finally {
      setIsLoadingCategories(false);
    }
  }, []);

  const downloadInvoice = async (orderId) => {
    const token = localStorage.getItem("token");
    try {
      const response = await fetch(`http://localhost:8080/admin/invoices/${orderId}/download`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) throw new Error("Failed to download invoice");

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `invoice_${orderId}.pdf`;
      document.body.appendChild(a);
      a.click();
      a.remove();
    } catch (err) {
      alert(`Invoice download failed: ${err.message}`);
    }
  };



  const handleStatusChange = async (orderId, newStatus) => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`http://localhost:8080/${orderId}/status`, {
        method: "PUT",
        headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
        body: JSON.stringify({ status: newStatus }),
      });
      if (!response.ok) throw new Error("Could not update status");
      setOrders((prev) =>
          prev.map((order) => (order.id === orderId ? { ...order, status: newStatus } : order))
      );
    } catch (err) {
      alert(`Could not update status: ${err.message}`);
    }
  };

  const updateCommentApproval = async (commentId, approvedStatus) => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`http://localhost:8080/comments/approve/${commentId}`, {
        method: "PATCH",
        headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
        body: JSON.stringify({ approved: approvedStatus }),
      });
      if (!response.ok) throw new Error("Could not update comments");
      setComments((prev) =>
          prev.map((comment) =>
              comment.id === commentId ? { ...comment, approved: approvedStatus } : comment
          )
      );
    } catch (err) {
      alert(`Could not update comments: ${err.message}`);
    }
  };

  const handleProductUpdate = async (productId, updatedName, updatedStock) => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`http://localhost:8080/products/${productId}/update-basic-info`, {
        method: "PATCH",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ name: updatedName, quantityInStock: updatedStock }),
      });

      if (!response.ok) throw new Error("Update failed");
      fetchProducts();
    } catch (err) {
      alert(`Update error: ${err.message}`);
    }
  };

  const handleAddProduct = async (e) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem("token");
      const response = await fetch("http://localhost:8080/products/add", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(newProduct),
      });

      if (!response.ok) throw new Error("Add failed");

      setShowAddForm(false);
      setNewProduct({
        name: "",
        quantityInStock: 0,
        description: "",
        imageUrl: "",
        warrantyStatus: false,
        serialNumber: "",
        model: "",
        distributorInfo: "",
        categoryIds: []
      });
      fetchProducts();
    } catch (err) {
      alert(`Add error: ${err.message}`);
    }
  };

  const handleAddCategory = async (e) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem("token");
      const response = await fetch("http://localhost:8080/categories/add", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(newCategory),
      });

      if (!response.ok) throw new Error("Add category failed");

      setShowAddCategoryForm(false);
      setNewCategory({ name: "" });
      fetchCategories();
    } catch (err) {
      alert(`Add category error: ${err.message}`);
    }
  };

  const handleUpdateCategory = async (e) => {
    e.preventDefault();
    if (!editingCategory) return;

    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`http://localhost:8080/categories/${editingCategory.id}`, {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(editingCategory),
      });

      if (!response.ok) throw new Error("Update category failed");

      setEditingCategory(null);
      fetchCategories();
    } catch (err) {
      alert(`Update category error: ${err.message}`);
    }
  };

  const handleDeleteProduct = async (productId) => {
    if (!window.confirm("Bu ürünü silmek istediğinizden emin misiniz?")) return;

    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`http://localhost:8080/products/${productId}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      if (!response.ok) throw new Error("Silinemedi");

      const responseData = await response.json();
      if (responseData.success) {
        fetchProducts();
      } else {
        throw new Error(responseData.message || "Delete failed");
      }
    } catch (err) {
      alert(`Ürün silinemedi: ${err.message}`);
    }
  };


  const handleDeleteCategory = async (categoryId) => {
    if (!window.confirm("Bu kategoriyi silmek istediğinizden emin misiniz?")) return;

    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`http://localhost:8080/categories/${categoryId}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) throw new Error("Delete category failed");

      const responseData = await response.json();
      if (responseData.success) {
        fetchCategories();
      } else {
        throw new Error(responseData.message || "Delete category failed");
      }
    } catch (err) {
      alert(`Delete category error: ${err.message}`);
    }
  };

  const handleCategoryChange = (categoryId) => {
    setNewProduct(prev => {
      const updatedCategoryIds = [...prev.categoryIds];
      const index = updatedCategoryIds.indexOf(categoryId);

      if (index === -1) {
        updatedCategoryIds.push(categoryId);
      } else {
        updatedCategoryIds.splice(index, 1);
      }

      return { ...prev, categoryIds: updatedCategoryIds };
    });
  };

  useEffect(() => {
    fetchOrders();
    fetchComments();
    fetchProducts();
    fetchCategories();
    const showTimer = setTimeout(() => setShowWelcome(true), 1000);
    const hideTimer = setTimeout(() => setShowWelcome(false), 9000);
    return () => {
      clearTimeout(showTimer);
      clearTimeout(hideTimer);
    };
  }, [fetchOrders, fetchComments, fetchProducts, fetchCategories]);

  const handleNavigateHome = () => navigate("/");

  return (
      <div className="productmanager-product-manager-page">
        <header className="productmanager-manager-header">
          <div className="productmanager-back-button2" onClick={handleNavigateHome}>
            <div className="productmanager-arrow-left2" />
          </div>
          <h1 className="productmanager-manager-title">Product Manager Page</h1>
          <img
              src={sephoraLogo}
              alt="Sephora Logo"
              className="productmanager-logo2"
              onClick={handleNavigateHome}
          />
        </header>

        {showWelcome && <div className="productmanager-manager-welcome">Welcome, Product Manager 👋</div>}

        <main className="productmanager-manager-container">
          <h2 className="productmanager-dashboard-heading">Dashboard</h2>

          {/* Tabs */}
          <div className="productmanager-tab-buttons">
            <button
                className={classNames("productmanager-tab", { active: activeTab === "orders" })}
                onClick={() => setActiveTab("orders")}
            >
              Orders
            </button>
            <button
                className={classNames("productmanager-tab", { active: activeTab === "comments" })}
                onClick={() => setActiveTab("comments")}
            >
              Comments
            </button>
            <button
                className={classNames("productmanager-tab", { active: activeTab === "products" })}
                onClick={() => setActiveTab("products")}
            >
              Products
            </button>
            <button
                className={classNames("productmanager-tab", { active: activeTab === "categories" })}
                onClick={() => setActiveTab("categories")}
            >
              Categories
            </button>
          </div>

          {/* Orders Tab */}
          {activeTab === "orders" && (
              <section className="productmanager-orders-section">
                {error && <p className="productmanager-error-message">{error}</p>}

                {isLoadingOrders ? (
                    <div className="productmanager-loading-indicator">Loading orders...</div>
                ) : orders.length === 0 ? (
                    <p className="productmanager-no-data-message">No orders found.</p>
                ) : (
                    <div className="productmanager-order-cards-wrapper">
                      {orders.map((order) => (
                          <div className="productmanager-order-card" key={order.id}>
                            <div className="productmanager-order-info">
                              <p><strong>Delivery ID:</strong> #{order.id}</p>
                              <p><strong>Customer ID:</strong> {order.userId}</p>
                              <p><strong>Product ID:</strong> {order.product?.id}</p>
                              <p><strong>Product:</strong> {order.product?.name}</p>
                              <p><strong>Quantity:</strong> {order.quantity}</p>
                              <p><strong>Total Price:</strong> {order.totalPrice}₺</p>
                              <p><strong>Address:</strong> {order.address?.address}</p>
                              <p>
                                <strong>Status:</strong>{" "}
                                <span className={`productmanager-status-pill productmanager-${order.status.toLowerCase()}`}>
                  {STATUS_OPTIONS[order.status] || order.status}
                </span>
                              </p>
                            </div>

                            <div style={{ display: "flex", flexDirection: "column", alignItems: "flex-end", gap: "10px" }}>
                              <div className="productmanager-status-change">
                                <label htmlFor={`status-${order.id}`}>Change Status:</label>
                                <select
                                    id={`status-${order.id}`}
                                    className="productmanager-status-dropdown"
                                    value={order.status}
                                    onChange={(e) => handleStatusChange(order.id, e.target.value)}
                                >
                                  {Object.keys(STATUS_OPTIONS).map((option) => (
                                      <option key={option} value={option}>
                                        {STATUS_OPTIONS[option]}
                                      </option>
                                  ))}
                                </select>
                              </div>

                              <button
                                  className="productmanager-approve-button"
                                  onClick={() => downloadInvoice(order.id)}
                                  style={{ alignSelf: "flex-start" }}
                              >
                                Download Invoice
                              </button>
                            </div>
                          </div>
                      ))}
                    </div>
                )}
              </section>
          )}


          {/* Comments Tab */}
                {activeTab === "comments" && (
                    <section className="productmanager-comments-section">
                      <h3 className="productmanager-orders-heading">Manage Comments</h3>
                      {isLoadingComments ? (
                          <div className="productmanager-loading-indicator">Loading comments...</div>
                      ) : comments.length === 0 ? (
                          <p className="productmanager-no-data-message">No comments found.</p>
                      ) : (
                          <div className="productmanager-order-cards-wrapper">
                            {comments.map((comment) => (
                                <div className="productmanager-order-card" key={comment.id}>
                            <div className="productmanager-order-info">
                              <p><strong>Ürün:</strong> {comment.productName}</p>
                              <p><strong>Kullanıcı:</strong> {comment.userFullName} (ID: {comment.userId})</p>
                              <p><strong>Rating:</strong> {comment.rating} ⭐</p>
                              <p><strong>İçerik:</strong> {comment.content}</p>
                              <p><strong>Tarih:</strong> {new Date(comment.createdAt).toLocaleString()}</p>
                              <p>
                                <strong>Durum:</strong>{" "}
                                {comment.approved === true
                                    ? "✅ Approved"
                                    : comment.approved === false
                                        ? "❌ Rejected"
                                        : "⏳ Pending"}
                              </p>
                            </div>
                            {comment.approved === null && (
                                <div className="productmanager-button-group">
                                  <button
                                      className="productmanager-approve-button"
                                      onClick={() => updateCommentApproval(comment.id, true)}
                                  >
                                    Onayla
                                  </button>
                                  <button
                                      className="productmanager-reject-button"
                                      onClick={() => updateCommentApproval(comment.id, false)}
                                  >
                                    Reddet
                                  </button>
                                </div>
                            )}
                          </div>
                      ))}
                    </div>
                )}
              </section>
          )}

          {/* Products Tab */}
          {activeTab === "products" && (
              <section className="productmanager-products-section">
                <h3 className="productmanager-orders-heading">Manage Products</h3>

                <button
                    className="productmanager-approve-button"
                    onClick={() => setShowAddForm(true)}
                    style={{ marginBottom: "16px", maxWidth: "150px" }}
                >
                  Add Product
                </button>

                {showAddForm && (
                    <form
                        className="productmanager-add-product-form"
                        onSubmit={handleAddProduct}
                    >
                      <div className="productmanager-form-row">
                        <div className="productmanager-form-group">
                          <label>
                            Name:
                            <input
                                required
                                value={newProduct.name}
                                onChange={(e) => setNewProduct({ ...newProduct, name: e.target.value })}
                            />
                          </label>
                        </div>
                        <div className="productmanager-form-group">
                          <label>
                            Stock:
                            <input
                                required
                                type="number"
                                value={newProduct.quantityInStock}
                                onChange={(e) =>
                                    setNewProduct({ ...newProduct, quantityInStock: parseInt(e.target.value) })
                                }
                            />
                          </label>
                        </div>
                      </div>

                      <div className="productmanager-form-row">
                        <div className="productmanager-form-group">
                          <label>
                            Serial Number:
                            <input
                                required
                                value={newProduct.serialNumber}
                                onChange={(e) =>
                                    setNewProduct({ ...newProduct, serialNumber: e.target.value })
                                }
                            />
                          </label>
                        </div>
                        <div className="productmanager-form-group">
                          <label>
                            Model:
                            <input
                                required
                                value={newProduct.model}
                                onChange={(e) =>
                                    setNewProduct({ ...newProduct, model: e.target.value })
                                }
                            />
                          </label>
                        </div>
                      </div>

                      <div className="productmanager-form-group">
                        <label>
                          Description:
                          <textarea
                              value={newProduct.description}
                              onChange={(e) =>
                                  setNewProduct({ ...newProduct, description: e.target.value })
                              }
                          />
                        </label>
                      </div>

                      <div className="productmanager-form-group">
                        <label>
                          Distributor Info:
                          <input
                              value={newProduct.distributorInfo}
                              onChange={(e) =>
                                  setNewProduct({ ...newProduct, distributorInfo: e.target.value })
                              }
                          />
                        </label>
                      </div>

                      <div className="productmanager-form-group">
                        <label>
                          Image URL:
                          <input
                              type="text"
                              value={newProduct.imageUrl}
                              onChange={(e) =>
                                  setNewProduct({ ...newProduct, imageUrl: e.target.value })
                              }
                          />
                        </label>
                      </div>

                      <div className="productmanager-form-group">
                        <label className="productmanager-checkbox-label">
                          <input
                              type="checkbox"
                              checked={newProduct.warrantyStatus}
                              onChange={(e) =>
                                  setNewProduct({ ...newProduct, warrantyStatus: e.target.checked })
                              }
                          />
                          Warranty Status
                        </label>
                      </div>

                      <div className="productmanager-form-group">
                        <label>Categories:</label>
                        <div className="productmanager-categories-list">
                          {isLoadingCategories ? (
                              <p>Loading categories...</p>
                          ) : (
                              categories.map((category) => (
                                  <label key={category.id} className="productmanager-category-checkbox">
                                    <input
                                        type="checkbox"
                                        checked={newProduct.categoryIds.includes(category.id)}
                                        onChange={() => handleCategoryChange(category.id)}
                                    />
                                    {category.name}
                                  </label>
                              ))
                          )}
                        </div>
                      </div>

                      <div className="productmanager-button-group">
                        <button className="productmanager-approve-button" type="submit">Submit</button>
                        <button
                            className="productmanager-reject-button"
                            type="button"
                            onClick={() => setShowAddForm(false)}
                        >
                          Cancel
                        </button>
                      </div>
                    </form>
                )}

                {isLoadingProducts ? (
                    <div className="productmanager-loading-indicator">Loading products...</div>
                ) : products.length === 0 ? (
                    <p className="productmanager-no-data-message">No products found.</p>
                ) : (
                    <div className="productmanager-order-cards-wrapper">
                      {products.map((product) => (
                          <div className="productmanager-order-card" key={product.id}>
                            <div className="productmanager-order-info productmanager-product-info-layout">
                              <div className="productmanager-product-inputs">
                                <label>
                                  Name:
                                  <input
                                      type="text"
                                      defaultValue={product.name}
                                      onBlur={(e) => product.name = e.target.value}
                                  />
                                </label>
                                <label>
                                  Stock:
                                  <input
                                      type="number"
                                      defaultValue={product.quantityInStock}
                                      onBlur={(e) => product.quantityInStock = parseInt(e.target.value)}
                                  />
                                </label>
                                <p><strong>Serial Number:</strong> {product.serialNumber}</p>
                                <p><strong>Model:</strong> {product.model}</p>
                                <p><strong>Status:</strong> {product.approved ? "Approved" : "Pending"}</p>
                              </div>
                              {product.imageUrl && (
                                  <img
                                      src={product.imageUrl}
                                      alt={product.name}
                                      className="productmanager-product-image-centered"
                                  />
                              )}
                            </div>
                            <div style={{ display: "flex", gap: "12px", }}>
                              <button
                                  className="productmanager-approve-button"
                                  onClick={() =>
                                      handleProductUpdate(product.id, product.name, product.quantityInStock)
                                  }
                              >
                                Save Changes
                              </button>
                              <button
                                  className="productmanager-delete-button"
                                  onClick={() => handleDeleteProduct(product.id)}
                              >
                                Delete
                              </button>
                            </div>
                          </div>

                      ))}
                    </div>
                )}

              </section>
          )}

          {/* Categories Tab */}
          {activeTab === "categories" && (
              <section className="productmanager-categories-section">
                <h3 className="productmanager-orders-heading">Manage Categories</h3>

                <button
                    className="productmanager-approve-button"
                    onClick={() => setShowAddCategoryForm(true)}
                    style={{ marginBottom: "16px", maxWidth: "170px" }}
                >
                  Add New Category
                </button>

                {showAddCategoryForm && (
                    <form
                        className="productmanager-add-category-form"
                        onSubmit={handleAddCategory}
                    >
                      <div className="productmanager-form-group">
                        <label>
                          Category Name:
                          <input
                              required
                              value={newCategory.name}
                              onChange={(e) => setNewCategory({ ...newCategory, name: e.target.value })}
                              placeholder="Enter category name"
                          />
                        </label>
                      </div>

                      <div className="productmanager-button-group">
                        <button className="productmanager-approve-button" type="submit">Add Category</button>
                        <button
                            className="productmanager-reject-button"
                            type="button"
                            onClick={() => setShowAddCategoryForm(false)}
                        >
                          Cancel
                        </button>
                      </div>
                    </form>
                )}

                {editingCategory && (
                    <form
                        className="productmanager-edit-category-form"
                        onSubmit={handleUpdateCategory}
                    >
                      <div className="productmanager-form-header">
                        <h4>Edit Category</h4>
                        <button
                            type="button"
                            className="productmanager-close-button"
                            onClick={() => setEditingCategory(null)}
                        >
                          ×
                        </button>
                      </div>
                      <div className="productmanager-form-group">
                        <label>
                          Category Name:
                          <input
                              required
                              value={editingCategory.name}
                              onChange={(e) => setEditingCategory({ ...editingCategory, name: e.target.value })}
                          />
                        </label>
                      </div>

                      <div className="productmanager-button-group">
                        <button className="productmanager-approve-button" type="submit">Update</button>
                        <button
                            className="productmanager-reject-button"
                            type="button"
                            onClick={() => setEditingCategory(null)}
                        >
                          Cancel
                        </button>
                      </div>
                    </form>
                )}

                {isLoadingCategories ? (
                    <div className="productmanager-loading-indicator">Loading categories...</div>
                ) : categories.length === 0 ? (
                    <p className="productmanager-no-data-message">No categories found.</p>
                ) : (
                    <div className="productmanager-categories-grid">
                      {categories.map((category) => (
                          <div className="productmanager-category-card" key={category.id}>
                            <div className="productmanager-category-info">
                              <h4>{category.name}</h4>
                              <p className="productmanager-category-id">ID: {category.id}</p>
                              {category.products && (
                                  <p className="productmanager-category-count">
                                    {category.products.length} Products
                                  </p>
                              )}
                            </div>
                            <div className="productmanager-category-actions">
                              <button
                                  className="productmanager-edit-button"
                                  onClick={() => setEditingCategory({...category})}
                              >
                                Edit
                              </button>
                              <button
                                  className="productmanager-delete-button"
                                  onClick={() => handleDeleteCategory(category.id)}
                              >
                                Delete
                              </button>
                            </div>
                          </div>
                      ))}
                    </div>
                )}
              </section>
          )}

        </main>
      </div>
  );
};

export default ProductManagerPage;