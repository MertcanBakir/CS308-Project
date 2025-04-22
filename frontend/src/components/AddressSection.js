import React, { useEffect, useState, useCallback } from "react";
import { FaTrashCan, FaPlus, FaLocationDot } from "react-icons/fa6";
import PropTypes from "prop-types";
import { toast } from "react-toastify";
import "./AddressSection.css";

const AddressSection = ({ onSelectAddress }) => {
  const [addresses, setAddresses] = useState([]);
  const [selectedAddressId, setSelectedAddressId] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [newAddress, setNewAddress] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchAddresses = useCallback(async () => {
    try {
      setIsLoading(true);
      setError(null);
      const token = localStorage.getItem("token");

      if (!token) {
        throw new Error("Authentication token not found");
      }

      const response = await fetch("http://localhost:8080/addresses", {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });

      if (!response.ok) {
        throw new Error("Failed to fetch addresses");
      }

      const data = await response.json();
      setAddresses(data.addresses || []);
    } catch (err) {
      setError("Could not retrieve addresses. Please try again.");
      console.error(err.message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAddresses();
  }, [fetchAddresses]);

  const handleAddAddress = async () => {
    if (!newAddress.trim()) return;

    try {
      const token = localStorage.getItem("token");

      const response = await fetch("http://localhost:8080/add-address", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ address: newAddress }),
      });

      if (!response.ok) {
        throw new Error("Failed to add address");
      }

      setNewAddress("");
      setShowForm(false);
      fetchAddresses();
    } catch (err) {
      toast.error("Adres eklenirken bir hata oluştu.");
      console.error(err.message);
    }
  };

  const handleDeleteAddress = async (id, event) => {
    event.stopPropagation();

    try {
      const token = localStorage.getItem("token");

      const response = await fetch("http://localhost:8080/delete-address", {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ address_id: id }),
      });

      if (!response.ok) {
        const data = await response.json();
        if (data.message?.includes("foreign key constraint")) {
          toast.error("Bu adres geçmiş siparişlerde kullanıldığı için silinemez.");
        } else {
          toast.error("Adres silinirken bir hata oluştu.");
        }
        return;
      }

      if (selectedAddressId === id) {
        setSelectedAddressId(null);
        onSelectAddress(null);
      }

      fetchAddresses();
    } catch (err) {
      toast.error("Adres silinirken beklenmeyen bir hata oluştu.");
      console.error(err.message);
    }
  };

  const handleSelectAddress = (id) => {
    const isAlreadySelected = selectedAddressId === id;
    const newSelectedId = isAlreadySelected ? null : id;

    setSelectedAddressId(newSelectedId);
    onSelectAddress(newSelectedId);
  };

  const handleCancelForm = () => {
    setShowForm(false);
    setNewAddress("");
  };

  return (
    <div className="box payment-section">
      <div className="section-header">
        <FaLocationDot className="section-icon" />
        <h2>Shipping Address</h2>
      </div>

      {error && <div className="address-error">{error}</div>}

      {isLoading ? (
        <div className="address-loading">Loading addresses...</div>
      ) : addresses.length === 0 && !showForm ? (
        <div className="no-cards-message">
          No saved addresses found. Please add a new address.
        </div>
      ) : (
        <div className="cards-container">
          {addresses.map((address) => (
            <div
              key={address.id}
              className={`card-item ${
                selectedAddressId === address.id ? "selected" : ""
              }`}
              onClick={() => handleSelectAddress(address.id)}
            >
              <div className="address-content">
                <div className="address-text">{address.address}</div>
              </div>
              <button
                className="card-delete-btn"
                onClick={(e) => handleDeleteAddress(address.id, e)}
                aria-label="Delete address"
              >
                <FaTrashCan />
              </button>
            </div>
          ))}
        </div>
      )}

      {showForm ? (
        <div className="card-form">
          <div className="form-header">
            <h3>Add New Address</h3>
          </div>
          <div className="form-group">
            <input
              type="text"
              placeholder="Enter new address"
              value={newAddress}
              onChange={(e) => setNewAddress(e.target.value)}
              className="form-input"
            />
          </div>
          <div className="form-actions">
            <button
              className="cancel-btn"
              onClick={handleCancelForm}
              disabled={isLoading}
            >
              Cancel
            </button>
            <button
              className="save-btn"
              onClick={handleAddAddress}
              disabled={!newAddress.trim() || isLoading}
            >
              Save Address
            </button>
          </div>
        </div>
      ) : (
        <button
          className="add-card-button"
          onClick={() => setShowForm(true)}
          disabled={isLoading}
        >
          <FaPlus className="add-icon" />
          <span>Add New Address</span>
        </button>
      )}
    </div>
  );
};

AddressSection.propTypes = {
  onSelectAddress: PropTypes.func.isRequired,
};

export default AddressSection;