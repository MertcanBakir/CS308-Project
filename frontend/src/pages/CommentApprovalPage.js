import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import sephoraLogo from "../assets/images/sephoraLogo.png";
import "./CommentApprovalPage.css";

const CommentApprovalPage = () => {
  const navigate = useNavigate();
  const { fullname } = useAuth();
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [successMessage, setSuccessMessage] = useState("");

  useEffect(() => {
    fetchComments();
  }, []);

  const fetchComments = async () => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch("http://localhost:8080/comments/all", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
        setComments(data);
      } else {
        console.error("Failed to fetch comments:", response.status);
      }
    } catch (error) {
      console.error("Error fetching comments:", error);
    } finally {
      setLoading(false);
    }
  };

  const updateCommentApproval = async (commentId, approvedStatus) => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`http://localhost:8080/comments/approve/${commentId}`, {
        method: "PATCH",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ approved: approvedStatus }),
      });

      if (response.ok) {
        setComments(prev =>
          prev.map(comment =>
            comment.id === commentId ? { ...comment, approved: approvedStatus } : comment
          )
        );
        setSuccessMessage(approvedStatus ? "✅ Comment approved!" : "❌ Comment rejected!");
        setTimeout(() => setSuccessMessage(""), 3000);
      } else {
        console.error("Failed to update comment:", response.status);
      }
    } catch (error) {
      console.error("Approval error:", error);
    }
  };

  const getApprovalStatusText = (approved) => {
    if (approved === null) return "⏳ Pending Approval";
    if (approved === true) return "✅ Approved";
    if (approved === false) return "❌ Rejected";
    return "Unknown";
  };

  return (
    <div style={{ fontFamily: "'Helvetica Neue', sans-serif", backgroundColor: "#fafafa", minHeight: "100vh" }}>
      <div className="manager-header" style={{
        position: "relative",
        padding: "24px 32px",
        backgroundColor: "#fff",
        borderBottom: "1px solid #e0e0e0",
        boxShadow: "0 2px 8px rgba(0,0,0,0.05)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center"
      }}>
        <div
          className="back-button2"
          style={{ position: "absolute", left: "24px", marginTop: "6px", cursor: "pointer" }}
          onClick={() => navigate("/product-manager-page")}
        >
          <div className="arrow-left2" />
        </div>

        <h1 style={{ fontSize: "26px", fontWeight: "600", color: "#333", margin: 0, marginTop: "-4px" }}>
          Comment Approval Page
        </h1>

        <img
          src={sephoraLogo}
          alt="Sephora Logo"
          className="logo2"
          onClick={() => navigate("/")}
        />
      </div>

      <div className="manager-container">
        <h2 className="manager-title">Manage Comments</h2>

        {successMessage && <div className="success-message">{successMessage}</div>}

        {loading ? (
          <p className="loading-text">Loading comments...</p>
        ) : comments.length === 0 ? (
          <p className="no-comments-text">No comments yet.</p>
        ) : (
          <div className="comments-grid">
            {comments.map((comment) => (
              <div key={comment.id} className="comment-card">
                <div className="comment-header">
                  <h3>{comment.productName}</h3>
                </div>
                <div className="comment-body">
                  <p><strong>User:</strong> {comment.userFullName}</p>
                  <p><strong>Rating:</strong> {comment.rating} ⭐</p>
                  <p><strong>Content:</strong> {comment.content}</p>
                  <p><strong>Date:</strong> {new Date(comment.createdAt).toLocaleString()}</p>
                  <p><strong>Status:</strong> {getApprovalStatusText(comment.approved)}</p>
                </div>

                {comment.approved === null && (
                  <div className="button-group">
                    <button
                      className="approve-button"
                      onClick={() => updateCommentApproval(comment.id, true)}
                    >
                      Approve
                    </button>
                    <button
                      className="reject-button"
                      onClick={() => updateCommentApproval(comment.id, false)}
                    >
                      Reject
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default CommentApprovalPage;