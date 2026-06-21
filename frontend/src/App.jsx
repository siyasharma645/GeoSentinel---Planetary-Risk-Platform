import { useState, useEffect, useRef, useCallback } from "react";

const T = {
  bg0: "#030912", bg1: "#080F1E", bg2: "#0D1829", bg3: "#112035",
  panel: "#0A1525", border: "#1A2E45", borderGlow: "#00D4FF22",
  cyan: "#00D4FF", cyanDim: "#00D4FF55", green: "#00FF9D",
  red: "#FF3B3B", amber: "#FFB800", purple: "#9B59FF",
  text: "#C8D8E8", textDim: "#5A7A9A", textBright: "#E8F4FF",
  mono: "'JetBrains Mono', monospace", sans: "'Inter', system-ui, sans-serif",
};

const riskColor = (s) => s >= 75 ? T.red : s >= 55 ? T.amber : s >= 35 ? T.cyan : T.green;
const riskLabel = (s) => s >= 75 ? "CRITICAL" : s >= 55 ? "HIGH" : s >= 35 ? "MODERATE" : "LOW";

// ── Shared UI ─────────────────────────────────────────────────────────────────
function Panel({ children, style, glow }) {
  return (
    <div style={{
      background: T.panel, borderRadius: 8, overflow: "hidden",
      border: `1px solid ${glow ? T.cyanDim : T.border}`,
      boxShadow: glow ? `0 0 24px ${T.borderGlow}` : "none",
      ...style,
    }}>{children}</div>
  );
}

function PHdr({ sub, title, right }) {
  return (
    <div style={{
      display: "flex", justifyContent: "space-between", alignItems: "center",
      padding: "10px 16px", borderBottom: `1px solid ${T.border}`,
      background: `${T.bg2}99`,
    }}>
      <div>
        <div style={{ fontFamily: T.mono, fontSize: 9, color: T.textDim, letterSpacing: "0.12em", textTransform: "uppercase" }}>{sub}</div>
        <div style={{ fontFamily: T.sans, fontSize: 13, fontWeight: 600, color: T.textBright }}>{title}</div>
      </div>
      {right}
    </div>
  );
}

function Badge({ children, color }) {
  return (
    <span style={{
      fontFamily: T.mono, fontSize: 10, fontWeight: 700, color,
      border: `1px solid ${color}55`, background: `${color}18`,
      borderRadius: 3, padding: "2px 7px", letterSpacing: "0.07em",
    }}>{children}</span>
  );
}

function Bar({ value, max = 100 }) {
  const col = riskColor(value);
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
      <div style={{ flex: 1, height: 4, background: T.bg0, borderRadius: 2, overflow: "hidden" }}>
        <div style={{
          width: `${(value / max) * 100}%`, height: "100%",
          background: `linear-gradient(90deg, ${col}88, ${col})`,
          borderRadius: 2, boxShadow: `0 0 6px ${col}`,
          transition: "width 0.8s ease",
        }} />
      </div>
      <span style={{ fontFamily: T.mono, fontSize: 11, color: col, minWidth: 28 }}>{value}</span>
    </div>
  );
}

