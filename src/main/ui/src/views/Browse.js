import './Browse.css';
import Button from '../components/Button';
import { useEffect, useState } from 'react';
import BlackCard from '../components/BlackCard';
import WhiteCard from '../components/WhiteCard';
import { getJson } from '../utils/fetchWrappers';
import { blackCardFromJson, whiteCardFromJson } from '../api/cards';

class CardSet {
  constructor (blackCard, whiteCards) {
    this.blackCard = blackCard;
    this.whiteCards = whiteCards;
  }

  blackCard;
  whiteCards;
}

function Cards ({ cardSet, sizing }) {
  return (
    <div className="BrowseCardsArea">
      <div className="BlackCardArea">
        <BlackCard text={cardSet.blackCard.text} sizing={sizing}/>
      </div>
      <div className="WhiteCardsArea">
        {
          cardSet.whiteCards.map(
            card => <WhiteCard key={card.text} text={card.text}
                               sizing={sizing}/>)
        }
      </div>
    </div>
  );
}

function Browse () {
  const sizing = 'medium';
  const [cardSet, setCardSet] = useState(null);
  const [retrieving, setRetrieving] = useState(false);

  function loadNextCards () {
      setRetrieving(true);
      getJson('/cards/randomBlack')
        .then(resp => {
          const blackCard = blackCardFromJson(resp.contents[0]);
          getJson(`/cards/randomWhite?count=${blackCard.slots}`)
            .then(resp => {
              setCardSet(
                new CardSet(blackCard,
                  resp.contents.map(json => whiteCardFromJson(json)),
                ),
              );
              setRetrieving(false);
              },
            );
        });
  }

  useEffect(() => {
    loadNextCards();
  }, []);

  return (
    cardSet && <main className="Browse">
      <Cards cardSet={cardSet} sizing={sizing}/>
      <div className="BrowseControls">
        <Button autoFocus aria-disabled={retrieving}
                onClick={() => {retrieving || loadNextCards()}}
                block={true}>Next</Button>
      </div>
    </main>
  );
}

export default Browse;