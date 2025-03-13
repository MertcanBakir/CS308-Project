import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Register.css"; 

function Register() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  
  const handleRegister = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    
    try {
      const response = await fetch('http://localhost:8080/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: name,
          email: email,
          password: password,
        }),
      });
      
      const data = await response.json();
      
      if (!response.ok) {
        throw new Error(data.message || 'Kayıt olurken bir hata oluştu');
      }
      
      // Başarılı kayıt
      console.log('Kayıt başarılı:', data);
      
      // Giriş sayfasına yönlendirme
      navigate('/login');
      
    } catch (error) {
      console.error('Kayıt hatası:', error);
      setError(error.message || 'Kayıt olurken bir hata oluştu');
    } finally {
      setLoading(false);
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
        {error && <div className="error-message">{error}</div>}
        
        <input
          type="text"
          placeholder="Full Name"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
          disabled={loading}
        />
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          disabled={loading}
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          disabled={loading}
        />
        <button 
          type="submit" 
          className="register-button"
          disabled={loading}
        >
          {loading ? 'Kaydediliyor...' : 'Register'}
        </button>
        <p className="redirect-text">
          Already have an account?{" "}
          <span 
            className="redirect-link" 
            onClick={() => navigate("/login")}
            style={{pointerEvents: loading ? 'none' : 'auto'}}
          >
            Log in
          </span>
        </p>
      </form>
    </div>
  );
}

export default Register;