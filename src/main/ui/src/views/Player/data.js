import { useEffect, useState } from 'react'
import { getJson, postJson } from '../../utils/fetchWrappers'

class PlayerWiring {
  /**
   * @type {string}
   */
  state;
  /**
   * @type {number}
   */
  round;
  /**
   * @type {function(DealtCard)}
   */
  handleSubmitCard;
  /**
   * @type {function(DealtCard)}
   */
  handleVote;
  /**
   * @type DealtCard
   */
  submittedCard;

  constructor(state, round, handleSubmitCard, handleVote, submittedCard) {
    this.state = state;
    this.round = round;
    this.handleSubmitCard = handleSubmitCard;
    this.handleVote = handleVote;
    this.submittedCard = submittedCard;
  }
}

/**
 * @return {PlayerWiring}
 */
export function usePlayerWiring(roomCode, playerId) {
  const [state, setState] = useState("waiting_to_start");
  const [round, setRound] = useState(0);
  const [submittedCard, setSubmittedCard] = useState(null);

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
        .then(() => {
          setSubmittedCard(card);
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

  return new PlayerWiring(state, round, handleSubmitCard, handleVote, submittedCard);
}