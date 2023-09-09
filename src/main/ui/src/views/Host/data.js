import { useEffect, useState } from 'react'
import { getJson, postJson } from '../../utils/fetchWrappers'

class BlackCardModel {
  constructor (text, slots, by) {
    this.text = text
    this.slots = slots
    this.by = by
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

/**
 *
 * @param roomCode
 * @param round
 * @returns {BlackCardModel}
 */
export function useCurrentBlackCard(roomCode, round) {
  const [blackCard, setBlackCard] = useState(null)

  useEffect(() => {
    getJson(`/room/${roomCode}/blackCard`)
    .then(json => setBlackCard(new BlackCardModel(
          json.text, json.slots, json.by,
        )),
      )
    .catch(err => console.error('Failed to get black card', err))
  }, [roomCode, round])

  return blackCard
}

export function useWinners(roomCode, round) {
  const [winners, setWinners] = useState([]);

  useEffect(() => {
    getJson(`/room/${roomCode}/winners`)
        .then(json => setWinners(json.contents))
        .catch(err => {
          console.error("Failed to get winners", err);
        })
  }, [roomCode, round]);

  return winners;
}

export function useScores(roomCode, round) {
  const [scores, setScores] = useState([]);
  useEffect(() => {
    getJson(`/room/${roomCode}/scores`)
        .then(json => setScores(json.contents))
        .catch(err => {
          console.error("Failed to get scores", err);
        })
  }, [roomCode, round]);

  return scores;
}

export class HostWiring {
  state;
  players;
  round;
  startRound;
  addGhostPlayer;

  constructor(state, players, round, startRound, addGhostPlayer) {
    this.state = state;
    this.players = players;
    this.round = round;
    this.startRound = startRound;
    this.addGhostPlayer = addGhostPlayer;
  }
}

/**
 * @param roomCode
 * @returns {HostWiring}
 */
export function useHostWiring(roomCode) {
  /*
States
======================
players_joining
   │
   │
   ▼
selecting◄──────┐
   │            │
   │            │
   ▼            │
voting          │
   │            │
   │            │
   ▼            │
reveal          │
   │            │
   └────────────┘
 */
  const [state, setState] = useState("players_joining");
  const [players, setPlayers] = useState([]);
  const [round, setRound] = useState(0);

  useEffect(() => {
    function requestTally() {
      postJson(`/room/${roomCode}/tally`)
          .catch(err => {
            console.error("Could not tally votes", err);
          });
    }

    function updatePlayers() {
      getJson(`/room/${roomCode}/players`)
          .then(json => {
            console.log("Updating players");
            setPlayers(json.contents);
          })
          .catch(err => console.error("Failed to get players", err));
    }

    function handleGameEvent(gameEvent) {
      console.log("Host got game event", gameEvent);
      switch (gameEvent.action) {
        case "PlayerJoined":
          updatePlayers();
          break;

        case "RoundStarted":
          getJson(`/room/${roomCode}/round`)
              .then(json => {
                setRound(json.value);
                setState("selecting");
              })
          break;

        case "CardSubmitted":
          // TODO show progress
          break;

        case "VotingStarted":
          setState("voting");
          break;

        case "VoteSubmitted":
          // TODO show progress
          break;

        case "VotingCompleted":
          requestTally();
          break;

        case "WinnerRevealed":
          setState("reveal")
          break;

        default:
          console.error("Unknown event", gameEvent);
          break;
      }
    }

    console.debug("Host opening event source", roomCode);
    const eventSource = new EventSource(`/room/${roomCode}/events/host`);
    eventSource.addEventListener('error', ev => {
      console.error("Failed to get events", ev);
    })

    eventSource.addEventListener("message", ev => {
      console.log("Host got event", ev);
      const msg = JSON.parse(ev.data);
      handleGameEvent(msg);
    });

    return () => {
      console.debug("Host closing event source", roomCode);
      eventSource.close();
    }
  }, [roomCode]);

  function startRound() {
    postJson(`/room/${roomCode}/start`)
        .catch(err => console.error("Failed to start", err));
  }

  function addGhostPlayer() {
    postJson(`/room/${roomCode}/ghostPlayer`)
        .catch(err => console.error("Failed to add ghost player", err));
  }

  return new HostWiring(state, players, round, startRound, addGhostPlayer);
}