function Sparkline({ data, color, w = 90, h = 28 }) {
  if (!data?.length) return null;
  const mn = Math.min(...data), mx = Math.max(...data);
  const range = mx - mn || 1;
  const pts = data.map((v, i) => {
    const x = (i / (data.length - 1)) * w;
    const y = h - ((v - mn) / range) * h;
    return `${x},${y}`;
  }).join(" ");
  return (
    <svg width={w} height={h} style={{ display: "block", overflow: "visible" }}>
      <polyline points={`0,${h} ${pts} ${w},${h}`} fill={`${color}18`} stroke="none" />
      <polyline points={pts} fill="none" stroke={color} strokeWidth="1.5"
        strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

// ── Built-in Location Database ────────────────────────────────────────────────
const LOCATIONS = [
  // Countries
  { name: "Afghanistan", type: "Country", country: "Afghanistan", countryCode: "AF", lat: 33.9391, lon: 67.7100 },
  { name: "Albania", type: "Country", country: "Albania", countryCode: "AL", lat: 41.1533, lon: 20.1683 },
  { name: "Algeria", type: "Country", country: "Algeria", countryCode: "DZ", lat: 28.0339, lon: 1.6596 },
  { name: "Angola", type: "Country", country: "Angola", countryCode: "AO", lat: -11.2027, lon: 17.8739 },
  { name: "Argentina", type: "Country", country: "Argentina", countryCode: "AR", lat: -38.4161, lon: -63.6167 },
  { name: "Armenia", type: "Country", country: "Armenia", countryCode: "AM", lat: 40.0691, lon: 45.0382 },
  { name: "Australia", type: "Country", country: "Australia", countryCode: "AU", lat: -25.2744, lon: 133.7751 },
  { name: "Austria", type: "Country", country: "Austria", countryCode: "AT", lat: 47.5162, lon: 14.5501 },
  { name: "Azerbaijan", type: "Country", country: "Azerbaijan", countryCode: "AZ", lat: 40.1431, lon: 47.5769 },
  { name: "Bangladesh", type: "Country", country: "Bangladesh", countryCode: "BD", lat: 23.6850, lon: 90.3563 },
  { name: "Belarus", type: "Country", country: "Belarus", countryCode: "BY", lat: 53.7098, lon: 27.9534 },
  { name: "Belgium", type: "Country", country: "Belgium", countryCode: "BE", lat: 50.8503, lon: 4.3517 },
  { name: "Benin", type: "Country", country: "Benin", countryCode: "BJ", lat: 9.3077, lon: 2.3158 },
  { name: "Bolivia", type: "Country", country: "Bolivia", countryCode: "BO", lat: -16.2902, lon: -63.5887 },
  { name: "Bosnia and Herzegovina", type: "Country", country: "Bosnia", countryCode: "BA", lat: 43.9159, lon: 17.6791 },
  { name: "Brazil", type: "Country", country: "Brazil", countryCode: "BR", lat: -14.2350, lon: -51.9253 },
  { name: "Bulgaria", type: "Country", country: "Bulgaria", countryCode: "BG", lat: 42.7339, lon: 25.4858 },
  { name: "Burkina Faso", type: "Country", country: "Burkina Faso", countryCode: "BF", lat: 12.3641, lon: -1.5275 },
  { name: "Burma / Myanmar", type: "Country", country: "Myanmar", countryCode: "MM", lat: 21.9162, lon: 95.9560 },
  { name: "Cambodia", type: "Country", country: "Cambodia", countryCode: "KH", lat: 12.5657, lon: 104.9910 },
  { name: "Cameroon", type: "Country", country: "Cameroon", countryCode: "CM", lat: 7.3697, lon: 12.3547 },
  { name: "Canada", type: "Country", country: "Canada", countryCode: "CA", lat: 56.1304, lon: -106.3468 },
  { name: "Central African Republic", type: "Country", country: "CAR", countryCode: "CF", lat: 6.6111, lon: 20.9394 },
  { name: "Chad", type: "Country", country: "Chad", countryCode: "TD", lat: 15.4542, lon: 18.7322 },
  { name: "Chile", type: "Country", country: "Chile", countryCode: "CL", lat: -35.6751, lon: -71.5430 },
  { name: "China", type: "Country", country: "China", countryCode: "CN", lat: 35.8617, lon: 104.1954 },
  { name: "Colombia", type: "Country", country: "Colombia", countryCode: "CO", lat: 4.5709, lon: -74.2973 },
  { name: "Congo (DRC)", type: "Country", country: "DRC", countryCode: "CD", lat: -4.0383, lon: 21.7587 },
  { name: "Costa Rica", type: "Country", country: "Costa Rica", countryCode: "CR", lat: 9.7489, lon: -83.7534 },
  { name: "Croatia", type: "Country", country: "Croatia", countryCode: "HR", lat: 45.1000, lon: 15.2000 },
  { name: "Cuba", type: "Country", country: "Cuba", countryCode: "CU", lat: 21.5218, lon: -77.7812 },
  { name: "Czech Republic", type: "Country", country: "Czechia", countryCode: "CZ", lat: 49.8175, lon: 15.4730 },
  { name: "Denmark", type: "Country", country: "Denmark", countryCode: "DK", lat: 56.2639, lon: 9.5018 },
  { name: "Dominican Republic", type: "Country", country: "Dominican Republic", countryCode: "DO", lat: 18.7357, lon: -70.1627 },
  { name: "Ecuador", type: "Country", country: "Ecuador", countryCode: "EC", lat: -1.8312, lon: -78.1834 },
  { name: "Egypt", type: "Country", country: "Egypt", countryCode: "EG", lat: 26.8206, lon: 30.8025 },
  { name: "El Salvador", type: "Country", country: "El Salvador", countryCode: "SV", lat: 13.7942, lon: -88.8965 },
  { name: "Eritrea", type: "Country", country: "Eritrea", countryCode: "ER", lat: 15.1794, lon: 39.7823 },
  { name: "Ethiopia", type: "Country", country: "Ethiopia", countryCode: "ET", lat: 9.1450, lon: 40.4897 },
  { name: "Finland", type: "Country", country: "Finland", countryCode: "FI", lat: 61.9241, lon: 25.7482 },
  { name: "France", type: "Country", country: "France", countryCode: "FR", lat: 46.2276, lon: 2.2137 },
  { name: "Germany", type: "Country", country: "Germany", countryCode: "DE", lat: 51.1657, lon: 10.4515 },
  { name: "Ghana", type: "Country", country: "Ghana", countryCode: "GH", lat: 7.9465, lon: -1.0232 },
  { name: "Greece", type: "Country", country: "Greece", countryCode: "GR", lat: 39.0742, lon: 21.8243 },
  { name: "Guatemala", type: "Country", country: "Guatemala", countryCode: "GT", lat: 15.7835, lon: -90.2308 },
  { name: "Guinea", type: "Country", country: "Guinea", countryCode: "GN", lat: 9.9456, lon: -11.2420 },
  { name: "Haiti", type: "Country", country: "Haiti", countryCode: "HT", lat: 18.9712, lon: -72.2852 },
  { name: "Honduras", type: "Country", country: "Honduras", countryCode: "HN", lat: 15.1999, lon: -86.2419 },
  { name: "Hungary", type: "Country", country: "Hungary", countryCode: "HU", lat: 47.1625, lon: 19.5033 },
  { name: "India", type: "Country", country: "India", countryCode: "IN", lat: 20.5937, lon: 78.9629 },
  { name: "Indonesia", type: "Country", country: "Indonesia", countryCode: "ID", lat: -0.7893, lon: 113.9213 },
  { name: "Iran", type: "Country", country: "Iran", countryCode: "IR", lat: 32.4279, lon: 53.6880 },
  { name: "Iraq", type: "Country", country: "Iraq", countryCode: "IQ", lat: 33.2232, lon: 43.6793 },
  { name: "Israel", type: "Country", country: "Israel", countryCode: "IL", lat: 31.0461, lon: 34.8516 },
  { name: "Italy", type: "Country", country: "Italy", countryCode: "IT", lat: 41.8719, lon: 12.5674 },
  { name: "Japan", type: "Country", country: "Japan", countryCode: "JP", lat: 36.2048, lon: 138.2529 },
  { name: "Jordan", type: "Country", country: "Jordan", countryCode: "JO", lat: 30.5852, lon: 36.2384 },
  { name: "Kazakhstan", type: "Country", country: "Kazakhstan", countryCode: "KZ", lat: 48.0196, lon: 66.9237 },
  { name: "Kenya", type: "Country", country: "Kenya", countryCode: "KE", lat: -0.0236, lon: 37.9062 },
  { name: "Kuwait", type: "Country", country: "Kuwait", countryCode: "KW", lat: 29.3117, lon: 47.4818 },
  { name: "Kyrgyzstan", type: "Country", country: "Kyrgyzstan", countryCode: "KG", lat: 41.2044, lon: 74.7661 },
  { name: "Laos", type: "Country", country: "Laos", countryCode: "LA", lat: 19.8563, lon: 102.4955 },
  { name: "Lebanon", type: "Country", country: "Lebanon", countryCode: "LB", lat: 33.8547, lon: 35.8623 },
  { name: "Liberia", type: "Country", country: "Liberia", countryCode: "LR", lat: 6.4281, lon: -9.4295 },
  { name: "Libya", type: "Country", country: "Libya", countryCode: "LY", lat: 26.3351, lon: 17.2283 },
  { name: "Madagascar", type: "Country", country: "Madagascar", countryCode: "MG", lat: -18.7669, lon: 46.8691 },
  { name: "Malawi", type: "Country", country: "Malawi", countryCode: "MW", lat: -13.2543, lon: 34.3015 },
  { name: "Malaysia", type: "Country", country: "Malaysia", countryCode: "MY", lat: 4.2105, lon: 101.9758 },
  { name: "Mali", type: "Country", country: "Mali", countryCode: "ML", lat: 17.5707, lon: -3.9962 },
  { name: "Mauritania", type: "Country", country: "Mauritania", countryCode: "MR", lat: 21.0079, lon: -10.9408 },
  { name: "Mexico", type: "Country", country: "Mexico", countryCode: "MX", lat: 23.6345, lon: -102.5528 },
  { name: "Mongolia", type: "Country", country: "Mongolia", countryCode: "MN", lat: 46.8625, lon: 103.8467 },
  { name: "Morocco", type: "Country", country: "Morocco", countryCode: "MA", lat: 31.7917, lon: -7.0926 },
  { name: "Mozambique", type: "Country", country: "Mozambique", countryCode: "MZ", lat: -18.6657, lon: 35.5296 },
  { name: "Namibia", type: "Country", country: "Namibia", countryCode: "NA", lat: -22.9576, lon: 18.4904 },
  { name: "Nepal", type: "Country", country: "Nepal", countryCode: "NP", lat: 28.3949, lon: 84.1240 },
  { name: "Netherlands", type: "Country", country: "Netherlands", countryCode: "NL", lat: 52.1326, lon: 5.2913 },
  { name: "New Zealand", type: "Country", country: "New Zealand", countryCode: "NZ", lat: -40.9006, lon: 174.8860 },
  { name: "Nicaragua", type: "Country", country: "Nicaragua", countryCode: "NI", lat: 12.8654, lon: -85.2072 },
  { name: "Niger", type: "Country", country: "Niger", countryCode: "NE", lat: 17.6078, lon: 8.0817 },
  { name: "Nigeria", type: "Country", country: "Nigeria", countryCode: "NG", lat: 9.0820, lon: 8.6753 },
  { name: "North Korea", type: "Country", country: "North Korea", countryCode: "KP", lat: 40.3399, lon: 127.5101 },
  { name: "Norway", type: "Country", country: "Norway", countryCode: "NO", lat: 60.4720, lon: 8.4689 },
  { name: "Oman", type: "Country", country: "Oman", countryCode: "OM", lat: 21.5129, lon: 55.9233 },
  { name: "Pakistan", type: "Country", country: "Pakistan", countryCode: "PK", lat: 30.3753, lon: 69.3451 },
  { name: "Palestine / Gaza", type: "Region", country: "Palestine", countryCode: "PS", lat: 31.9522, lon: 35.2332 },
  { name: "Panama", type: "Country", country: "Panama", countryCode: "PA", lat: 8.5380, lon: -80.7821 },
  { name: "Papua New Guinea", type: "Country", country: "PNG", countryCode: "PG", lat: -6.3149, lon: 143.9555 },
  { name: "Paraguay", type: "Country", country: "Paraguay", countryCode: "PY", lat: -23.4425, lon: -58.4438 },
  { name: "Peru", type: "Country", country: "Peru", countryCode: "PE", lat: -9.1900, lon: -75.0152 },
  { name: "Philippines", type: "Country", country: "Philippines", countryCode: "PH", lat: 12.8797, lon: 121.7740 },
  { name: "Poland", type: "Country", country: "Poland", countryCode: "PL", lat: 51.9194, lon: 19.1451 },
  { name: "Portugal", type: "Country", country: "Portugal", countryCode: "PT", lat: 39.3999, lon: -8.2245 },
  { name: "Qatar", type: "Country", country: "Qatar", countryCode: "QA", lat: 25.3548, lon: 51.1839 },
  { name: "Romania", type: "Country", country: "Romania", countryCode: "RO", lat: 45.9432, lon: 24.9668 },
  { name: "Russia", type: "Country", country: "Russia", countryCode: "RU", lat: 61.5240, lon: 105.3188 },
  { name: "Rwanda", type: "Country", country: "Rwanda", countryCode: "RW", lat: -1.9403, lon: 29.8739 },
  { name: "Saudi Arabia", type: "Country", country: "Saudi Arabia", countryCode: "SA", lat: 23.8859, lon: 45.0792 },
  { name: "Senegal", type: "Country", country: "Senegal", countryCode: "SN", lat: 14.4974, lon: -14.4524 },
  { name: "Sierra Leone", type: "Country", country: "Sierra Leone", countryCode: "SL", lat: 8.4606, lon: -11.7799 },
  { name: "Somalia", type: "Country", country: "Somalia", countryCode: "SO", lat: 5.1521, lon: 46.1996 },
  { name: "South Africa", type: "Country", country: "South Africa", countryCode: "ZA", lat: -30.5595, lon: 22.9375 },
  { name: "South Korea", type: "Country", country: "South Korea", countryCode: "KR", lat: 35.9078, lon: 127.7669 },
  { name: "South Sudan", type: "Country", country: "South Sudan", countryCode: "SS", lat: 6.8770, lon: 31.3070 },
  { name: "Spain", type: "Country", country: "Spain", countryCode: "ES", lat: 40.4637, lon: -3.7492 },
  { name: "Sri Lanka", type: "Country", country: "Sri Lanka", countryCode: "LK", lat: 7.8731, lon: 80.7718 },
  { name: "Sudan", type: "Country", country: "Sudan", countryCode: "SD", lat: 12.8628, lon: 30.2176 },
  { name: "Syria", type: "Country", country: "Syria", countryCode: "SY", lat: 34.8021, lon: 38.9968 },
  { name: "Tajikistan", type: "Country", country: "Tajikistan", countryCode: "TJ", lat: 38.8610, lon: 71.2761 },
  { name: "Tanzania", type: "Country", country: "Tanzania", countryCode: "TZ", lat: -6.3690, lon: 34.8888 },
  { name: "Thailand", type: "Country", country: "Thailand", countryCode: "TH", lat: 15.8700, lon: 100.9925 },
  { name: "Timor-Leste", type: "Country", country: "Timor-Leste", countryCode: "TL", lat: -8.8742, lon: 125.7275 },
  { name: "Togo", type: "Country", country: "Togo", countryCode: "TG", lat: 8.6195, lon: 0.8248 },
  { name: "Tunisia", type: "Country", country: "Tunisia", countryCode: "TN", lat: 33.8869, lon: 9.5375 },
  { name: "Turkey / Türkiye", type: "Country", country: "Turkey", countryCode: "TR", lat: 38.9637, lon: 35.2433 },
  { name: "Turkmenistan", type: "Country", country: "Turkmenistan", countryCode: "TM", lat: 38.9697, lon: 59.5563 },
  { name: "Uganda", type: "Country", country: "Uganda", countryCode: "UG", lat: 1.3733, lon: 32.2903 },
  { name: "Ukraine", type: "Country", country: "Ukraine", countryCode: "UA", lat: 48.3794, lon: 31.1656 },
  { name: "United Arab Emirates", type: "Country", country: "UAE", countryCode: "AE", lat: 23.4241, lon: 53.8478 },
  { name: "United Kingdom", type: "Country", country: "UK", countryCode: "GB", lat: 55.3781, lon: -3.4360 },
  { name: "United States", type: "Country", country: "USA", countryCode: "US", lat: 37.0902, lon: -95.7129 },
  { name: "Uruguay", type: "Country", country: "Uruguay", countryCode: "UY", lat: -32.5228, lon: -55.7658 },
  { name: "Uzbekistan", type: "Country", country: "Uzbekistan", countryCode: "UZ", lat: 41.3775, lon: 64.5853 },
  { name: "Venezuela", type: "Country", country: "Venezuela", countryCode: "VE", lat: 6.4238, lon: -66.5897 },
  { name: "Vietnam", type: "Country", country: "Vietnam", countryCode: "VN", lat: 14.0583, lon: 108.2772 },
  { name: "Yemen", type: "Country", country: "Yemen", countryCode: "YE", lat: 15.5527, lon: 48.5164 },
  { name: "Zambia", type: "Country", country: "Zambia", countryCode: "ZM", lat: -13.1339, lon: 27.8493 },
  { name: "Zimbabwe", type: "Country", country: "Zimbabwe", countryCode: "ZW", lat: -19.0154, lon: 29.1549 },
  // Major Cities
  { name: "Dhaka", type: "City", country: "Bangladesh", countryCode: "BD", lat: 23.8103, lon: 90.4125 },
  { name: "Mumbai", type: "City", country: "India", countryCode: "IN", lat: 19.0760, lon: 72.8777 },
  { name: "Delhi / New Delhi", type: "City", country: "India", countryCode: "IN", lat: 28.6139, lon: 77.2090 },
  { name: "Karachi", type: "City", country: "Pakistan", countryCode: "PK", lat: 24.8607, lon: 67.0011 },
  { name: "Lahore", type: "City", country: "Pakistan", countryCode: "PK", lat: 31.5204, lon: 74.3587 },
  { name: "Kabul", type: "City", country: "Afghanistan", countryCode: "AF", lat: 34.5553, lon: 69.2075 },
  { name: "Nairobi", type: "City", country: "Kenya", countryCode: "KE", lat: -1.2921, lon: 36.8219 },
  { name: "Lagos", type: "City", country: "Nigeria", countryCode: "NG", lat: 6.5244, lon: 3.3792 },
  { name: "Cairo", type: "City", country: "Egypt", countryCode: "EG", lat: 30.0444, lon: 31.2357 },
  { name: "Mogadishu", type: "City", country: "Somalia", countryCode: "SO", lat: 2.0469, lon: 45.3182 },
  { name: "Sana'a", type: "City", country: "Yemen", countryCode: "YE", lat: 15.3694, lon: 44.1910 },
  { name: "Khartoum", type: "City", country: "Sudan", countryCode: "SD", lat: 15.5007, lon: 32.5599 },
  { name: "Jakarta", type: "City", country: "Indonesia", countryCode: "ID", lat: -6.2088, lon: 106.8456 },
  { name: "Manila", type: "City", country: "Philippines", countryCode: "PH", lat: 14.5995, lon: 120.9842 },
  { name: "Bangkok", type: "City", country: "Thailand", countryCode: "TH", lat: 13.7563, lon: 100.5018 },
  { name: "Kathmandu", type: "City", country: "Nepal", countryCode: "NP", lat: 27.7172, lon: 85.3240 },
  { name: "Colombo", type: "City", country: "Sri Lanka", countryCode: "LK", lat: 6.9271, lon: 79.8612 },
  { name: "Caracas", type: "City", country: "Venezuela", countryCode: "VE", lat: 10.4806, lon: -66.9036 },
  { name: "Port-au-Prince", type: "City", country: "Haiti", countryCode: "HT", lat: 18.5944, lon: -72.3074 },
  { name: "Beirut", type: "City", country: "Lebanon", countryCode: "LB", lat: 33.8938, lon: 35.5018 },
  { name: "Damascus", type: "City", country: "Syria", countryCode: "SY", lat: 33.5138, lon: 36.2765 },
  { name: "Baghdad", type: "City", country: "Iraq", countryCode: "IQ", lat: 33.3152, lon: 44.3661 },
  { name: "Kabul", type: "City", country: "Afghanistan", countryCode: "AF", lat: 34.5553, lon: 69.2075 },
  { name: "Los Angeles", type: "City", country: "USA", countryCode: "US", lat: 34.0522, lon: -118.2437 },
  { name: "New York", type: "City", country: "USA", countryCode: "US", lat: 40.7128, lon: -74.0060 },
  { name: "Miami", type: "City", country: "USA", countryCode: "US", lat: 25.7617, lon: -80.1918 },
  { name: "London", type: "City", country: "UK", countryCode: "GB", lat: 51.5074, lon: -0.1278 },
  { name: "Paris", type: "City", country: "France", countryCode: "FR", lat: 48.8566, lon: 2.3522 },
  { name: "Tokyo", type: "City", country: "Japan", countryCode: "JP", lat: 35.6762, lon: 139.6503 },
  { name: "Shanghai", type: "City", country: "China", countryCode: "CN", lat: 31.2304, lon: 121.4737 },
  { name: "Beijing", type: "City", country: "China", countryCode: "CN", lat: 39.9042, lon: 116.4074 },
  { name: "São Paulo", type: "City", country: "Brazil", countryCode: "BR", lat: -23.5505, lon: -46.6333 },
  { name: "Mexico City", type: "City", country: "Mexico", countryCode: "MX", lat: 19.4326, lon: -99.1332 },
  { name: "Sydney", type: "City", country: "Australia", countryCode: "AU", lat: -33.8688, lon: 151.2093 },
  { name: "Cape Town", type: "City", country: "South Africa", countryCode: "ZA", lat: -33.9249, lon: 18.4241 },
  { name: "Johannesburg", type: "City", country: "South Africa", countryCode: "ZA", lat: -26.2041, lon: 28.0473 },
  { name: "Addis Ababa", type: "City", country: "Ethiopia", countryCode: "ET", lat: 9.1450, lon: 38.7251 },
  { name: "Accra", type: "City", country: "Ghana", countryCode: "GH", lat: 5.6037, lon: -0.1870 },
  { name: "Kinshasa", type: "City", country: "DRC", countryCode: "CD", lat: -4.3317, lon: 15.3139 },
  { name: "Kabul", type: "City", country: "Afghanistan", countryCode: "AF", lat: 34.5553, lon: 69.2075 },
  // Regions & geographic areas
  { name: "Amazon Basin", type: "Region", country: "Brazil", countryCode: "BR", lat: -3.4653, lon: -62.2159 },
  { name: "Sahel Region", type: "Region", country: "Africa", countryCode: "ML", lat: 14.0, lon: 2.0 },
  { name: "Horn of Africa", type: "Region", country: "Africa", countryCode: "ET", lat: 8.0, lon: 42.0 },
  { name: "Mekong Delta", type: "Region", country: "Vietnam", countryCode: "VN", lat: 10.2, lon: 105.9 },
  { name: "Ganges Delta / Sundarbans", type: "Region", country: "Bangladesh", countryCode: "BD", lat: 21.9, lon: 89.5 },
  { name: "Indus Valley", type: "Region", country: "Pakistan", countryCode: "PK", lat: 28.0, lon: 70.0 },
  { name: "Great Barrier Reef", type: "Region", country: "Australia", countryCode: "AU", lat: -18.2, lon: 147.7 },
  { name: "Arctic Circle", type: "Region", country: "International", countryCode: "NO", lat: 71.0, lon: 25.0 },
  { name: "Himalayan Region", type: "Region", country: "Nepal/Tibet", countryCode: "NP", lat: 28.0, lon: 84.0 },
  { name: "Nile Basin", type: "Region", country: "Egypt/Sudan", countryCode: "EG", lat: 20.0, lon: 34.0 },
  { name: "California", type: "State", country: "USA", countryCode: "US", lat: 36.7783, lon: -119.4179 },
  { name: "Texas", type: "State", country: "USA", countryCode: "US", lat: 31.9686, lon: -99.9018 },
  { name: "Florida", type: "State", country: "USA", countryCode: "US", lat: 27.9944, lon: -81.7603 },
  { name: "Sindh", type: "Province", country: "Pakistan", countryCode: "PK", lat: 26.0, lon: 68.7 },
  { name: "Punjab", type: "Province", country: "Pakistan", countryCode: "PK", lat: 30.5, lon: 72.0 },
  { name: "Rajasthan", type: "State", country: "India", countryCode: "IN", lat: 27.0238, lon: 74.2179 },
  { name: "Kerala", type: "State", country: "India", countryCode: "IN", lat: 10.8505, lon: 76.2711 },
  { name: "Gaza Strip", type: "Region", country: "Palestine", countryCode: "PS", lat: 31.3547, lon: 34.3088 },
  { name: "West Bank", type: "Region", country: "Palestine", countryCode: "PS", lat: 31.9522, lon: 35.2332 },
  { name: "Donbas", type: "Region", country: "Ukraine", countryCode: "UA", lat: 48.0, lon: 37.8 },
  { name: "Tigray", type: "Region", country: "Ethiopia", countryCode: "ET", lat: 14.0, lon: 38.5 },
  { name: "Darfur", type: "Region", country: "Sudan", countryCode: "SD", lat: 13.5, lon: 24.0 },
];

function LocationSearch({ onSelect }) {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState([]);

  const handleInput = (e) => {
    const v = e.target.value;
    setQuery(v);
    if (v.length < 1) { setResults([]); return; }
    const q = v.toLowerCase();
    const matches = LOCATIONS.filter(l =>
      l.name.toLowerCase().includes(q) ||
      l.country.toLowerCase().includes(q) ||
      l.countryCode.toLowerCase() === q
    ).slice(0, 7);
    setResults(matches);
  };

  const select = (r) => {
    onSelect({ ...r, full: `${r.name}, ${r.country}` });
    setResults([]);
    setQuery(r.name);
  };

  return (
    <div style={{ position: "relative", width: "100%", maxWidth: 560 }}>
      <div style={{ position: "relative" }}>
        <span style={{
          position: "absolute", left: 14, top: "50%", transform: "translateY(-50%)",
          fontFamily: T.mono, fontSize: 14, color: T.cyanDim, pointerEvents: "none",
        }}>⊕</span>
        <input
          value={query}
          onChange={handleInput}
          placeholder="Search any country, city, or region…"
          autoFocus
          style={{
            width: "100%", padding: "14px 14px 14px 42px",
            background: T.bg2, border: `1px solid ${T.cyanDim}`,
            borderRadius: results.length > 0 ? "8px 8px 0 0" : 8,
            fontFamily: T.mono, fontSize: 13, color: T.textBright,
            outline: "none", boxShadow: `0 0 20px ${T.borderGlow}`,
          }}
        />
      </div>
      {results.length > 0 && (
        <div style={{
          position: "absolute", top: "100%", left: 0, right: 0,
          background: T.bg1, border: `1px solid ${T.cyanDim}`, borderTop: "none",
          borderRadius: "0 0 8px 8px", overflow: "hidden", zIndex: 100,
          boxShadow: `0 8px 32px #00000088`,
        }}>
          {results.map((r, i) => (
            <div key={i}
              onClick={() => select(r)}
              style={{
                padding: "10px 16px", cursor: "pointer",
                borderBottom: i < results.length - 1 ? `1px solid ${T.border}` : "none",
                display: "flex", justifyContent: "space-between", alignItems: "center",
                transition: "background 0.1s",
              }}
              onMouseEnter={e => e.currentTarget.style.background = `${T.cyan}12`}
              onMouseLeave={e => e.currentTarget.style.background = "transparent"}
            >
              <div style={{ display: "flex", flexDirection: "column", gap: 2 }}>
                <span style={{ fontFamily: T.sans, fontSize: 13, color: T.textBright }}>{r.name}</span>
                <span style={{ fontFamily: T.mono, fontSize: 9, color: T.textDim }}>{r.country}</span>
              </div>
              <span style={{
                fontFamily: T.mono, fontSize: 9, color: T.textDim,
                background: T.bg2, border: `1px solid ${T.border}`,
                borderRadius: 3, padding: "2px 6px",
              }}>{r.type}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

// ── Mini Map pin ──────────────────────────────────────────────────────────────
function MiniMap({ lat, lon, riskScore }) {
  const W = 320, H = 160;
  const col = riskColor(riskScore);

  // Center the view on the location with ~40° padding
  const zoom = 3.5;
  const toSVG = (la, lo) => {
    const x = ((lo - lon) * zoom + 180) / 360 * W;
    const y = ((lat - la) * zoom + 90) / 180 * H;
    return { x, y };
  };

  // Graticule lines centered on location
  const gridLons = Array.from({ length: 13 }, (_, i) => lon - 30 + i * 5);
  const gridLats = Array.from({ length: 7 }, (_, i) => lat - 15 + i * 5);

  const cx = W / 2, cy = H / 2;

  return (
    <svg viewBox={`0 0 ${W} ${H}`} width="100%" style={{ display: "block", borderRadius: 6 }}>
      <rect width={W} height={H} fill={T.bg0} />
      {gridLons.map(lo => {
        const x = toSVG(lat, lo).x;
        return <line key={lo} x1={x} y1={0} x2={x} y2={H}
          stroke={T.border} strokeWidth="0.5" strokeDasharray="2,6" />;
      })}
      {gridLats.map(la => {
        const y = toSVG(la, lon).y;
        return <line key={la} x1={0} y1={y} x2={W} y2={y}
          stroke={T.border} strokeWidth="0.5" strokeDasharray="2,6" />;
      })}
      {/* Cross-hair */}
      <line x1={cx - 16} y1={cy} x2={cx + 16} y2={cy} stroke={col} strokeWidth="1" strokeOpacity="0.6" />
      <line x1={cx} y1={cy - 16} x2={cx} y2={cy + 16} stroke={col} strokeWidth="1" strokeOpacity="0.6" />
      {/* Pulse rings */}
      <circle cx={cx} cy={cy} r={22} fill="none" stroke={col} strokeWidth="1" strokeOpacity="0.2"
        style={{ animation: "pulseRing 2s ease-out infinite" }} />
      <circle cx={cx} cy={cy} r={14} fill="none" stroke={col} strokeWidth="1" strokeOpacity="0.35"
        style={{ animation: "pulseRing 2s ease-out infinite 0.5s" }} />
      <circle cx={cx} cy={cy} r={6} fill={col} opacity="0.9"
        style={{ filter: `drop-shadow(0 0 6px ${col})` }} />
      {/* Coords */}
      <text x={8} y={H - 8} style={{ fontFamily: T.mono, fontSize: 8, fill: T.textDim }}>
        {lat.toFixed(4)}°{lat >= 0 ? "N" : "S"}, {lon.toFixed(4)}°{lon >= 0 ? "E" : "W"}
      </text>
    </svg>
  );
}

// ── AI Report Generator ───────────────────────────────────────────────────────
async function fetchLocationReport(location) {
  const systemPrompt = `You are GeoSentinel AI, a planetary risk intelligence system. When given a location, you generate a structured JSON risk intelligence report. 

IMPORTANT: Return ONLY valid JSON, no markdown, no explanation, no backticks.

The JSON must have this exact structure:
{
  "overallRisk": <number 0-100>,
  "riskLevel": "<CRITICAL|HIGH|MODERATE|LOW>",
  "summary": "<2-3 sentence expert summary of the location's risk profile>",
  "categories": {
    "climate": { "score": <0-100>, "label": "<status>", "detail": "<1 sentence>" },
    "disaster": { "score": <0-100>, "label": "<status>", "detail": "<1 sentence>" },
    "water": { "score": <0-100>, "label": "<status>", "detail": "<1 sentence>" },
    "food": { "score": <0-100>, "label": "<status>", "detail": "<1 sentence>" },
    "health": { "score": <0-100>, "label": "<status>", "detail": "<1 sentence>" },
    "conflict": { "score": <0-100>, "label": "<status>", "detail": "<1 sentence>" }
  },
  "activeThreats": [
    { "type": "<threat type>", "severity": "<HIGH|MEDIUM|LOW>", "description": "<detail>" }
  ],
  "keyMetrics": [
    { "label": "<metric name>", "value": "<value with unit>", "trend": "<up|down|stable>" }
  ],
  "forecast": {
    "30day": "<brief 30-day outlook>",
    "90day": "<brief 90-day outlook>",
    "probability": <escalation probability 0-100>
  },
  "recommendations": ["<action 1>", "<action 2>", "<action 3>"]
}

Use real, accurate data for well-known locations. Be geographically and scientifically precise. activeThreats should have 2-4 items. keyMetrics should have 4-6 items. recommendations should have 3 items.`;

  const response = await fetch("https://api.anthropic.com/v1/messages", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      model: "claude-sonnet-4-6",
      max_tokens: 1000,
      system: systemPrompt,
      messages: [{
        role: "user",
        content: `Generate a risk intelligence report for: ${location.name}, ${location.country} (lat: ${location.lat.toFixed(3)}, lon: ${location.lon.toFixed(3)})`,
      }],
    }),
  });
  const data = await response.json();
  const text = data.content?.[0]?.text || "{}";
  return JSON.parse(text.replace(/```json|```/g, "").trim());
}

// ── Report View ───────────────────────────────────────────────────────────────
function ReportView({ location, report }) {
  const col = riskColor(report.overallRisk);
  const catIcons = { climate: "◈", disaster: "◉", water: "◇", food: "△", health: "○", conflict: "⬡" };
  const trendArrow = (t) => t === "up" ? "↑" : t === "down" ? "↓" : "→";
  const trendColor = (t) => t === "up" ? T.red : t === "down" ? T.green : T.textDim;

  // Build mini sparklines from score (deterministic from score)
  const fakeSpark = (seed) => Array.from({ length: 12 }, (_, i) => {
    const noise = Math.sin(i * seed * 0.7) * 8 + Math.cos(i * seed) * 4;
    return Math.max(0, Math.min(100, seed + noise + i * 0.3));
  });

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
      {/* Hero */}
      <Panel glow>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 320px", minHeight: 160 }}>
          <div style={{ padding: 20 }}>
            <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 8 }}>
              <Badge color={col}>{report.riskLevel}</Badge>
              <span style={{ fontFamily: T.mono, fontSize: 9, color: T.textDim }}>
                {location.countryCode} · {location.lat.toFixed(2)}°, {location.lon.toFixed(2)}°
              </span>
            </div>
            <div style={{ fontFamily: T.sans, fontSize: 22, fontWeight: 700, color: T.textBright, marginBottom: 6 }}>
              {location.name}
            </div>
            <div style={{ fontFamily: T.sans, fontSize: 12, color: T.text, lineHeight: 1.7, maxWidth: 500, marginBottom: 14 }}>
              {report.summary}
            </div>
            <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
              <div>
                <div style={{ fontFamily: T.mono, fontSize: 42, fontWeight: 800, color: col, lineHeight: 1 }}>
                  {report.overallRisk}
                </div>
                <div style={{ fontFamily: T.mono, fontSize: 9, color: T.textDim, letterSpacing: "0.1em" }}>GLOBAL RISK INDEX</div>
              </div>
              <div style={{ flex: 1, maxWidth: 200 }}>
                <Sparkline data={fakeSpark(report.overallRisk)} color={col} w={180} h={40} />
              </div>
            </div>
          </div>
          <div style={{ borderLeft: `1px solid ${T.border}`, padding: 0, overflow: "hidden" }}>
            <MiniMap lat={location.lat} lon={location.lon} riskScore={report.overallRisk} />
          </div>
        </div>
      </Panel>

      {/* 6 Risk Categories */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 10 }}>
        {Object.entries(report.categories).map(([key, cat]) => {
          const c = riskColor(cat.score);
          return (
            <Panel key={key} style={{ padding: 14 }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 10 }}>
                <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
                  <span style={{ fontSize: 14, color: c }}>{catIcons[key]}</span>
                  <span style={{ fontFamily: T.mono, fontSize: 9, color: T.textDim, textTransform: "uppercase", letterSpacing: "0.1em" }}>
                    {key}
                  </span>
                </div>
                <Badge color={c}>{cat.label}</Badge>
              </div>
              <div style={{ fontFamily: T.mono, fontSize: 26, fontWeight: 800, color: c, marginBottom: 6 }}>{cat.score}</div>
              <Bar value={cat.score} />
              <div style={{ fontFamily: T.sans, fontSize: 11, color: T.textDim, marginTop: 8, lineHeight: 1.5 }}>{cat.detail}</div>
            </Panel>
          );
        })}
      </div>

      {/* Threats + Metrics */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 14 }}>
        <Panel>
          <PHdr sub="Live Monitoring" title="Active Threats" right={
            <Badge color={T.red}>{report.activeThreats?.length || 0}</Badge>
          } />
          <div>
            {(report.activeThreats || []).map((t, i) => {
              const tc = t.severity === "HIGH" ? T.red : t.severity === "MEDIUM" ? T.amber : T.cyan;
              return (
                <div key={i} style={{
                  padding: "12px 16px", borderBottom: `1px solid ${T.border}22`,
                  borderLeft: `3px solid ${tc}`,
                }}>
                  <div style={{ display: "flex", gap: 8, alignItems: "center", marginBottom: 4 }}>
                    <span style={{ fontFamily: T.mono, fontSize: 11, fontWeight: 700, color: T.textBright }}>{t.type}</span>
                    <Badge color={tc}>{t.severity}</Badge>
                  </div>
                  <div style={{ fontFamily: T.sans, fontSize: 11, color: T.textDim, lineHeight: 1.5 }}>{t.description}</div>
                </div>
              );
            })}
          </div>
        </Panel>

        <Panel>
          <PHdr sub="Data Points" title="Key Metrics" />
          <div style={{ padding: "8px 0" }}>
            {(report.keyMetrics || []).map((m, i) => (
              <div key={i} style={{
                padding: "10px 16px", borderBottom: `1px solid ${T.border}22`,
                display: "flex", justifyContent: "space-between", alignItems: "center",
              }}>
                <div>
                  <div style={{ fontFamily: T.mono, fontSize: 9, color: T.textDim, marginBottom: 2 }}>{m.label}</div>
                  <div style={{ fontFamily: T.mono, fontSize: 14, fontWeight: 700, color: T.textBright }}>{m.value}</div>
                </div>
                <span style={{ fontFamily: T.mono, fontSize: 16, color: trendColor(m.trend) }}>
                  {trendArrow(m.trend)}
                </span>
              </div>
            ))}
          </div>
        </Panel>
      </div>

      {/* Forecast + Recommendations */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 14 }}>
        <Panel>
          <PHdr sub="AI Projection" title="Risk Forecast" />
          <div style={{ padding: 16 }}>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 10, marginBottom: 14 }}>
              {[["30-Day Outlook", report.forecast?.["30day"]], ["90-Day Outlook", report.forecast?.["90day"]]].map(([l, v]) => (
                <div key={l} style={{
                  background: T.bg2, borderRadius: 6, padding: 12,
                  border: `1px solid ${T.border}`,
                }}>
                  <div style={{ fontFamily: T.mono, fontSize: 9, color: T.textDim, marginBottom: 6 }}>{l}</div>
                  <div style={{ fontFamily: T.sans, fontSize: 11, color: T.text, lineHeight: 1.5 }}>{v}</div>
                </div>
              ))}
            </div>
            <div style={{ marginBottom: 6 }}>
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 6 }}>
                <span style={{ fontFamily: T.mono, fontSize: 9, color: T.textDim }}>Escalation probability (90 days)</span>
                <span style={{ fontFamily: T.mono, fontSize: 11, color: riskColor(report.forecast?.probability) }}>
                  {report.forecast?.probability}%
                </span>
              </div>
              <Bar value={report.forecast?.probability || 0} />
            </div>
          </div>
        </Panel>

        <Panel>
          <PHdr sub="Strategic" title="Recommendations" />
          <div style={{ padding: 16, display: "flex", flexDirection: "column", gap: 10 }}>
            {(report.recommendations || []).map((r, i) => (
              <div key={i} style={{
                display: "flex", gap: 12, padding: "10px 12px",
                background: T.bg2, borderRadius: 6, border: `1px solid ${T.border}`,
              }}>
                <div style={{
                  width: 22, height: 22, borderRadius: 4, flexShrink: 0,
                  background: `${T.cyan}18`, border: `1px solid ${T.cyanDim}`,
                  display: "flex", alignItems: "center", justifyContent: "center",
                  fontFamily: T.mono, fontSize: 10, color: T.cyan, fontWeight: 700,
                }}>{i + 1}</div>
                <div style={{ fontFamily: T.sans, fontSize: 12, color: T.text, lineHeight: 1.5 }}>{r}</div>
              </div>
            ))}
          </div>
        </Panel>
      </div>
    </div>
  );
}

