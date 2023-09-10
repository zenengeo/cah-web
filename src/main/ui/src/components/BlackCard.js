import './Cards.css';
import './BlackCard.css';
import BreakableText from './BreakableText';

function BlackCard({text, sizing = 'large'}) {
  let className = `Card BlackCard ${sizing}`;

  return (
      <div className={className}>
        <BreakableText text={text}/>
      </div>
  );
}

export default BlackCard;