import { useEffect, useState } from 'react';
import { getJson, postJson } from '../utils/fetchWrappers';

export class DealtCardModel {
  /**
   * @type string
   */
  id
  /**
   * @type WhiteCardModel
   */
  card
}

export class WhiteCardModel {
  constructor (text) {
    this.text = text;
  }

  /**
   * @type string
   */
  text
}

/**
 *
 * @param roomCode
 * @param round
 * @returns {DealtCardModel[]}
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
 * @returns {DealtCardModel[]}
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

export class BlackCardModel {
  constructor (text, slots, by) {
    this.text = text;
    this.slots = slots;
    this.by = by;
  }

  /**
   * @type string
   */
  text;
  /**
   * @type number
   */
  slots;
  /**
   * @type string
   */
  by;
}

export function blackCardFromJson (json) {
  return new BlackCardModel(
    json.text, json.slots, json.by,
  );
}

export function whiteCardFromJson (json) {
  return new WhiteCardModel(json.text);
}