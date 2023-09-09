import './Cards.css';
import './BlackCard.css';
import BreakableText from './BreakableText';

function BlackCard({text, large=true, sizing}) {
  let className = "BlackCard";
  if (sizing) {
    switch (sizing) {
      case 'smallest':
        className += ' SmallestCard'
        break;
      case 'small':
        className += ' SmallCard';
        break;
      case 'large':
      default:
        className += ' LargeCard';
        break;
    }
  }
  else {
    if (large) {
      className += " LargeCard";
    }
    else {
      className += " SmallCard";
    }
  }
  return (
      <div className={className}>
        <BreakableText text={text}/>
      </div>
  );
}

export default BlackCard;