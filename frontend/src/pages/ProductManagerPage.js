import { useEffect, useState } from "react";
import ManagerLayout from "./ManagerLayout";
import "./ProductManagerPage.css";

const ProductManagerPage = () => {
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
        console.error("Comments could not be withdrawn:", response.status);
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
          "Authorization": `Bearer ${token}`,
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
        console.error("Comments could not update:", response.status);
      }
    } catch (error) {
      console.error("Error during Approval/Rejection process:", error);
    }
  };

  const getApprovalStatusText = (approved) => {
    if (approved === null) return "⏳ Pending Approval";
    if (approved === true) return "✅ Approved";
    if (approved === false) return "❌ Rejected";
    return "Unknown";
  };

  return (
    <ManagerLayout type="product">
      <div className="manager-container">
        <h2 className="manager-title">Tüm Yorumlar</h2>

        {successMessage && <div className="success-message">{successMessage}</div>}

        {loading ? (
          <p className="loading-text">Comments loading...</p>
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
                  <p><strong>User:</strong> {comment.userFullName} (ID: {comment.userId})</p>
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
                      Confirm
                    </button>
                    <button
                      className="reject-button"
                      onClick={() => updateCommentApproval(comment.id, false)}
                    >
                      Decline
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </ManagerLayout>
  );
};

export default ProductManagerPage;
