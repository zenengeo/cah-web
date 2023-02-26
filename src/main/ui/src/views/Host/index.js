import "./Host.css";
import {PlayersJoining} from "./PlayersJoining";
import {Selecting} from "./Selecting";
import {Voting} from "./Voting";
import {Reveal} from "./Reveal";
import {useHostWiring} from "./data";

function Main({roomCode}) {
  const {
    state,
    players,
    round,
    startRound,
    addGhostPlayer
  } = useHostWiring(roomCode);

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