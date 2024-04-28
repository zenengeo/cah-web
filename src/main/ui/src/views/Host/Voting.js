import { useCurrentBlackCard } from './data';
import BlackCard from '../../components/BlackCard';

export function Voting({roomCode, round}) {
  const blackCard = useCurrentBlackCard(roomCode, round);

  return (
      <main className="Voting">
        <div className="VoteNow">Vote now!</div>
        <div>{blackCard && <BlackCard text={blackCard.text}/>}</div>
      </main>
  );
}