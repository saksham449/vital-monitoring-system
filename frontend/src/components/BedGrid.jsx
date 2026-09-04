import BedCard from './BedCard';

export default function BedGrid({ beds }) {
  return (
    <section>
      <div className="section-heading">
        <h2>Patient Beds</h2>
        <span>{beds.length} monitored</span>
      </div>
      <div className="bed-grid">
        {beds.map((bed) => <BedCard key={bed.bedId} bed={bed} />)}
      </div>
    </section>
  );
}
