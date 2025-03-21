import React, { useState } from 'react';
import './Login.css';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext'; 

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth(); 

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await fetch('http://localhost:8080/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify({
          email: email,
          password: password,
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        if (response.status === 401) {
          setError('Invalid email or password. Please try again.');
        } else {
          setError(data.message || 'An unexpected error occurred. Please try again.');
        }
        return;
      }

      if (data.token) {
        login(data.token); 
        navigate('/'); 
      } else {
        setError('Login successful, but no token received.');
      }

    } catch (error) {
      setError('Network error. Please check your connection and try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="back-button">
        <a href="/" title="Back to Home">
          <i className="arrow-left"></i>
        </a>
      </div>

      <form onSubmit={handleLogin} className="login-form">
        <div className="logo-container">
          <img src="https://www.sephora.com/img/ufe/logo.svg" alt="Sephora" className="sephora-logo" />
        </div>

        <h2>Login</h2>
        {error && <div className="error-message">{error}</div>}

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
          className="login-button"
          disabled={loading}
        >
          {loading ? 'Logging in...' : 'Log In'}
        </button>
        <div className="create-account">
          <p>Don't have an account? <a href="/register">Create Account</a></p>
        </div>
      </form>
    </div>
  );
}

export default Login;