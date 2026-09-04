const API_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '');

async function request(path) {
  const response = await fetch(`${API_URL}${path}`);
  if (!response.ok) throw new Error(`API request failed: ${response.status}`);
  return response.json();
}

export const getBeds = () => request('/api/beds');
export const getActiveAlerts = () => request('/api/alerts/active');
export const getAlertHistory = () => request('/api/alerts');
