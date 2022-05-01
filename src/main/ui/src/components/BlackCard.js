import "./Cards.css";
import "./BlackCard.css";
import BreakableText from "./BreakableText";

function BlackCard({text, by, large=true}) {
  let className = "BlackCard";
  if (large) {
    className += " LargeCard";
  }
  else {
    className += " SmallCard";
  }
  return (
      <div className={className}>
        <BreakableText text={text}/>
      </div>
  );
}

export default BlackCard;