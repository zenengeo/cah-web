import {useEffect, useState} from "react";
import {getJson, postJson} from "../utils/fetchWrappers";

export class DealtCard {
  /**
   * @type string
   */
  id
  /**
   * @type WhiteCard
   */
  card
}

export class WhiteCard {
  /**
   * @type string
   */
  text
}

/**
 *
 * @param roomCode
 * @param round
 * @returns {DealtCard[]}
 */
export function useVotingCards(roomCode, round) {
  const [candidates, setCandidates] = useState([]);
  useEffect(() => {
    getJson(`/room/${roomCode}/submittedCards`)
        .then(json => {
          setCandidates(json.contents);
        });
  }, [roomCode, round]);

  return candidates;
}

/**
 *
 * @param roomCode
 * @param playerId
 * @returns {DealtCard[]}
 */
export function usePlayerCards(roomCode, playerId) {
  const [cards, setCards] = useState([]);
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

  return cards;
}