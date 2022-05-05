import {useEffect, useState} from "react";
import {getJson, postJson} from "../utils/fetchWrappers";
import WhiteCard from "../components/WhiteCard";
import "./Player.css";
import Button from "../components/Button";

function Waiting({text}) {
  return (
      <main className="Waiting">
        {text}
      </main>
  )
}

function PickCards({roomCode, playerId, handleSubmitCard}) {
  const [cards, setCards] = useState([]);
  const [selectedCard, setSelectedCard] = useState();

  useEffect(() => {
    postJson(`/room/${roomCode}/whiteCards`, {
      playerId,
      count: 4
    })
        .then(json => {
          setCards(json.contents);
        })
        .catch(err => console.error("Failed to deal white cards", err));
  }, [roomCode, playerId]);

  return (
      <main className="PickCards">
        <h1>Pick your card to put up for vote</h1>
        <div className="CardsContainer">
          {
            cards.flatMap(card =>
                <WhiteCard key={card.id} text={card.card.text}
                           onClick={() => setSelectedCard(card)}
                           selected={selectedCard && selectedCard.id
                               === card.id}
                />
            )
          }
        </div>
        <Button disabled={!selectedCard} className="PlayerSubmitButton" block
                onClick={() => handleSubmitCard(selectedCard)}>Send</Button>
      </main>
  )
}

function Vote({roomCode, round, handleVote}) {
  const [candidates, setCandidates] = useState([]);
  const [vote, setVote] = useState();

  useEffect(() => {
    getJson(`/room/${roomCode}/submittedCards`)
        .then(json => {
            setCandidates(json.contents);
        });
  }, [roomCode, round]);

  return (
      <main className="Vote">
        <h1>Pick your favorite</h1>
        <div className="CardsContainer">
          {candidates.flatMap(card =>
              <WhiteCard key={card.id} text={card.card.text}
                         onClick={() => setVote(card)}
                         selected={vote && vote.id === card.id}/>
          )
          }
        </div>
            <Button disabled={!vote} className="PlayerSubmitButton" block
                    onClick={() => handleVote(vote)}>Send</Button>
      </main>
  )
}

function Player({roomCode, playerId, playerName}) {
  const [state, setState] = useState("waiting_to_start");
  const [round, setRound] = useState(0);

  useEffect(() => {

    function handleGameEvent(event) {
      console.debug("Player received game event", event);
      switch (event.action) {
        case 'RoundStarted':
          getJson(`/room/${roomCode}/round`)
              .then(json => {
                setRound(json.value);
                setState("pick_cards");
              })
          break;

        case 'VotingStarted':
          setState("voting");
          break;

        case 'WinnerRevealed':
          setState("winner_revealed");
          break;

        default:
          console.log("Unknown event", event);
          break;
      }
    }

    console.debug("Player opening event source", playerId, roomCode);
    const eventSource = new EventSource(`/room/${roomCode}/events/player`);
    eventSource.addEventListener("error",
        ev => console.error("Failed to get player events", ev));
    eventSource.addEventListener("message",
        ev => handleGameEvent(JSON.parse(ev.data)));

    return () => {
      console.debug("Player closing event source", playerId, roomCode);
      eventSource.close()
    }
  }, [roomCode, playerId]);

  function handleSubmitCard(card) {
    setState("picked_card");
    postJson(`/room/${roomCode}/submittedCard`, {
      cardId: card.id,
      playerId
    })
        .catch(err => {
          console.error("Failed to submit card", err);
        });
  }

  function handleVote(card) {
    setState("voted");
    postJson(`/room/${roomCode}/vote`, {
      cardId: card.id,
      playerId
    })
        .catch(err => {
          console.error("Failed to submit card", err);
        });
  }

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