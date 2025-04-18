import { createContext, useContext, useEffect, useState } from "react";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem("token"));
  const [token, setToken] = useState(localStorage.getItem("token") || null);
  const [email, setEmail] = useState(localStorage.getItem("email") || null);
  const [fullname, setFullname] = useState(localStorage.getItem("fullname") || null);
  const [role, setRole] = useState(localStorage.getItem("role") || null);

  const [address, setAddress] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem("address")) || [];
    } catch (e) {
      return [];
    }
  });

  const [card, setCard] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem("card")) || [];
    } catch (e) {
      return [];
    }
  });


  useEffect(() => {
    const storedToken = localStorage.getItem("token");
    const storedEmail = localStorage.getItem("email");
    const storedAddress = localStorage.getItem("address");
    const storedCard = localStorage.getItem("card");
    const storedFullname = localStorage.getItem("fullname");
    const storedRole = localStorage.getItem("role");

    if (storedToken) {
      setIsLoggedIn(true);
      setToken(storedToken);
    }

    if (storedEmail) setEmail(storedEmail);
    if (storedFullname) setFullname(storedFullname);
    if (storedRole) setRole(storedRole);
    if (storedCard) {
      try {
        setCard(JSON.parse(storedCard));
      } catch (e) {
        setCard([]);
      }
    }

    if (storedAddress) {
      try {
        setAddress(JSON.parse(storedAddress));
      } catch (e) {
        setAddress([]);
      }
    }

  }, []);

  // login: artık tek bir nesne parametresi alıyor
  const login = ({ token, email, address, fullname, card }) => {
    localStorage.setItem("token", token);
    localStorage.setItem("email", email);
    localStorage.setItem("address", JSON.stringify(address));
    localStorage.setItem("fullname", fullname);
    localStorage.setItem("card", JSON.stringify(card));
    localStorage.setItem("role", role);

    setIsLoggedIn(true);
    setToken(token);
    setEmail(email);
    setAddress(address);
    setFullname(fullname);
    setCard(card);
    setRole(role);
  };


  const logout = () => {
    ["token", "email", "address", "fullname", "card", "role"].forEach(key => localStorage.removeItem(key));

    setIsLoggedIn(false);
    setToken(null);
    setEmail(null);
    setAddress(null);
    setCard(null);
    setFullname(null);
    setRole(null);
    window.location.href = "/";
  };

  return (
      <AuthContext.Provider value={{ isLoggedIn, token, email, address, fullname, card, role, login, logout }}>
        {children}
      </AuthContext.Provider>
  );
};

export const useAuth = () => {
  return useContext(AuthContext);
};
