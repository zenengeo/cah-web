import './Player.css';
import { useCandidateCards, usePlayerCards } from '../../api/cards';
import { usePlayerWiring } from './data';
import { useCurrentBlackCard } from '../Host/data';
import ShowAndPick from './ShowAndPick';

function Waiting ({ text }) {
  return (
    <main className="Waiting">
      {text}
    </main>
  );
}

function PickCards ({ roomCode, round, playerId, handleSubmitCard }) {
  const whiteCards = usePlayerCards(roomCode, playerId);
  const blackCard = useCurrentBlackCard(roomCode, round);

  return (
    whiteCards && blackCard &&
      <ShowAndPick
        title="Pick your card"
        submitText="Pick"
        blackCard={blackCard}
        whiteCards={whiteCards}
        handleSubmit={handleSubmitCard}
      />
  );
}

function Vote ({ roomCode, round, handleVote, submittedCard }) {
  const candidates = useCandidateCards(roomCode, round);
  const blackCard = useCurrentBlackCard(roomCode, round);

  function annotate (card) {
    return card.id === submittedCard.id ? 'Yours' : null;
  }

  return (
    candidates && blackCard &&
      <ShowAndPick
        title="Vote for the b"
        submitText="Vote"
        blackCard={blackCard}
        whiteCards={candidates}
        handleSubmit={handleVote}
        annotate={annotate}
      />
  );
}

function Player ({ roomCode, playerId }) {
  const {
    state,
    round,
    handleSubmitCard,
    handleVote,
    submittedCard,
  } = usePlayerWiring(roomCode, playerId);

  switch (state) {
    default:
    case 'waiting_to_start':
      return <Waiting text="Waiting for game to start..."/>;

    case 'pick_cards':
      console.debug('pick_cards -> PickCards');
      return <PickCards roomCode={roomCode} round={round} playerId={playerId}
                        handleSubmitCard={handleSubmitCard}/>;
    case 'picked_card':
      console.debug('picked_card -> Waiting');
      return <Waiting text="Waiting for others to pick..."/>;

    case 'voting':
      console.debug('voting -> Vote');
      return <Vote roomCode={roomCode} round={round} handleVote={handleVote}
                   submittedCard={submittedCard}/>;
    case 'voted':
      return <Waiting text="Waiting for others to vote..."/>;

    case 'winner_revealed':
      return <Waiting text="Round finished. Waiting on host..."/>;
  }
}

export default Player;