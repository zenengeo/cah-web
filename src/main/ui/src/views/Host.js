import {useEffect, useState} from "react";
import Button from "../components/Button";
import {getJson, postJson} from "../utils/fetchWrappers";
import BlackCard from "../components/BlackCard";
import WhiteCard from "../components/WhiteCard";
import "./Host.css";
import "../utils/Flex.css";
import GhostIcon from "../components/GhostIcon";

function PlayersJoining({players, onReadyToStart, addGhostPlayer}) {
  const nonGhostPlayers = players.filter(player => !player.ghost).length;
  const lackingPlayerCount = Math.max(0, 2 - nonGhostPlayers);
  return (
      <main className="PlayersJoining">
        <div className="Players">
          <h2>Players</h2>
          <div className="PlayersJoined">
            {
              players.map(player =>
                  <div className="JoinedPlayer" key={player.playerId}>
                    {player.playerName} {player.ghost && <GhostIcon />}
                  </div>
              )
            }
          </div>
        </div>
        <div>
          <Button className="AddGhostButton" block onClick={addGhostPlayer}><GhostIcon /> Add ghost player</Button>
        </div>
        {
          nonGhostPlayers >= 2 ?
            <div>
              <Button className="ReadyToStartButton" block onClick={onReadyToStart}>Ready to start!</Button>
            </div>
              : <div>
                  Need {lackingPlayerCount} more player{lackingPlayerCount===1?"":"s"}
              </div>
        }
      </main>
  );
}

function useCurrentBlackCard(roomCode, round) {
  const [blackCard, setBlackCard] = useState();

  useEffect(() => {
    getJson(`/room/${roomCode}/blackCard`)
        .then(json => setBlackCard(json))
        .catch(err => console.error("Failed to get black card", err));
  }, [roomCode, round]);

  return blackCard;
}

function Selecting({roomCode, round}) {
  const blackCard = useCurrentBlackCard(roomCode, round);

  return (
      <main className="Selecting">
        { blackCard && <BlackCard text={blackCard.text}/> }
      </main>
  );

}

function Voting({roomCode, round}) {
  const blackCard = useCurrentBlackCard(roomCode, round);

  return (
      <main className="Voting">
        <div>{blackCard && <BlackCard text={blackCard.text}/>}</div>
        <div className="Status">Waiting for votes...</div>
      </main>
  );
}

function Reveal({roomCode, round, startNewRound}) {
  const [winners, setWinners] = useState([]);
  const [scores, setScores] = useState([]);
  const blackCard = useCurrentBlackCard(roomCode, round);

  useEffect(() => {
    getJson(`/room/${roomCode}/winners`)
        .then(json => setWinners(json.contents))
        .catch(err => {
          console.error("Failed to get winners", err);
        })
  }, [roomCode, round]);

  useEffect(() => {
    getJson(`/room/${roomCode}/scores`)
        .then(json => setScores(json.contents))
        .catch(err => {
          console.error("Failed to get scores", err);
        })
  }, [roomCode, round]);

  if (!winners || !blackCard || !scores) {
    return <main>Calculating...</main>
  }

  return (
      <main className="Reveal">
        <div className="Cards">
          <BlackCard text={blackCard.text} large={false}/>
          <div className="WinningCards">
            {
              winners.map(winner =>
                  <div key={winner.playedBy.playerName} className="WinningCard">
                    <div className="PlayedByText">
                      Played by {winner.playedBy.playerName}{winner.playedBy.ghost && <GhostIcon className="GhostPlayer" />} with {winner.votes} vote{winner.votes>1?"s":""}
                    </div>
                    <div>
                      <WhiteCard text={winner.card.card.text}/>
                    </div>
                  </div>
              )
            }
          </div>
        </div>
        <div className="AllScores">
          <div className="AllScoresTitle TextCenter">Scores</div>
          <div className="PlayerScores">
            {
              scores.map(playerScore =>
                <div key={playerScore.info.playerId} className="PlayerScore FlexColumn TextCenter">
                  <div className="PlayerScoreName">
                    {playerScore.info.playerName} {playerScore.info.ghost && <GhostIcon />}
                  </div>
                  <div>{playerScore.score}</div>
                </div>
              )
            }
          </div>
        </div>
        <div className="FlexRow FlexJustifyCenter">
          <Button className="StartNewRoundButton ShrinkVertical" autoFocus onClick={startNewRound}>Start New Round</Button>
        </div>
      </main>
  );

}

function Main({roomCode}) {
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

  switch (state) {
    default:
    case "players_joining":
      return <PlayersJoining players={players} onReadyToStart={startRound} addGhostPlayer={addGhostPlayer}/>;

    case "selecting":
      return <Selecting roomCode={roomCode} round={round}/>;

    case "voting":
      return <Voting roomCode={roomCode} round={round}/>;

    case "reveal":
      return <Reveal roomCode={roomCode} round={round} startNewRound={startRound}/>;
  }
}

function Host({roomCode}) {
  return (
      <div className="Host">
        <Main roomCode={roomCode}/>
        <footer>Join this game with the code <span className="RoomCode">{roomCode}</span></footer>
      </div>
  );
}

export default Host;