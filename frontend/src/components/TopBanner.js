import React, { useState } from "react";
import "./TopBanner.css";

const TopBanner = ({
  message = "Welcome to Sephora – Address of beauty is here 💄✨",
  showCloseButton = true,
  onClose = () => {},
}) => {
  const [isVisible, setIsVisible] = useState(true);

  const handleClose = () => {
    setIsVisible(false);
    onClose();
  };

  if (!isVisible) return null;

  return (
    <div className="top-banner">
      <div className="top-banner-content">
        <p className="top-banner-text">{message}</p>
      </div>

      {showCloseButton && (
        <button
          className="top-banner-close"
          onClick={handleClose}
          aria-label="Close banner"
        >
          ×
        </button>
      )}
    </div>
  );
};

export default TopBanner;