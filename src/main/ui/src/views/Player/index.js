import { useState } from 'react';
import './Player.css';
import Button from '../../components/Button';
import { usePlayerCards, useVotingCards } from '../../api/cards';
import { usePlayerWiring } from './data';
import { useCurrentBlackCard } from '../Host/data';
import ShowAndPick from '../../components/ShowAndPick';

function Waiting({text}) {
  return (
      <main className="Waiting">
        {text}
      </main>
  )
}

function PickCards({roomCode, round, playerId, handleSubmitCard}) {
  const [selectedCard, setSelectedCard] = useState();
  const cards = usePlayerCards(roomCode, playerId);
  const blackCard = useCurrentBlackCard(roomCode, round);

  return (
      cards && blackCard ?
      <main className="PickCards">
        <h1>Pick your card</h1>
        <ShowAndPick
          blackCard={blackCard}
          whiteCards={cards}
          selectedCard={selectedCard}
          handleSelected={setSelectedCard}
          />
        <Button disabled={!selectedCard} className="PlayerSubmitButton" block
                onClick={() => handleSubmitCard(selectedCard)}>Send</Button>
      </main>
        : <div/>
  )
}

function Vote({roomCode, round, handleVote, submittedCard}) {
  const [vote, setVote] = useState();
  const candidates = useVotingCards(roomCode, round);
  const blackCard = useCurrentBlackCard(roomCode, round);

  function annotate(card) {
    return card.id === submittedCard.id ? 'Yours' : null;
  }

  return (
      candidates && blackCard ?
      <main className="Vote">
        <h1>Vote for the best</h1>
        <ShowAndPick
          blackCard={blackCard}
          whiteCards={candidates}
          selectedCard={vote}
          handleSelected={setVote}
          annotate={annotate}
          />
        <Button disabled={!vote} className="PlayerSubmitButton" block
                onClick={() => handleVote(vote)}>Send</Button>
      </main>
        : <div/>
  )
}

function Player({roomCode, playerId}) {
  const {
    state,
    round,
    handleSubmitCard,
    handleVote,
    submittedCard
  } = usePlayerWiring(roomCode, playerId);

  switch (state) {
    default:
    case "waiting_to_start":
      return <Waiting text="Waiting for game to start..."/>

    case "pick_cards":
      console.debug("pick_cards -> PickCards")
      return <PickCards roomCode={roomCode} round={round} playerId={playerId}
                        handleSubmitCard={handleSubmitCard}/>
    case "picked_card":
      console.debug("picked_card -> Waiting")
      return <Waiting text="Waiting for others to pick..."/>

    case "voting":
      console.debug("voting -> Vote")
      return <Vote roomCode={roomCode} round={round} handleVote={handleVote} submittedCard={submittedCard}/>;
    case "voted":
      return <Waiting text="Waiting for others to vote..."/>

    case "winner_revealed":
      return <Waiting text="Round finished. Waiting on host..."/>
  }
}

export default Player;