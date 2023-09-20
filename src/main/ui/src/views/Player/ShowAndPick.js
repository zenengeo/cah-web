import BlackCard from '../../components/BlackCard';
import WhiteCard from '../../components/WhiteCard';
import './ShowAndPick.css';
import Button from '../../components/Button';
import { useState } from 'react';

/**
 *
 * @param title {string}
 * @param submitText {string}
 * @param blackCard {BlackCardModel}
 * @param whiteCards {DealtCardModel[]}
 * @param handleSubmit {function(DealtCardModel)}
 * @param annotate {function(DealtCardModel):string}
 * @returns {JSX.Element}
 * @constructor
 */
function ShowAndPick({title, submitText, blackCard, whiteCards, handleSubmit, annotate}) {
  const [selectedCard, setSelectedCard] = useState(null);

  return (
    <main className="ShowAndPick">
      <h1>{title}</h1>
      <BlackCard text={blackCard.text} by={blackCard.by} sizing="small"/>
      <div className="PickWhiteCards FlexGrowOnly">
        {
          whiteCards.map(card =>
            <WhiteCard key={card.id} text={card.card.text}
                       onClick={() => setSelectedCard(card)}
                       selected={selectedCard
                         && selectedCard.id === card.id}
                       annotated={annotate && annotate(card)}
            />,
          )
        }
      </div>
      <div className="PickButtonArea">
        <Button disabled={!selectedCard} className="PlayerSubmitButton"
                block
                onClick={() => handleSubmit(selectedCard)}>{submitText}</Button>
      </div>
    </main>
  )
}

export default ShowAndPick;