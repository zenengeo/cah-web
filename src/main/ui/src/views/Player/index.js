import {useState} from "react";
import WhiteCard from "../../components/WhiteCard";
import "./Player.css";
import Button from "../../components/Button";
import CardsContainer from "../../components/CardsContainer";
import {usePlayerCards, useVotingCards} from "../../api/cards";
import {usePlayerWiring} from "./data";

function Waiting({text}) {
  return (
      <main className="Waiting">
        {text}
      </main>
  )
}

function PickCards({roomCode, playerId, handleSubmitCard}) {
  const [selectedCard, setSelectedCard] = useState();
  const cards = usePlayerCards(roomCode, playerId);

  return (
      <main className="PickCards">
        <h1>Pick your card to put up for vote</h1>
        <CardsContainer>
          {
            cards.map(card =>
                <WhiteCard key={card.id} text={card.card.text}
                           onClick={() => setSelectedCard(card)}
                           selected={selectedCard && selectedCard.id
                               === card.id}
                />
            )
          }
        </CardsContainer>
        <Button disabled={!selectedCard} className="PlayerSubmitButton" block
                onClick={() => handleSubmitCard(selectedCard)}>Send</Button>
      </main>
  )
}

function Vote({roomCode, round, handleVote}) {
  const [vote, setVote] = useState();
  const candidates = useVotingCards(roomCode, round);

  return (
      <main className="Vote">
        <h1>Pick your favorite</h1>
        <CardsContainer>
          {
            candidates.map(card =>
                <WhiteCard key={card.id} text={card.card.text}
                           onClick={() => setVote(card)}
                           selected={vote && vote.id === card.id}/>
            )
          }
        </CardsContainer>
        <Button disabled={!vote} className="PlayerSubmitButton" block
                onClick={() => handleVote(vote)}>Send</Button>
      </main>
  )
}

function Player({roomCode, playerId}) {
  const {
    state,
    round,
    handleSubmitCard,
    handleVote
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
      return <Vote roomCode={roomCode} round={round} handleVote={handleVote} />;
    case "voted":
      return <Waiting text="Waiting for others to vote..."/>

    case "winner_revealed":
      return <Waiting text="Round finished. Waiting on host..."/>
  }
}

export default Player;