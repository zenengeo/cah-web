import './App.css';
import {useState} from "react";
import Lobby from "./views/Lobby";
import Host from "./views/Host";
import Player from "./views/Player";
import {postJson} from "./utils/fetchWrappers";

function App() {
  const [mode, setMode] = useState("lobby");
  const [roomCode, setRoomCode] = useState("");
  const [playerName, setPlayerName] = useState("");
  const [playerId, setPlayerId] = useState("");

  console.log("App render");

  function startAsHost() {
    postJson('/room')
        .then(roomInfo => {
          setRoomCode(roomInfo.roomCode);
          setMode("host");
        })
        .catch(err => console.error("Failed to create room", err));
  }

  function startAsPlayer(playerName, roomCode) {
    postJson(`/room/${roomCode}/join`, {
      playerName
    })
        .then(data => {
          setRoomCode(roomCode);
          setPlayerName(data.playerName);
          setPlayerId(data.playerId);
          setMode("player");
        })
        .catch(err => console.error("Failed to join", err));
  }

  switch (mode) {
    case "host":
      return <Host roomCode={roomCode}/>
    case "player":
      return <Player playerId={playerId} playerName={playerName}
                     roomCode={roomCode}/>
    case "lobby":
    default:
      return <Lobby startAsHost={startAsHost} startAsPlayer={startAsPlayer}/>

  }
}

export default App;
