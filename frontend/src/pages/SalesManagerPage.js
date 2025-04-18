import { useNavigate } from "react-router-dom";
import sephoraLogo from "../assets/images/sephoraLogo.png";
import "./SalesManagerPage.css";

const SalesManagerPage = () => {
  const navigate = useNavigate();

  return (
    <div className="sales-page-container">
      <div className="sales-header">
        <div
          className="sales-back-button"
          onClick={() => navigate("/")}
        >
          <div className="sales-arrow-left" />
        </div>

        <h1 className="sales-page-title">
          Sales Manager Page
        </h1>

        <img
          src={sephoraLogo}
          alt="Sephora Logo"
          className="sales-logo"
          onClick={() => navigate("/")}
        />
      </div>

      <div className="sales-main-content">
        <h2 className="sales-subtitle">Welcome to Sales Manager Dashboard</h2>
      </div>
    </div>
  );
};

export default SalesManagerPage;