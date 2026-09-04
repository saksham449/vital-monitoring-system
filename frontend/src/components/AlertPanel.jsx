function AlertItem({ alert }) {
  return (
    <div className={`alert-item ${alert.severity.toLowerCase()}`}>
      <div className="alert-main">
        <div>
          <strong>{alert.bedId}</strong>
          <span>{alert.patientName}</span>
        </div>
        <span className={`severity ${alert.severity.toLowerCase()}`}>{alert.severity}</span>
      </div>
      <div className="alert-detail">
        <strong>{alert.vitalType.replaceAll('_', ' ')} — {alert.value}</strong>
        <span>Threshold: {alert.threshold}</span>
      </div>
    </div>
  );
}

export default function AlertPanel({ alerts }) {
  return (
    <section className="panel">
      <div className="section-heading">
        <h2>Active Alerts</h2>
        <span>{alerts.length}</span>
      </div>
      {alerts.length === 0 ? (
        <div className="empty-state">No active alerts</div>
      ) : (
        <div className="alert-list">
          {alerts.map((alert) => <AlertItem key={alert.id} alert={alert} />)}
        </div>
      )}
    </section>
  );
}
