import { useState, useEffect } from "react";
import {
  Chart as ChartJS,
  LineElement,
  CategoryScale,
  LinearScale,
  PointElement,
  Tooltip,
  Legend,
  Filler,
} from "chart.js";
import { Line } from "react-chartjs-2";
import "./SalesManagerPage.css";
import { useNavigate } from "react-router-dom";
import sephoraLogo from "../assets/images/sephoraLogo.png";

ChartJS.register(LineElement, CategoryScale, LinearScale, PointElement, Tooltip, Legend, Filler);

const SalesManagerPage = () => {
  const navigate = useNavigate();
  const [showWelcome, setShowWelcome] = useState(false);
  const [orders, setOrders] = useState([]);
  const [productOptions, setProductOptions] = useState([]);
  const [selectedProductId, setSelectedProductId] = useState("");
  const [dateRange, setDateRange] = useState({ start: "", end: "" });
  const [chartData, setChartData] = useState(null);
  
  const colors = {
    primary: "#DB1F6E",
    secondary: "#FF90B8",
    accent: "#000000",
    light: "#F5F5F5",
  };

  useEffect(() => {
    const timer = setTimeout(() => setShowWelcome(true), 1000);
    const hideTimer = setTimeout(() => setShowWelcome(false), 9000);
    return () => {
      clearTimeout(timer);
      clearTimeout(hideTimer);
    };
  }, []);

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const token = localStorage.getItem("token");
        const response = await fetch("http://localhost:8080/all-order-SL", {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        });

        if (!response.ok) throw new Error("Failed to fetch orders");

        const data = await response.json();
        setOrders(data);

        const uniqueProducts = Array.from(
          new Set(data.map((item) => item.productId))
        ).map((id) => {
          const product = data.find((item) => item.productId === id);
          return { id, name: product.productName };
        });

        setProductOptions(uniqueProducts);
      } catch (err) {
        console.error("Order fetch error:", err);
      }
    };

    fetchOrders();
  }, []);

  useEffect(() => {
    if (orders.length === 0) return;

    const filtered = orders.filter((order) => {
      const orderDate = order.time.split("T")[0];
      const isInRange =
        (!dateRange.start || orderDate >= dateRange.start) &&
        (!dateRange.end || orderDate <= dateRange.end);

      const isProductMatch =
        !selectedProductId || order.productId === parseInt(selectedProductId);

      return isInRange && isProductMatch;
    });

    const groupedByDate = {};
    filtered.forEach((order) => {
      const date = order.time.split("T")[0];
      if (!groupedByDate[date]) {
        groupedByDate[date] = { revenue: 0 };
      }
      groupedByDate[date].revenue += order.price * order.quantity;
    });

    const dates = Object.keys(groupedByDate).sort();
    const revenue = dates.map((date) => groupedByDate[date].revenue);
    const cost = revenue.map((r) => r * 0.5);
    const profit = revenue.map((r, i) => r - cost[i]);

    setChartData({
      labels: dates,
      datasets: [
        {
          label: "Revenue",
          data: revenue,
          borderColor: colors.primary,
          backgroundColor: 'rgba(219, 31, 110, 0.1)',
          borderWidth: 2,
          pointBackgroundColor: colors.primary,
          pointBorderColor: '#fff',
          pointBorderWidth: 2,
          pointRadius: 4,
          pointHoverRadius: 6,
          tension: 0.4,
          fill: true,
        },
        {
          label: "Cost",
          data: cost,
          borderColor: colors.secondary,
          backgroundColor: 'rgba(255, 144, 184, 0.1)',
          borderWidth: 2,
          pointBackgroundColor: colors.secondary,
          pointBorderColor: '#fff',
          pointBorderWidth: 2,
          pointRadius: 4,
          pointHoverRadius: 6,
          tension: 0.4,
          fill: true,
        },
        {
          label: "Profit",
          data: profit,
          borderColor: colors.accent,
          backgroundColor: 'rgba(0, 0, 0, 0.05)',
          borderWidth: 2,
          pointBackgroundColor: colors.accent,
          pointBorderColor: '#fff',
          pointBorderWidth: 2,
          pointRadius: 4,
          pointHoverRadius: 6,
          tension: 0.4,
          fill: true,
        },
      ],
    });
  }, [selectedProductId, dateRange, orders]);

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      x: {
        grid: {
          display: false,
          drawBorder: false,
        },
        ticks: {
          font: {
            size: 12,
            family: "'Helvetica Neue', Arial, sans-serif",
          },
          color: '#606060',
        },
      },
      y: {
        grid: {
          color: 'rgba(0, 0, 0, 0.05)',
          drawBorder: false,
        },
        ticks: {
          font: {
            size: 12,
            family: "'Helvetica Neue', Arial, sans-serif",
          },
          color: '#606060',
          callback: (value) => {
            return '₺' + value.toLocaleString();
          },
        },
        beginAtZero: true,
      },
    },
    plugins: {
      legend: {
        position: 'top',
        align: 'end',
        labels: {
          boxWidth: 12,
          usePointStyle: true,
          pointStyle: 'circle',
          font: {
            family: "'Helvetica Neue', Arial, sans-serif",
            size: 12,
            weight: 500,
          },
          padding: 20,
        },
      },
      tooltip: {
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        titleColor: '#333',
        bodyColor: '#666',
        bodyFont: {
          size: 13,
        },
        titleFont: {
          size: 14,
          weight: 600,
        },
        padding: 12,
        boxPadding: 6,
        borderColor: 'rgba(0, 0, 0, 0.1)',
        borderWidth: 1,
        displayColors: true,
        callbacks: {
          title: (tooltipItems) => {
            const date = new Date(tooltipItems[0].label);
            return date.toLocaleDateString('en-US', { 
              year: 'numeric', 
              month: 'short', 
              day: 'numeric' 
            });
          },
          label: (context) => {
            let label = context.dataset.label || '';
            if (label) {
              label += ': ';
            }
            if (context.parsed.y !== null) {
              label += new Intl.NumberFormat('tr-TR', { 
                style: 'currency', 
                currency: 'TRY' 
              }).format(context.parsed.y);
            }
            return label;
          }
        }
      },
    },
    interaction: {
      mode: 'index',
      intersect: false,
    },
    animation: {
      duration: 1000,
      easing: 'easeOutQuart',
    },
  };



  return (
    <div className="salesman-page">

      <div className="salesman-header">
        <div className="salesman-back-button2" onClick={() =>navigate("/")}>
          <div className="salesman-arrow-left2" />
        </div>
        <h1 className="salesman-title">Sales Manager Page</h1>
        <img
          src={sephoraLogo}
          alt="Sephora Logo"
          className="logo2"
          onClick={() => navigate("/")}
        />
      </div>

      {showWelcome && <div className="salesman-welcome">Welcome, Sales Manager 👋</div>}

      <div className="salesman-content">
        <div className="sales-dashboard-header">
          <h2>Sales Performance Dashboard</h2>
        </div>

        <div className="filter-panel">
          <div className="filter-group">
            <label htmlFor="product-select">Choose a product:</label>
            <select
              id="product-select"
              value={selectedProductId}
              onChange={(e) => setSelectedProductId(e.target.value)}
              className="filter-select"
            >
              <option value="">All Products</option>
              {productOptions.map((prod) => (
                <option key={prod.id} value={prod.id}>
                  {prod.name}
                </option>
              ))}
            </select>
          </div>

          <div className="date-filters">
            <div className="date-filter-group">
              <label>Start Date:</label>
              <input
                type="date"
                value={dateRange.start}
                onChange={(e) => setDateRange({ ...dateRange, start: e.target.value })}
                className="date-input"
              />
            </div>
            <div className="date-filter-group">
              <label>End Date:</label>
              <input
                type="date"
                value={dateRange.end}
                onChange={(e) => setDateRange({ ...dateRange, end: e.target.value })}
                className="date-input"
              />
            </div>
          </div>
        </div>

        <div className="chart-card">
          <div className="chart-container">
            {chartData ? (
              <Line data={chartData} options={chartOptions} />
            ) : (
              <div className="no-data-message">
                <p>Please select filters to view performance data</p>
              </div>
            )}
          </div>
        </div>

        {chartData && (
          <div className="metrics-summary">
            <div className="metric-card revenue">
              <h3>Total Revenue</h3>
              <p className="metric-value">
                ₺{chartData.datasets[0].data.reduce((sum, val) => sum + val, 0).toLocaleString()}
              </p>
            </div>
            <div className="metric-card cost">
              <h3>Total Cost</h3>
              <p className="metric-value">
                ₺{chartData.datasets[1].data.reduce((sum, val) => sum + val, 0).toLocaleString()}
              </p>
            </div>
            <div className="metric-card profit">
              <h3>Total Profit</h3>
              <p className="metric-value">
               ₺{chartData.datasets[2].data.reduce((sum, val) => sum + val, 0).toLocaleString()}
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default SalesManagerPage;