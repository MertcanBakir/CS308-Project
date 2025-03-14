import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Register.css"; 

function Register() {
  const [fullName, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [address, setAddress] = useState("");
  const [creditCardLast4Digits, setCardNumber] = useState("");
  const navigate = useNavigate();
  
  const handleRegister = async (e) => {
    e.preventDefault();
    const userData = { fullName, email, password, address, creditCardLast4Digits };
    
    try {
      console.log('Sending request to: http://localhost:8080/register');
      
      const response = await fetch('http://localhost:8080/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify(userData),
      });
      
      if (response.ok) {
        console.log("Registration successful");
        navigate("/login");
      } else {
        console.error("Registration failed");
      }
    } catch (error) {
      console.error("Error during registration:", error);
    }
  };
  
  return (
    <div className="register-container">
      <div className="back-button">
        <a href="/" title="Ana Sayfaya Dön">
          <i className="arrow-left"></i>
        </a>
      </div>
      
      <form onSubmit={handleRegister} className="register-form">
        <div className="logo-container">
          <img src="https://www.sephora.com/img/ufe/logo.svg" alt="Sephora" className="sephora-logo" />
        </div>
        
        <h2>Register</h2>
        <input
          type="text"
          placeholder="Full Name"
          value={fullName}
          onChange={(e) => setName(e.target.value)}
          required
        />
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <input
          type="text"
          placeholder="Address"
          value={address}
          onChange={(e) => setAddress(e.target.value)}
          required
        />
        <h3>Credit Card Information (Optional)</h3>
        <input
          type="text"
          placeholder="Card Number"
          value={creditCardLast4Digits}
          onChange={(e) => setCardNumber(e.target.value)}
        />
        <button type="submit" className="register-button">
          Register
        </button>
        <p className="redirect-text">
          Already have an account?{" "}
          <span className="redirect-link" onClick={() => navigate("/login")}>
            Log in
          </span>
        </p>
      </form>
    </div>
  );
}

export default Register;