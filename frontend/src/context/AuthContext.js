import { createContext, useContext, useEffect, useState } from "react";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem("token"));
  const [token, setToken] = useState(localStorage.getItem("token") || null);
  const [email, setEmail] = useState(localStorage.getItem("email") || null);
  const [address, setAddress] = useState(localStorage.getItem("address") || null);
  const [fullname, setFullname] = useState(localStorage.getItem("fullname") || null);
  const [card, setCard] = useState(localStorage.getItem("card") || null);
  const [role, setRole] = useState(localStorage.getItem("role") || null);

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
    if (storedAddress) setAddress(storedAddress);
    if (storedCard) setCard(storedCard);
    if (storedFullname) setFullname(storedFullname);
    if (storedRole) setRole(storedRole);
  }, []);

  // login: artık tek bir nesne parametresi alıyor
  const login = ({ token, email, address, fullname, card, role }) => {
    localStorage.setItem("token", token);
    localStorage.setItem("email", email);
    localStorage.setItem("address", address);
    localStorage.setItem("fullname", fullname);
    localStorage.setItem("card", card);
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
