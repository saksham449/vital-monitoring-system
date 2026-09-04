export default function Header({ connected }) {
  return (
    <header className="header">
      <div>
        <div className="eyebrow">CENTRAL MONITORING</div>
        <h1>Central Vital Monitoring</h1>
        <p>Real-time multi-patient IoT monitoring dashboard</p>
      </div>
      <div className={`connection ${connected ? 'connected' : 'disconnected'}`}>
        <span>●</span> {connected ? 'Live monitoring' : 'Disconnected'}
      </div>
    </header>
  );
}
