function Vital({ label, value, unit }) {
  return (
    <div className="vital">
      <span>{label}</span>
      <strong>{value ?? '—'} <small>{unit}</small></strong>
    </div>
  );
}

export default function BedCard({ bed }) {
  const status = bed.status?.toLowerCase() || 'normal';
  return (
    <article className={`bed-card ${status}`}>
      <div className="bed-card-top">
        <div>
          <div className="bed-id">{bed.bedId}</div>
          <h3>{bed.patientName}</h3>
          <span className="age">Age {bed.age}</span>
        </div>
        <span className={`status-badge ${status}`}>{bed.status}</span>
      </div>
      <div className="vitals-grid">
        <Vital label="Heart Rate" value={bed.heartRate} unit="BPM" />
        <Vital label="SpO2" value={bed.spo2} unit="%" />
        <Vital label="Temperature" value={bed.temperature?.toFixed?.(1) ?? bed.temperature} unit="°C" />
        <Vital label="Blood Pressure" value={bed.systolic != null ? `${bed.systolic}/${bed.diastolic}` : null} unit="mmHg" />
      </div>
    </article>
  );
}
