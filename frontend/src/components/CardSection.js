import React, { useEffect, useState, useCallback, useRef } from "react";
import { FaTrashCan } from "react-icons/fa6";
import { IoIosAddCircleOutline } from "react-icons/io";

const CardSection = ({ onSelectCard }) => {
  const [cards, setCards] = useState([]);
  const [selectedCardIndex, setSelectedCardIndex] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [newCard, setNewCard] = useState({
    name: "",
    surname: "",
    cardNumber: "",
    cvv: "",
    cardExpiryDate: "",
  });

  const token = localStorage.getItem("token");

  const nameRef = useRef(null);
  const surnameRef = useRef(null);
  const numberRef = useRef(null);
  const cvvRef = useRef(null);
  const expiryRef = useRef(null);

  const fetchCards = useCallback(async () => {
    try {
      const res = await fetch("http://localhost:8080/cards", {
        headers: { Authorization: `Bearer ${token}` },
      });
      const data = await res.json();
      setCards(data.cards);
    } catch (err) {
      console.error("Failed to receive cards:", err);
    }
  }, [token]);

  useEffect(() => {
    fetchCards();
  }, [fetchCards]);

  const handleAdd = async () => {
    const { name, surname, cardNumber, cvv, cardExpiryDate } = newCard;
    if (
      !name ||
      !surname ||
      cardNumber.length !== 16 ||
      cvv.length !== 3 ||
      !/^\d{2}\/\d{2}$/.test(cardExpiryDate)
    ) {
      alert("Please fill in all fields correctly.");
      return;
    }

    await fetch("http://localhost:8080/add-card", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(newCard),
    });

    setNewCard({
      name: "",
      surname: "",
      cardNumber: "",
      cvv: "",
      cardExpiryDate: "",
    });
    setShowForm(false);
    fetchCards();
  };

  const handleDelete = async (id) => {
    await fetch("http://localhost:8080/delete-card", {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ card_id: id }),
    });
    fetchCards();
    setSelectedCardIndex(null);
    onSelectCard(null); 
  };

  const formatCardNumber = (number) => {
    if (!number || number.length < 4) return "**** **** **** ****";
    const last4 = number.slice(-4);
    return `**** **** **** ${last4}`;
  };

  return (
    <div className="box address-box">
      Choose a card
      {cards.map((card, i) => (
        <div className="address-wrapper" key={i}>
          <div
            className={`adres ${selectedCardIndex === i ? "selected" : ""}`}
            onClick={() => {
              const newIndex = selectedCardIndex === i ? null : i;
              setSelectedCardIndex(newIndex);
              onSelectCard(newIndex !== null ? card.id : null);
            }}
          >
            {formatCardNumber(card.cardNumber)} - {card.name} {card.surname}
            <div
              className="trash-button-checkout"
              onClick={(e) => {
                e.stopPropagation();
                handleDelete(card.id);
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
            ref={nameRef}
            className="modal-input"
            type="text"
            placeholder="Cardholder Name"
            value={newCard.name}
            onChange={(e) =>
              setNewCard({
                ...newCard,
                name: e.target.value.replace(/[^a-zA-ZğüşöçıİĞÜŞÖÇ\s]/g, ""),
              })
            }
            onKeyDown={(e) => e.key === "Enter" && surnameRef.current.focus()}
          />
          <input
            ref={surnameRef}
            className="modal-input"
            type="text"
            placeholder="Cardholder Surname"
            value={newCard.surname}
            onChange={(e) =>
              setNewCard({
                ...newCard,
                surname: e.target.value.replace(/[^a-zA-ZğüşöçıİĞÜŞÖÇ\s]/g, ""),
              })
            }
            onKeyDown={(e) => e.key === "Enter" && numberRef.current.focus()}
          />
          <input
            ref={numberRef}
            className="modal-input"
            type="text"
            placeholder="Card Number (16 Digits)"
            maxLength={16}
            value={newCard.cardNumber}
            onChange={(e) =>
              setNewCard({
                ...newCard,
                cardNumber: e.target.value.replace(/\D/g, ""),
              })
            }
            onKeyDown={(e) => e.key === "Enter" && cvvRef.current.focus()}
          />
          <input
            ref={cvvRef}
            className="modal-input"
            type="text"
            placeholder="CVV"
            maxLength={3}
            value={newCard.cvv}
            onChange={(e) =>
              setNewCard({ ...newCard, cvv: e.target.value.replace(/\D/g, "") })
            }
            onKeyDown={(e) => e.key === "Enter" && expiryRef.current.focus()}
          />
          <input
            ref={expiryRef}
            className="modal-input"
            type="text"
            placeholder="MM/YY"
            maxLength={5}
            value={newCard.cardExpiryDate}
            onChange={(e) => {
              let value = e.target.value.replace(/[^\d]/g, "");
              if (value.length >= 3) {
                value = value.slice(0, 2) + "/" + value.slice(2);
              }
              setNewCard({ ...newCard, cardExpiryDate: value.slice(0, 5) });
            }}
            onKeyDown={(e) => e.key === "Enter" && handleAdd()}
          />
          <div className="modal-actions">
            <button onClick={() => setShowForm(false)}>Cancel</button>
            <button onClick={handleAdd}>Save</button>
          </div>
        </div>
      )}

      {!showForm && (
        <IoIosAddCircleOutline
          className="add-button"
          onClick={() => {
            setShowForm(true);
            setTimeout(() => nameRef.current?.focus(), 50);
          }}
        />
      )}
    </div>
  );
};

export default CardSection;