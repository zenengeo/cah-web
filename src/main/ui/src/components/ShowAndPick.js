import BlackCard from './BlackCard';
import CardsContainer from './CardsContainer';
import WhiteCard from './WhiteCard';
import './ShowAndPick.css';

/**
 *
 * @param blackCard
 * @param whiteCards
 * @param selectedCard
 * @param handleSelected {function(DealtCard)}
 * @param annotate {function(DealtCard):string}
 * @returns {JSX.Element}
 * @constructor
 */
function ShowAndPick({blackCard, whiteCards, selectedCard, handleSelected, annotate}) {
  return (
    <div className="ShowAndPick">
      <BlackCard text={blackCard.text} by={blackCard.by} large={false} sizing="smallest"/>
      <CardsContainer>
        {
          whiteCards.map(card =>
            <WhiteCard key={card.id} text={card.card.text}
                       onClick={() => handleSelected(card)}
                       selected={selectedCard
                         && selectedCard.id === card.id}
                       annotated={annotate && annotate(card)}
            />
          )
        }
      </CardsContainer>

    </div>
  )
}

export default ShowAndPick;