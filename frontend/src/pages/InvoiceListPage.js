import React, { useState } from "react";
import axios from "axios";
import "./InvoiceListPage.css";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";

const InvoiceListPage = () => {
  const { token } = useAuth();
  const [start, setStart] = useState("");
  const [end, setEnd] = useState("");
  const [invoices, setInvoices] = useState([]);
  const navigate = useNavigate();

  const fetchInvoices = async () => {
    try {
      const response = await axios.get(`${process.env.REACT_APP_API_URL}/invoices`, {
        params: { start, end },
        headers: { Authorization: `Bearer ${token}` },
      });
      setInvoices(response.data);
    } catch (err) {
      console.error("Fetch error:", err);
      alert("Could not fetch invoices.");
    }
  };

  const fetchInvoiceBlobUrl = async (orderId) => {
    const response = await fetch(`${process.env.REACT_APP_API_URL}/invoices/${orderId}/download`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
  
    if (!response.ok) {
      throw new Error("Failed to fetch invoice.");
    }
  
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    return url;
  };
  
  const handleDownloadInvoice = async (orderId) => {
    try {
      if (!orderId) {
        alert("No invoice available for this order.");
        return;
      }
  
      const url = await fetchInvoiceBlobUrl(orderId);
  
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", `invoice-${orderId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
  
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error("Error downloading invoice:", err);
      alert("Could not download invoice.");
    }
  };
  
  const handleViewInvoice = async (orderId) => {
    try {
      if (!orderId) {
        alert("No invoice available for this order.");
        return;
      }
  
      const url = await fetchInvoiceBlobUrl(orderId);
      window.open(url, "_blank");
    } catch (err) {
      console.error("Error viewing invoice:", err);
      alert("Could not open invoice.");
    }
  };

  return (
    <div className="invoice-page">
      <div className="invoice-header">
        <button className="back-button" onClick={() => navigate("/sales-manager-page")}>← </button>
        <h2>Invoices</h2>
      </div>

      <div className="invoice-filters">
        <input type="date" value={start} onChange={(e) => setStart(e.target.value)} />
        <input type="date" value={end} onChange={(e) => setEnd(e.target.value)} />
        <button className="fetch-button" onClick={fetchInvoices}>Fetch Invoices</button>
      </div>

      <div className="invoice-table-wrapper">
        <table className="invoice-table">
          <thead>
            <tr>
              <th>Order ID</th>
              <th>Customer</th>
              <th>Product</th>
              <th>Quantity</th>
              <th>Created At</th>
              <th>Address</th>
              <th>Card</th>
              <th>PDF</th>
            </tr>
          </thead>
          <tbody>
            {invoices.length > 0 ? (
              invoices.map((inv) => (
                <tr key={inv.orderId}>
                  <td>{inv.orderId}</td>
                  <td>{inv.customerName}</td>
                  <td>{inv.productName}</td>
                  <td>{inv.quantity}</td>
                  <td>{new Date(inv.createdAt).toLocaleString()}</td>
                  <td>{inv.address}</td>
                  <td>{inv.cardLast4}</td>
                  <td>
                  <button className="download-btn" onClick={() => handleDownloadInvoice(inv.orderId)}>
                      Download
                    </button>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="8" style={{ textAlign: "center", padding: "12px" }}>
                  No invoices found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default InvoiceListPage;