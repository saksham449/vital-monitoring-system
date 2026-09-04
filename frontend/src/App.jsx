import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import Header from './components/Header';
import BedGrid from './components/BedGrid';
import AlertPanel from './components/AlertPanel';
import AlertHistory from './components/AlertHistory';
import { getActiveAlerts, getAlertHistory, getBeds } from './services/api';
import './App.css';

const WS_URL = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws';

export default function App() {
  const [beds, setBeds] = useState([]);
  const [activeAlerts, setActiveAlerts] = useState([]);
  const [history, setHistory] = useState([]);
  const [connected, setConnected] = useState(false);
  const [tab, setTab] = useState('live');
  const [error, setError] = useState('');
  const clientRef = useRef(null);

  const loadInitialData = useCallback(async () => {
    try {
      const [bedData, activeData, historyData] = await Promise.all([
        getBeds(), getActiveAlerts(), getAlertHistory()
      ]);
      setBeds(bedData);
      setActiveAlerts(activeData);
      setHistory(historyData);
      setError('');
    } catch (err) {
      console.error(err);
      setError('Backend is unavailable. Start the backend and refresh this page.');
    }
  }, []);

  useEffect(() => { loadInitialData(); }, [loadInitialData]);

  useEffect(() => {
    const client = new Client({
      brokerURL: WS_URL,
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        setConnected(true);
        setError('');
        client.subscribe('/topic/vitals', (message) => {
          const incoming = JSON.parse(message.body);
          setBeds((current) => {
            const exists = current.some((bed) => bed.bedId === incoming.bedId);
            if (!exists) return [...current, incoming];
            return current.map((bed) => bed.bedId === incoming.bedId ? incoming : bed);
          });
        });
        client.subscribe('/topic/alerts', (message) => {
          const incoming = JSON.parse(message.body);
          setActiveAlerts((current) => {
            if (incoming.status === 'RESOLVED') {
              return current.filter((alert) => alert.id !== incoming.id);
            }
            const exists = current.some((alert) => alert.id === incoming.id);
            if (exists) return current.map((alert) => alert.id === incoming.id ? incoming : alert);
            return [incoming, ...current];
          });
          setHistory((current) => {
            const exists = current.some((alert) => alert.id === incoming.id);
            if (exists) return current.map((alert) => alert.id === incoming.id ? incoming : alert);
            return [incoming, ...current];
          });
        });
      },
      onDisconnect: () => setConnected(false),
      onWebSocketClose: () => setConnected(false),
      onWebSocketError: () => setConnected(false),
      onStompError: (frame) => {
        console.error('STOMP error', frame.headers?.message);
        setConnected(false);
      }
    });

    clientRef.current = client;
    client.activate();
    return () => {
      setConnected(false);
      client.deactivate();
      clientRef.current = null;
    };
  }, []);

  const counts = useMemo(() => ({
    normal: beds.filter((b) => b.status === 'NORMAL').length,
    warning: beds.filter((b) => b.status === 'WARNING').length,
    critical: beds.filter((b) => b.status === 'CRITICAL').length,
  }), [beds]);

  return (
    <div className="app-shell">
      <Header connected={connected} />

      {error && <div className="error-banner">{error}</div>}

      <main>
        <div className="summary-grid">
          <Summary label="Monitored Beds" value={beds.length} />
          <Summary label="Normal" value={counts.normal} className="normal-text" />
          <Summary label="Warning" value={counts.warning} className="warning-text" />
          <Summary label="Critical" value={counts.critical} className="critical-text" />
          <Summary label="Active Alerts" value={activeAlerts.length} className="critical-text" />
        </div>

        <div className="tabs">
          <button className={tab === 'live' ? 'active' : ''} onClick={() => setTab('live')}>Live Dashboard</button>
          <button className={tab === 'history' ? 'active' : ''} onClick={() => setTab('history')}>Alert History</button>
        </div>

        {tab === 'live' ? (
          <div className="live-layout">
            <BedGrid beds={beds} />
            <AlertPanel alerts={activeAlerts} />
          </div>
        ) : (
          <AlertHistory alerts={history} />
        )}
      </main>

      <footer>
        Demo system • Synthetic patient data • Not a clinical decision system
      </footer>
    </div>
  );
}

function Summary({ label, value, className = '' }) {
  return (
    <div className="summary-card">
      <span>{label}</span>
      <strong className={className}>{value}</strong>
    </div>
  );
}
