
import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import TopBanner from "../components/TopBanner";
import SearchBar from "../components/SearchBar";
import sephoraLogo from "../assets/images/sephoraLogo.png";
import LoginImage from "../assets/images/LoginImage.png";
import CartImage from "../assets/images/cart.png";
import "./ProductDetail.css";
import AddToCart from "../components/AddToCart";
import { useAuth } from "../context/AuthContext";
import Modal from "react-modal";
import {AiOutlineHeart} from "react-icons/ai";

const ProductDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isLoggedIn } = useAuth();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [comments, setComments] = useState([]);
  const [averageRating, setAverageRating] = useState(null);
  const [canComment, setCanComment] = useState(false);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newRating, setNewRating] = useState(0);
  const [newContent, setNewContent] = useState("");
  const handleSearchEnter = (query) => {
    navigate("/", { state: { searchQuery: query } });
  };


  useEffect(() => {
    const fetchProduct = async () => {
      try {
        const response = await fetch(`${process.env.REACT_APP_API_URL}/products/${id}`);
        if (!response.ok) throw new Error("Could not get product information!");
        const data = await response.json();
        setProduct(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    const fetchCommentsAndPermission = async () => {
      const commentRes = await fetch(`${process.env.REACT_APP_API_URL}/comments/product/${id}`);
      if (commentRes.ok) {
        const commentData = await commentRes.json();
        setComments(commentData.comments);

        if (commentData.comments.length > 0) {
          const total = commentData.comments.reduce((sum, c) => sum + c.rating, 0);
          setAverageRating((total / commentData.comments.length).toFixed(1));
        }
      } else {
        console.error("Comments could not be retrieved:", commentRes.status);
      }

      const token = localStorage.getItem("token");
      const permissionRes = await fetch(`${process.env.REACT_APP_API_URL}/comments/can-comment/${id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (permissionRes.ok) {
        const permissionData = await permissionRes.json();
        setCanComment(permissionData.canComment);
      } else {
        console.error("Comment permission not received:", permissionRes.status);
      }
    };




    fetchProduct();
    fetchCommentsAndPermission();
  }, [id]);




  const handleAddToWishlist = async () => {
    const token = localStorage.getItem("token");
    try {
      const response = await fetch(`${process.env.REACT_APP_API_URL}/real-wishlist/add/${id}`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) throw new Error("Failed to add to real wishlist");

      alert("Ürün wishlist'e eklendi!");
    } catch (err) {
      console.error("RealWishlist error:", err);
      alert("Wishlist'e eklenirken bir hata oluştu.");
    }
  };

  const submitComment = async () => {
    const token = localStorage.getItem("token");
    await fetch(`${process.env.REACT_APP_API_URL}/comments/add`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        productId: Number(id),
        rating: newRating,
        content: newContent,
      }),
    });

    setIsModalOpen(false);
    setNewRating(0);
    setNewContent("");
    window.location.reload();
  };

  if (loading) return <p>Item is loading...</p>;
  if (error) return <p>Error: {error}</p>;

  return (
    <div className="home">
      <TopBanner />
      <div className="tools">
        <div className="left-tools">
          <img
            src={sephoraLogo}
            alt="Sephora Logo"
            className="logo"
            onClick={() => navigate("/")}
          />
          <div className="back-button" onClick={() => navigate(-1)}>
            <i className="arrow-left"></i>
          </div>
        </div>
        <SearchBar onSearchEnter={handleSearchEnter} />
        <div className="right-tools">
          {isLoggedIn ? (
              <>
                <div
                    className="header-login-container"
                    onClick={() => navigate("/profile")}
                >
                  <img src={LoginImage} alt="Profile" className="logologin" />
                  <span className="login-text">Profile</span>
                </div>

                <div
                    className="header-login-container"
                    onClick={() => navigate("/wishlist")}
                >
                  <AiOutlineHeart size={35} />
                  <span className="login-text">Wishlist</span>
                </div>
              </>
          ) : (
              <div
                  className="header-login-container"
                  onClick={() => navigate("/login")}
              >
                <span className="login-text">Login / Register</span>
              </div>
          )}
          <div className="cart-container" onClick={() => navigate("/cart")}>
            <img src={CartImage} alt="Cart" className="cart-logo" />
            <span className="cart-text">Cart</span>
          </div>
        </div>
      </div>

      <div className="product-detail-container">
        <div className="productimage-container">
          <img src={product.imageUrl} alt={product.name} className="product-image" />
        </div>
        <div className="product-info-container">
          <h1 className="product-title">{product.name}</h1>
          <p className="product-description">{product.description}</p>
          <p><strong>Price:</strong> {product.price}₺</p>
          {averageRating && (
            <p><strong>Rating:</strong> {"⭐".repeat(Math.floor(averageRating))} ({averageRating})</p>
          )}
          <p><strong>Model:</strong> {product.model}</p>
          <p><strong>Stock:</strong> {product.quantityInStock}</p>
          <p><strong>Guarantee:</strong> {product.warrantyStatus ? "Var" : "Yok"}</p>
          <p><strong>Distributor:</strong> {product.distributorInfo}</p>

          {product.quantityInStock === 0 ? (
            <button className="out-of-stock-button" disabled>Stokta Yok</button>
          ) : (
            <AddToCart product={product} />
          )}
          <button className="wishlist-button" onClick={handleAddToWishlist}>
            ❤ Add to Wishlist
          </button>


          {canComment && (
            <button
              className="add-to-cart-button"
              onClick={() => setIsModalOpen(true)}
              style={{ marginTop: "20px" }}
            >
              Comment
            </button>
          )}

          <div className="comment-section">
            <h2>Comments</h2>
            {comments.length === 0 ? (
              <p>No comments yet.</p>
            ) : (
              comments.map((comment) => (
                <div key={comment.id} className="comment-box">
                  <p><strong>{comment.fullName}</strong> ({new Date(comment.createdAt).toLocaleString()})</p>
                  <p>{"⭐".repeat(comment.rating)} ({comment.rating})</p>
                  <p>{comment.content}</p>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      <Modal
        isOpen={isModalOpen}
        onRequestClose={() => setIsModalOpen(false)}
        className="modal"
        overlayClassName="modal-overlay"
        ariaHideApp={false}
      >
        <h2>Comment</h2>
        <div className="star-rating">
          {[1, 2, 3, 4, 5].map((star) => (
            <span
              key={star}
              className={`star ${star <= newRating ? "filled" : ""}`}
              onClick={() => setNewRating(star)}
            >
              ★
            </span>
          ))}
        </div>
        <textarea
          placeholder="Write your comment..."
          value={newContent}
          onChange={(e) => setNewContent(e.target.value)}
        />
        <button className="add-to-cart-button" onClick={submitComment}>Submit</button>
      </Modal>
    </div>
  );
};

export default ProductDetail;