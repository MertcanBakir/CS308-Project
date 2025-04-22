import React, { useEffect, useState, useCallback, useRef } from "react";
import { FaTrashCan, FaCreditCard } from "react-icons/fa6";
import { IoMdAdd } from "react-icons/io";
import { RiVisaLine, RiMastercardLine } from "react-icons/ri";
import { toast } from "react-toastify";
import "../pages/Checkout.css";

const CardSection = ({ onSelectCard }) => {
  const [cards, setCards] = useState([]);
  const [selectedCardIndex, setSelectedCardIndex] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [newCard, setNewCard] = useState({
    firstField: "",
    lastField: "",
    inputOne: "",
    inputTwo: "",
    inputThree: "",
  });

  const formRef = useRef(null);
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
      if (!res.ok) throw new Error(`API error: ${res.status}`);
      const data = await res.json();
      setCards(data.cards || []);
    } catch (err) {
      console.error("Kartlar alınamadı:", err);
    }
  }, [token]);

  useEffect(() => {
    fetchCards();
  }, [fetchCards]);

  const getCardType = (number) => {
    if (!number) return null;
    if (number.startsWith("4")) return "visa";
    if (/^5[1-5]/.test(number) || (/^2[2-7]/.test(number) &&
      number.length >= 4 &&
      parseInt(number.substring(0, 4)) >= 2221 &&
      parseInt(number.substring(0, 4)) <= 2720)) {
      return "mastercard";
    }
    return null;
  };

  const formatCardNumberInput = (value) => {
    const v = value.replace(/\s+/g, "").replace(/[^0-9]/gi, "");
    const parts = [];
    for (let i = 0; i < v.length; i += 4) {
      parts.push(v.substring(i, i + 4));
    }
    return parts.join(" ");
  };

  const formatExpiryDate = (value) => {
    const v = value.replace(/\D/g, "");
    if (v.length >= 3) return `${v.slice(0, 2)}/${v.slice(2, 4)}`;
    return v;
  };

  const validateForm = () => {
    const { firstField, lastField, inputOne, inputTwo, inputThree } = newCard;

    if (!firstField.trim()) {
      toast.error("İsim gereklidir");
      return false;
    }

    if (!lastField.trim()) {
      toast.error("Soyisim gereklidir");
      return false;
    }

    const cleanNumber = inputOne.replace(/\s+/g, "");
    if (cleanNumber.length !== 16) {
      toast.error("Kart numarası 16 haneli olmalı");
      return false;
    }

    if (inputTwo.length !== 3) {
      toast.error("CVV 3 haneli olmalı");
      return false;
    }

    if (!/^\d{2}\/\d{2}$/.test(inputThree)) {
      toast.error("Tarih formatı geçersiz (MM/YY)");
      return false;
    } else {
      const [month, year] = inputThree.split("/");
      const now = new Date();
      const currentYear = now.getFullYear() % 100;
      const currentMonth = now.getMonth() + 1;

      const mm = parseInt(month, 10);
      const yy = parseInt(year, 10);

      if (mm < 1 || mm > 12 || yy < currentYear || (yy === currentYear && mm < currentMonth)) {
        toast.error("Geçersiz tarih.");
        return false;
      }
    }

    return true;
  };

  const handleAdd = async (e) => {
    e.preventDefault();
    if (!validateForm() || isSubmitting) return;
    setIsSubmitting(true);

    try {
      const res = await fetch("http://localhost:8080/add-card", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          name: newCard.firstField,
          surname: newCard.lastField,
          cardNumber: newCard.inputOne.replace(/\s+/g, ""),
          cvv: newCard.inputTwo,
          cardExpiryDate: newCard.inputThree,
        }),
      });

      if (!res.ok) throw new Error(`API error: ${res.status}`);

      setNewCard({
        firstField: "",
        lastField: "",
        inputOne: "",
        inputTwo: "",
        inputThree: "",
      });
      setShowForm(false);
      fetchCards();
    } catch (err) {
      console.error("Kart ekleme hatası:", err);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (id, e) => {
    e.stopPropagation();
    try {
      const res = await fetch("http://localhost:8080/delete-card", {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ card_id: id }),
      });

      if (!res.ok) {
        const data = await res.json();
        if (data.message?.includes("foreign key constraint")) {
          toast.error("Bu kart geçmiş siparişlerde kullanıldığı için silinemez.");
        } else {
          toast.error("Kart silinirken bir hata oluştu.");
        }
        return;
      }

      fetchCards();
      if (selectedCardIndex !== null && cards[selectedCardIndex]?.id === id) {
        setSelectedCardIndex(null);
        onSelectCard(null);
      }
    } catch (err) {
      console.error("Kart silinirken hata:", err);
      toast.error("Kart silinirken beklenmeyen bir hata oluştu.");
    }
  };

  const handleCardSelect = (index) => {
    const newIndex = selectedCardIndex === index ? null : index;
    setSelectedCardIndex(newIndex);
    onSelectCard(newIndex !== null ? cards[index].id : null);
  };

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (formRef.current && !formRef.current.contains(e.target)) {
        setShowForm(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [showForm]);

  const handleKeyDown = (e, nextRef) => {
    if (e.key === "Enter" && nextRef) {
      e.preventDefault();
      nextRef.current.focus();
    }
  };

  return (
    <div className="box payment-section">
      <div className="section-header">
        <FaCreditCard className="section-icon" />
        <h2>Ödeme Yöntemi</h2>
      </div>

      {cards.length === 0 && !showForm ? (
        <div className="no-cards-message">Henüz kayıtlı kartınız yok</div>
      ) : (
        <div className="cards-container">
          {cards.map((card, i) => {
            const cardType = getCardType(card.cardNumber);
            return (
              <div key={i} className={`card-item ${selectedCardIndex === i ? "selected" : ""}`} onClick={() => handleCardSelect(i)}>
                <div className="card-badge">
                  {cardType === "visa" && <RiVisaLine className="card-badge-icon visa" />}
                  {cardType === "mastercard" && <RiMastercardLine className="card-badge-icon mastercard" />}
                  {!cardType && <FaCreditCard className="card-badge-icon" />}
                </div>
                <div className="card-details">
                  <div className="card-number">**** **** **** {card.cardNumber.slice(-4)}</div>
                  <div className="card-holder">{card.name} {card.surname}</div>
                </div>
                <button className="card-delete-btn" onClick={(e) => handleDelete(card.id, e)} aria-label="Kartı Sil">
                  <FaTrashCan />
                </button>
              </div>
            );
          })}
        </div>
      )}

      {!showForm && (
        <button className="add-card-button" onClick={() => {
          setShowForm(true);
          setTimeout(() => nameRef.current?.focus(), 50);
        }}>
          <IoMdAdd className="add-icon" />
          <span>Yeni Kart Ekle</span>
        </button>
      )}

      {showForm && (
        <form ref={formRef} className="card-form" onSubmit={handleAdd} autoComplete="off">
          <div className="form-header"><h3>Kart Bilgileri</h3></div>

          <div className="form-row">
            <div className="form-group">
              <label>Ad</label>
              <input
                ref={nameRef}
                value={newCard.firstField}
                onChange={(e) => setNewCard({ ...newCard, firstField: e.target.value })}
                onKeyDown={(e) => handleKeyDown(e, surnameRef)}
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label>Soyad</label>
              <input
                ref={surnameRef}
                value={newCard.lastField}
                onChange={(e) => setNewCard({ ...newCard, lastField: e.target.value })}
                onKeyDown={(e) => handleKeyDown(e, numberRef)}
                className="form-input"
              />
            </div>
          </div>

          <div className="form-group">
            <label>Numara</label>
            <input
              ref={numberRef}
              value={formatCardNumberInput(newCard.inputOne)}
              onChange={(e) => setNewCard({ ...newCard, inputOne: e.target.value })}
              onKeyDown={(e) => handleKeyDown(e, expiryRef)}
              maxLength={19}
              className="form-input"
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Son Kullanma</label>
              <input
                ref={expiryRef}
                value={newCard.inputThree}
                onChange={(e) => setNewCard({ ...newCard, inputThree: formatExpiryDate(e.target.value) })}
                onKeyDown={(e) => handleKeyDown(e, cvvRef)}
                maxLength={5}
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label>CVV</label>
              <input
                ref={cvvRef}
                value={newCard.inputTwo}
                onChange={(e) => setNewCard({ ...newCard, inputTwo: e.target.value.replace(/\D/g, "") })}
                maxLength={3}
                className="form-input"
              />
            </div>
          </div>

          <div className="form-actions">
            <button
              type="button"
              className="cancel-btn"
              onClick={() => {
                setShowForm(false);
                setNewCard({
                  firstField: "",
                  lastField: "",
                  inputOne: "",
                  inputTwo: "",
                  inputThree: "",
                });
              }}
            >
              İptal
            </button>

            <button type="submit" className="save-btn" disabled={isSubmitting}>
              {isSubmitting ? "Kaydediliyor..." : "Kartı Kaydet"}
            </button>
          </div>
        </form>
      )}
    </div>
  );
};

export default CardSection;