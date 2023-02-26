import {useCurrentBlackCard, useScores, useWinners} from "./data";
import BlackCard from "../../components/BlackCard";
import GhostIcon from "../../components/GhostIcon";
import WhiteCard from "../../components/WhiteCard";
import Button from "../../components/Button";

export function Reveal({roomCode, round, startNewRound}) {
  const blackCard = useCurrentBlackCard(roomCode, round);
  const winners = useWinners(roomCode, round);
  const scores = useScores(roomCode, round);

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
                      Played
                      by {winner.playedBy.playerName}{winner.playedBy.ghost &&
                        <GhostIcon
                            className="GhostPlayer"/>} with {winner.votes} vote{winner.votes
                    > 1 ? "s" : ""}
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
                  <div key={playerScore.info.playerId}
                       className="PlayerScore FlexColumn TextCenter">
                    <div className="PlayerScoreName">
                      {playerScore.info.playerName} {playerScore.info.ghost &&
                        <GhostIcon/>}
                    </div>
                    <div>{playerScore.score}</div>
                  </div>
              )
            }
          </div>
        </div>
        <div className="FlexRow FlexJustifyCenter">
          <Button className="StartNewRoundButton ShrinkVertical" autoFocus
                  onClick={startNewRound}>Start New Round</Button>
        </div>
      </main>
  );

}