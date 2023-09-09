import './Cards.css'
import './WhiteCard.css'
import BreakableText from './BreakableText'

function WhiteCard({text, selected=false, onClick, large=false, annotated}) {
  let className = "WhiteCard";
  if (onClick) {
    className += " Clickable";
  }
  if (selected) {
    className += " Selected";
  }
  if (large) {
    className += " LargeCard";
  }
  else {
    className += " SmallCard";
  }
  return (
      <div onClick={onClick} className={className}>
        { annotated && <div className="Annotation">{annotated}</div> }
        <BreakableText text={text}/>
      </div>
  );
}

export default WhiteCard;