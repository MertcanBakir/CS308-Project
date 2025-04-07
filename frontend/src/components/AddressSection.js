import React, { useEffect, useState } from "react";
import { FaTrashCan } from "react-icons/fa6";
import { IoIosAddCircleOutline } from "react-icons/io";

const AddressSection = ({ onSelectAddress }) => {
  const [addresses, setAddresses] = useState([]);
  const [selectedAddressIndex, setSelectedAddressIndex] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [newAddress, setNewAddress] = useState("");
  const token = localStorage.getItem("token");

  useEffect(() => {
    fetchAddresses();
  }, [token]);

  const fetchAddresses = async () => {
    try {
      const res = await fetch("http://localhost:8080/addresses", {
        headers: { Authorization: `Bearer ${token}` },
      });
      const data = await res.json();
      setAddresses(data.addresses);
    } catch (err) {
      console.error("Adresler alınamadı:", err);
    }
  };

  const handleAdd = async () => {
    if (!newAddress.trim()) return;
    await fetch("http://localhost:8080/add-address", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ address: newAddress }),
    });
    setNewAddress("");
    setShowForm(false);
    fetchAddresses();
  };

  const handleDelete = async (id) => {
    await fetch("http://localhost:8080/delete-address", {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ address_id: id }),
    });
    fetchAddresses();
    setSelectedAddressIndex(null);
    onSelectAddress(null);
  };

  return (
    <div className="box address-box">
      Choose an address
      {addresses.map((a, i) => (
        <div className="address-wrapper" key={i}>
          <div
            className={`adres ${selectedAddressIndex === i ? "selected" : ""}`}
            onClick={() => {
              const newIndex = selectedAddressIndex === i ? null : i;
              setSelectedAddressIndex(newIndex);
              onSelectAddress(newIndex !== null ? a.id : null);
            }}
          >
            {a.address}
            <div
              className="trash-button-checkout"
              onClick={(e) => {
                e.stopPropagation();
                handleDelete(a.id);
              }}
            >
              <FaTrashCan />
            </div>
          </div>
        </div>
      ))}
      {showForm && (
        <div className="address-wrapper">
          <input
            type="text"
            className="modal-input"
            placeholder="Yeni adres girin..."
            value={newAddress}
            onChange={(e) => setNewAddress(e.target.value)}
          />
          <div className="modal-actions">
            <button onClick={() => setShowForm(false)}>İptal</button>
            <button onClick={handleAdd}>Kaydet</button>
          </div>
        </div>
      )}
      {!showForm && (
        <IoIosAddCircleOutline
          className="add-button"
          onClick={() => setShowForm(true)}
        />
      )}
    </div>
  );
};

export default AddressSection;