// ── Ask AI about location ─────────────────────────────────────────────────────
function LocationChat({ location, report }) {
  const [messages, setMessages] = useState([{
    role: "assistant",
    content: `Intelligence brief loaded for ${location.name}. Ask me anything about this location's risk profile — current threats, historical patterns, future projections, or comparisons with other regions.`,
  }]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const endRef = useRef(null);
  useEffect(() => endRef.current?.scrollIntoView({ behavior: "smooth" }), [messages]);

  const suggestions = [
    `What are the biggest risks in ${location.name}?`,
    `How has the risk changed over the past decade?`,
    `Compare ${location.name} to similar regions`,
    `What should be the priority response actions?`,
  ];

  const send = async (text) => {
    const msg = text || input.trim();
    if (!msg || loading) return;
    setInput("");
    const newMessages = [...messages, { role: "user", content: msg }];
    setMessages(newMessages);
    setLoading(true);

    const systemPrompt = `You are GeoSentinel AI. The user is viewing a risk intelligence report for ${location.name}, ${location.country} (${location.lat.toFixed(3)}°, ${location.lon.toFixed(3)}°).

Current report data:
- Overall Risk Index: ${report.overallRisk}/100 (${report.riskLevel})
- Climate: ${report.categories.climate.score}/100
- Disaster: ${report.categories.disaster.score}/100
- Water: ${report.categories.water.score}/100
- Food: ${report.categories.food.score}/100
- Health: ${report.categories.health.score}/100
- Conflict: ${report.categories.conflict.score}/100
- Active threats: ${report.activeThreats?.map(t => t.type).join(", ")}
- Summary: ${report.summary}

Answer questions about this location with expert-level precision. Be concise (3-4 sentences max per point). Use specific data where possible.`;

    try {
      const res = await fetch("https://api.anthropic.com/v1/messages", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          model: "claude-sonnet-4-6",
          max_tokens: 1000,
          system: systemPrompt,
          messages: newMessages.slice(-8).map(m => ({ role: m.role, content: m.content })),
        }),
      });
      const data = await res.json();
      setMessages(prev => [...prev, { role: "assistant", content: data.content?.[0]?.text || "Unable to retrieve analysis." }]);
    } catch {
      setMessages(prev => [...prev, { role: "assistant", content: "Connection interrupted." }]);
    } finally { setLoading(false); }
  };

  return (
    <Panel style={{ display: "flex", flexDirection: "column", height: 420 }}>
      <PHdr sub="AI Intelligence" title={`Ask about ${location.name}`}
        right={<Badge color={T.green}>ONLINE</Badge>} />
      <div style={{ flex: 1, overflow: "auto", padding: 16, display: "flex", flexDirection: "column", gap: 12 }}>
        {messages.map((m, i) => (
          <div key={i} style={{ display: "flex", flexDirection: "column", alignItems: m.role === "user" ? "flex-end" : "flex-start" }}>
            <div style={{ fontFamily: T.mono, fontSize: 8, color: m.role === "user" ? T.textDim : T.cyan, marginBottom: 3 }}>
              {m.role === "user" ? "YOU" : "✦ GEOSENTINEL AI"}
            </div>
            <div style={{
              maxWidth: "85%", padding: "10px 14px",
              borderRadius: m.role === "user" ? "10px 10px 3px 10px" : "10px 10px 10px 3px",
              background: m.role === "user" ? `${T.cyan}15` : T.bg2,
              border: `1px solid ${m.role === "user" ? T.cyanDim : T.border}`,
              fontFamily: T.sans, fontSize: 12, color: T.text, lineHeight: 1.7,
              whiteSpace: "pre-wrap",
            }}>{m.content}</div>
          </div>
        ))}
        {loading && (
          <div style={{ display: "flex", flexDirection: "column", alignItems: "flex-start" }}>
            <div style={{ fontFamily: T.mono, fontSize: 8, color: T.cyan, marginBottom: 3 }}>✦ GEOSENTINEL AI</div>
            <div style={{ padding: "10px 14px", borderRadius: "10px 10px 10px 3px", background: T.bg2, border: `1px solid ${T.border}`, display: "flex", gap: 5 }}>
              {[0,1,2].map(i => <div key={i} style={{ width: 6, height: 6, borderRadius: "50%", background: T.cyan, animation: "dotPulse 1.2s ease-in-out infinite", animationDelay: `${i*0.2}s` }} />)}
            </div>
          </div>
        )}
        <div ref={endRef} />
      </div>
      {messages.length <= 1 && (
        <div style={{ padding: "0 16px 10px", display: "flex", flexWrap: "wrap", gap: 6 }}>
          {suggestions.map(s => (
            <button key={s} onClick={() => send(s)} style={{
              fontFamily: T.mono, fontSize: 9, color: T.cyan,
              background: `${T.cyan}10`, border: `1px solid ${T.cyanDim}`,
              borderRadius: 4, padding: "4px 9px", cursor: "pointer",
            }}>{s}</button>
          ))}
        </div>
      )}
      <div style={{ padding: "10px 16px", borderTop: `1px solid ${T.border}`, display: "flex", gap: 8 }}>
        <input value={input} onChange={e => setInput(e.target.value)}
          onKeyDown={e => e.key === "Enter" && send()}
          placeholder="Ask about threats, forecasts, comparisons…"
          style={{
            flex: 1, background: T.bg2, border: `1px solid ${T.border}`,
            borderRadius: 6, padding: "8px 12px", fontFamily: T.mono,
            fontSize: 11, color: T.text, outline: "none",
          }} />
        <button onClick={() => send()} disabled={loading || !input.trim()}
          style={{
            padding: "8px 16px", borderRadius: 6, border: "none",
            background: loading || !input.trim() ? `${T.cyan}25` : T.cyan,
            color: T.bg0, fontFamily: T.mono, fontSize: 11, fontWeight: 700,
            cursor: loading || !input.trim() ? "default" : "pointer",
          }}>SEND</button>
      </div>
    </Panel>
  );
}

