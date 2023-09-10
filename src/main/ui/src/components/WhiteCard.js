import './Cards.css';
import './WhiteCard.css';
import BreakableText from './BreakableText';

function WhiteCard({text, selected=false, onClick, sizing = 'small', annotated}) {
  let className = `Card WhiteCard ${sizing}`;
  if (onClick) {
    className += " Clickable";
  }
  if (selected) {
    className += " Selected";
  }

  return (
      <div onClick={onClick} className={className}>
        { annotated && <div className="Annotation">{annotated}</div> }
        <BreakableText text={text}/>
      </div>
  );
}

export default WhiteCard;