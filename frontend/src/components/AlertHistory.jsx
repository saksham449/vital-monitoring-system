function formatDate(value) {
  if (!value) return '—';
  return new Date(value).toLocaleString();
}

export default function AlertHistory({ alerts }) {
  return (
    <section className="panel history-panel">
      <div className="section-heading">
        <h2>Alert History</h2>
        <span>{alerts.length} records</span>
      </div>
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Bed</th><th>Patient</th><th>Vital</th><th>Value</th>
              <th>Severity</th><th>Status</th><th>Created</th><th>Resolved</th>
            </tr>
          </thead>
          <tbody>
            {alerts.map((alert) => (
              <tr key={alert.id}>
                <td>{alert.bedId}</td>
                <td>{alert.patientName}</td>
                <td>{alert.vitalType.replaceAll('_', ' ')}</td>
                <td>{alert.value}</td>
                <td><span className={`severity ${alert.severity.toLowerCase()}`}>{alert.severity}</span></td>
                <td><span className={`history-status ${alert.status.toLowerCase()}`}>{alert.status}</span></td>
                <td>{formatDate(alert.createdAt)}</td>
                <td>{formatDate(alert.resolvedAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {alerts.length === 0 && <div className="empty-state">No alert history yet.</div>}
      </div>
    </section>
  );
}