// ── Loading State ─────────────────────────────────────────────────────────────
function LoadingReport({ location }) {
  const [step, setStep] = useState(0);
  const steps = [
    "Acquiring satellite intelligence…",
    "Analyzing climate risk vectors…",
    "Cross-referencing disaster databases…",
    "Evaluating water & food security…",
    "Running health threat assessment…",
    "Generating composite risk index…",
  ];
  useEffect(() => {
    const t = setInterval(() => setStep(s => (s + 1) % steps.length), 800);
    return () => clearInterval(t);
  }, []);
  return (
    <div style={{ display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", height: 340, gap: 20 }}>
      <div style={{
        width: 64, height: 64, borderRadius: "50%",
        border: `2px solid ${T.cyanDim}`, borderTopColor: T.cyan,
        animation: "spin 1s linear infinite",
      }} />
      <div>
        <div style={{ fontFamily: T.sans, fontSize: 16, fontWeight: 600, color: T.textBright, textAlign: "center", marginBottom: 8 }}>
          Analyzing {location.name}
        </div>
        <div style={{ fontFamily: T.mono, fontSize: 11, color: T.cyan, textAlign: "center", animation: "blink 0.8s ease infinite" }}>
          {steps[step]}
        </div>
      </div>
    </div>
  );
}

// ── Main App ──────────────────────────────────────────────────────────────────
export default function GeoSentinel() {
  const [location, setLocation] = useState(null);
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState("");

  const handleSelect = async (loc) => {
    setLocation(loc);
    setReport(null);
    setError(null);
    setLoading(true);
    try {
      const data = await fetchLocationReport(loc);
      setReport(data);
    } catch (e) {
      setError("Failed to generate intelligence report. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const reset = () => { setLocation(null); setReport(null); setError(null); };

  return (
    <div style={{
      fontFamily: T.sans, background: T.bg0, color: T.text,
      minHeight: "100vh", display: "flex", flexDirection: "column",
    }}>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500;700&display=swap');
        * { box-sizing: border-box; margin: 0; padding: 0; }
        ::-webkit-scrollbar { width: 4px; }
        ::-webkit-scrollbar-track { background: transparent; }
        ::-webkit-scrollbar-thumb { background: #1A2E45; border-radius: 2px; }
        button { font-family: inherit; cursor: pointer; }
        input { font-family: inherit; }
        @keyframes pulseRing { 0% { r: 8; opacity: 0.7; } 100% { r: 32; opacity: 0; } }
        @keyframes spin { to { transform: rotate(360deg); } }
        @keyframes blink { 0%,100% { opacity: 1; } 50% { opacity: 0.4; } }
        @keyframes dotPulse { 0%,80%,100% { opacity: 0.3; transform: scale(0.8); } 40% { opacity: 1; transform: scale(1); } }
        @keyframes fadeUp { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: translateY(0); } }
      `}</style>

      {/* Header */}
      <header style={{
        height: 48, background: T.bg1, borderBottom: `1px solid ${T.border}`,
        display: "flex", alignItems: "center", padding: "0 20px", gap: 16, flexShrink: 0,
      }}>
        <div style={{ display: "flex", alignItems: "center", gap: 8, cursor: "pointer" }} onClick={reset}>
          <div style={{
            width: 28, height: 28, borderRadius: 7,
            background: `linear-gradient(135deg, ${T.cyan}, ${T.purple})`,
            display: "flex", alignItems: "center", justifyContent: "center",
            fontWeight: 900, fontSize: 13, color: T.bg0,
            boxShadow: `0 0 14px ${T.cyan}44`,
          }}>G</div>
          <span style={{ fontFamily: T.mono, fontSize: 13, fontWeight: 700, color: T.textBright, letterSpacing: "0.06em" }}>GEOSENTINEL</span>
          <span style={{
            fontFamily: T.mono, fontSize: 8, color: T.cyan, letterSpacing: "0.1em",
            background: `${T.cyan}15`, border: `1px solid ${T.cyanDim}`,
            borderRadius: 3, padding: "2px 6px",
          }}>RISK INTEL</span>
        </div>

        {location && (
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <span style={{ color: T.border, fontSize: 16 }}>›</span>
            <span style={{ fontFamily: T.mono, fontSize: 11, color: T.textDim }}>{location.name}</span>
            <button onClick={reset} style={{
              fontFamily: T.mono, fontSize: 9, color: T.textDim,
              background: T.bg2, border: `1px solid ${T.border}`,
              borderRadius: 4, padding: "3px 8px",
            }}>✕ CLEAR</button>
          </div>
        )}

        <div style={{ marginLeft: "auto", display: "flex", alignItems: "center", gap: 6 }}>
          <div style={{ width: 6, height: 6, borderRadius: "50%", background: T.green, boxShadow: `0 0 6px ${T.green}` }} />
          <span style={{ fontFamily: T.mono, fontSize: 9, color: T.green }}>LIVE</span>
        </div>
      </header>

      {/* Body */}
      <main style={{ flex: 1, padding: 20, maxWidth: 1100, margin: "0 auto", width: "100%" }}>

        {/* Search bar — always shown */}
        <div style={{
          marginBottom: 24,
          animation: location ? "none" : "fadeUp 0.4s ease",
        }}>
          {!location && (
            <div style={{ textAlign: "center", marginBottom: 28, paddingTop: 40 }}>
              <div style={{ fontFamily: T.mono, fontSize: 11, color: T.cyan, letterSpacing: "0.15em", marginBottom: 10 }}>
                PLANETARY RISK INTELLIGENCE PLATFORM
              </div>
              <div style={{ fontFamily: T.sans, fontSize: 28, fontWeight: 700, color: T.textBright, marginBottom: 8 }}>
                Select any location on Earth
              </div>
              <div style={{ fontFamily: T.sans, fontSize: 14, color: T.textDim }}>
                Get an AI-generated risk intelligence brief — climate, disasters, water, food, health & conflict
              </div>
            </div>
          )}

          <div style={{ display: "flex", justifyContent: location ? "flex-start" : "center" }}>
            <LocationSearch onSelect={handleSelect} />
          </div>
        </div>

        {/* Quick-select cards */}
        {!location && !loading && (
          <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 12, marginTop: 32, animation: "fadeUp 0.5s ease 0.1s both" }}>
            {[
              { name: "Bangladesh", country: "Bangladesh", countryCode: "BD", type: "Country", lat: 23.6850, lon: 90.3563, note: "Flood & climate risk" },
              { name: "Somalia", country: "Somalia", countryCode: "SO", type: "Country", lat: 5.1521, lon: 46.1996, note: "Drought & famine" },
              { name: "California", country: "USA", countryCode: "US", type: "State", lat: 36.7783, lon: -119.4179, note: "Wildfire & heatwave" },
              { name: "Pakistan", country: "Pakistan", countryCode: "PK", type: "Country", lat: 30.3753, lon: 69.3451, note: "Heatwave & flooding" },
              { name: "Amazon Basin", country: "Brazil", countryCode: "BR", type: "Region", lat: -3.4653, lon: -62.2159, note: "Deforestation crisis" },
              { name: "Yemen", country: "Yemen", countryCode: "YE", type: "Country", lat: 15.5527, lon: 48.5164, note: "Conflict & famine" },
            ].map(loc => (
              <div key={loc.name}
                onClick={() => handleSelect(loc)}
                style={{
                  padding: "14px 16px", borderRadius: 8, cursor: "pointer",
                  background: T.panel, border: `1px solid ${T.border}`,
                  transition: "all 0.15s",
                }}
                onMouseEnter={e => { e.currentTarget.style.borderColor = T.cyanDim; e.currentTarget.style.background = `${T.cyan}08`; }}
                onMouseLeave={e => { e.currentTarget.style.borderColor = T.border; e.currentTarget.style.background = T.panel; }}
              >
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 4 }}>
                  <div style={{ fontFamily: T.sans, fontSize: 13, fontWeight: 600, color: T.textBright }}>{loc.name}</div>
                  <span style={{ fontFamily: T.mono, fontSize: 8, color: T.textDim, background: T.bg2, border: `1px solid ${T.border}`, borderRadius: 3, padding: "2px 5px" }}>{loc.type.toUpperCase()}</span>
                </div>
                <div style={{ fontFamily: T.mono, fontSize: 9, color: T.textDim, marginBottom: 4 }}>{loc.country}</div>
                <div style={{ fontFamily: T.mono, fontSize: 9, color: T.cyan }}>{loc.note}</div>
              </div>
            ))}
          </div>
        )}

        {loading && location && <LoadingReport location={location} />}

        {error && (
          <div style={{ textAlign: "center", padding: 40 }}>
            <div style={{ fontFamily: T.mono, fontSize: 12, color: T.red }}>{error}</div>
          </div>
        )}

        {report && location && (
          <div style={{ display: "flex", flexDirection: "column", gap: 14, animation: "fadeUp 0.4s ease" }}>
            <ReportView location={location} report={report} />
            <LocationChat location={location} report={report} />
          </div>
        )}
      </main>
    </div>
  );
}
