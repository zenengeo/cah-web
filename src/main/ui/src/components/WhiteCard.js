import "./Cards.css";
import "./WhiteCard.css";
import BreakableText from "./BreakableText";

function WhiteCard({text, by, selected=false, onClick, large=false}) {
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
        <BreakableText text={text}/>
      </div>
  );
}

export default WhiteCard;