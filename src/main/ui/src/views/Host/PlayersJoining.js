import GhostIcon from "../../components/GhostIcon";
import Button from "../../components/Button";

export function PlayersJoining({players, onReadyToStart, addGhostPlayer}) {
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
                    {player.playerName} {player.ghost && <GhostIcon/>}
                  </div>
              )
            }
          </div>
        </div>
        <div>
          <Button className="AddGhostButton ShrinkVertical" block
                  onClick={addGhostPlayer}><GhostIcon/> Add ghost
            player</Button>
        </div>
        {
          nonGhostPlayers >= 2 ?
              <div>
                <Button className="ReadyToStartButton" block
                        onClick={onReadyToStart}>Ready to start!</Button>
              </div>
              : <div>
                Need {lackingPlayerCount} more player{lackingPlayerCount === 1 ? ""
                  : "s"}
              </div>
        }
      </main>
  );
}