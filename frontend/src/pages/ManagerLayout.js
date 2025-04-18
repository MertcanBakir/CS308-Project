import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import sephoraLogo from "../assets/images/sephoraLogo.png";
import "./ManagerPage.css";

const ManagerLayout = ({ type, children }) => {
  const navigate = useNavigate();
  const { fullname } = useAuth();
  const [showWelcome, setShowWelcome] = useState(false);

  const roleText = type === "product" ? "Product Manager" : "Sales Manager";

  useEffect(() => {
    const showTimer = setTimeout(() => setShowWelcome(true), 1000);
    const hideTimer = setTimeout(() => setShowWelcome(false), 9000);

    return () => {
      clearTimeout(showTimer);
      clearTimeout(hideTimer);
    };
  }, []);

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
          style={{ position: "absolute", left: "24px", marginTop: "6px" }}
          onClick={() => navigate("/")}
        >
          <div className="arrow-left2" />
        </div>

        <h1 style={{ fontSize: "26px", fontWeight: "600", color: "#333", margin: 0, marginTop: "-4px" }}>
          {roleText} Page
        </h1>

        <img
          src={sephoraLogo}
          alt="Sephora Logo"
          className="logo2"
          onClick={() => navigate("/")}
        />
      </div>

      {/* Welcome mesajı */}
      {showWelcome && (
        <div
          className="manager-welcome"
          style={{
            position: "fixed",
            bottom: "20px",
            left: "20px",
            fontSize: "16px",
            backgroundColor: "#DB1F6E",
            color: "#fff",
            padding: "10px 16px",
            borderRadius: "12px",
            boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
            opacity: 0.95,
            transition: "opacity 0.5s ease",
            zIndex: 1000
          }}
        >
          Welcome, {roleText} 👋
        </div>
      )}

      {/* Sayfa içeriği */}
      <div style={{ padding: "24px" }}>
        {children}
      </div>
    </div>
  );
};

export default ManagerLayout;
