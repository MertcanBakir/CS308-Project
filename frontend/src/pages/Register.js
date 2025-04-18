import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Register.css"; 

function Register() {
  const [fullName, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [passwordStrength, setPasswordStrength] = useState("");
  const [passwordMessage, setPasswordMessage] = useState("");
  const [matchError, setMatchError] = useState("");
  const navigate = useNavigate();

  const evaluatePassword = (pwd) => {
    const hasUpper = /[A-Z]/.test(pwd);
    const hasLower = /[a-z]/.test(pwd);
    const hasNumber = /[0-9]/.test(pwd);
    const hasSymbol = /[^A-Za-z0-9]/.test(pwd);
    const lengthValid = pwd.length >= 8;

    if (!lengthValid || (!hasUpper && !hasLower && !hasNumber)) {
      setPasswordStrength("weak");
      setPasswordMessage("Password is weak");
    } else if (lengthValid && hasNumber && (hasUpper || hasLower)) {
      setPasswordStrength("medium");
      setPasswordMessage("Password acceptable");
    } 
    if (lengthValid && hasUpper && hasLower && hasNumber && hasSymbol) {
      setPasswordStrength("strong");
      setPasswordMessage("Password is strong");
    }
  };

  const handlePasswordChange = (e) => {
    const newPassword = e.target.value;
    setPassword(newPassword);
    evaluatePassword(newPassword);
    setMatchError("");
  };

  const handleConfirmPasswordChange = (e) => {
    setConfirmPassword(e.target.value);
    setMatchError("");
  };

  const handleRegister = async (e) => {
    e.preventDefault();

    if (passwordStrength === "weak") {
      alert("Please choose a stronger password.");
      return;
    }

    if (password !== confirmPassword) {
      setMatchError("Passwords do not match");
      return;
    }

    const userData = { fullName, email, password };

    try {
      const response = await fetch('http://localhost:8080/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify(userData),
      });

      if (response.ok) {
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
        <a href="/" title="Return to home page">
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
          onChange={handlePasswordChange}
          required
        />
        {password && (
          <p className={`password-strength ${passwordStrength}`}>
            {passwordMessage}
          </p>
        )}
        <input
          type="password"
          placeholder="Confirm Password"
          value={confirmPassword}
          onChange={handleConfirmPasswordChange}
          required
        />
        {matchError && (
          <p className="password-mismatch-error" style={{ color: 'red' }}>
            {matchError}
          </p>
        )}

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