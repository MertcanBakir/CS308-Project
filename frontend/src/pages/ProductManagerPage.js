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
        console.error("Yorumlar çekilemedi:", response.status);
      }
    } catch (error) {
      console.error("Yorumları çekerken hata:", error);
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
        setSuccessMessage(approvedStatus ? "✅ Yorum onaylandı!" : "❌ Yorum reddedildi!");
        setTimeout(() => setSuccessMessage(""), 3000);
      } else {
        console.error("Yorum güncellenemedi:", response.status);
      }
    } catch (error) {
      console.error("Onay/Red işlemi sırasında hata:", error);
    }
  };

  const getApprovalStatusText = (approved) => {
    if (approved === null) return "⏳ Onay Bekliyor";
    if (approved === true) return "✅ Onaylandı";
    if (approved === false) return "❌ Reddedildi";
    return "Bilinmiyor";
  };

  return (
    <ManagerLayout type="product">
      <div className="manager-container">
        <h2 className="manager-title">Tüm Yorumlar</h2>

        {successMessage && <div className="success-message">{successMessage}</div>}

        {loading ? (
          <p className="loading-text">Yorumlar yükleniyor...</p>
        ) : comments.length === 0 ? (
          <p className="no-comments-text">Henüz yorum yapılmamış.</p>
        ) : (
          <div className="comments-grid">
            {comments.map((comment) => (
              <div key={comment.id} className="comment-card">
                <div className="comment-header">
                  <h3>{comment.productName}</h3>
                </div>
                <div className="comment-body">
                  <p><strong>Kullanıcı:</strong> {comment.userFullName} (ID: {comment.userId})</p>
                  <p><strong>Puan:</strong> {comment.rating} ⭐</p>
                  <p><strong>İçerik:</strong> {comment.content}</p>
                  <p><strong>Tarih:</strong> {new Date(comment.createdAt).toLocaleString()}</p>
                  <p><strong>Durum:</strong> {getApprovalStatusText(comment.approved)}</p>
                </div>

                {comment.approved === null && (
                  <div className="button-group">
                    <button
                      className="approve-button"
                      onClick={() => updateCommentApproval(comment.id, true)}
                    >
                      Onayla
                    </button>
                    <button
                      className="reject-button"
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
      </div>
    </ManagerLayout>
  );
};

export default ProductManagerPage;
