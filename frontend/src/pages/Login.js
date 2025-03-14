import React, { useState } from 'react';
import './Login.css';
import { useNavigate } from 'react-router-dom'; 

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate(); 
  
  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    
    console.log('Attempting to login with:', { email, password });
    
    try {
      console.log('Sending request to: http://localhost:8080/login');
      
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
      
      console.log('Response status:', response.status);
      console.log('Response headers:', [...response.headers.entries()]);
      
      const data = await response.json();
      console.log('Response data:', data);
      
      if (!response.ok) {
        throw new Error(data.message || 'Error occurred during login');
      }
      
      console.log('Login successful:', data);
      
      if (data.token) {
        localStorage.setItem('token', data.token);
        console.log('Token stored in localStorage');
      } else {
        console.log('No token received from server');
      }
      
      console.log('Redirecting to home page');
      navigate('/'); 
      
    } catch (error) {
      console.error('Login error details:', error);
      setError(error.message || 'Network error or server not responding');
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