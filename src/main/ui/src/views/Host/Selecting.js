import {useCurrentBlackCard} from "./data";
import BlackCard from "../../components/BlackCard";

export function Selecting({roomCode, round}) {
  const blackCard = useCurrentBlackCard(roomCode, round);

  return (
      <main className="Selecting">
        {blackCard && <BlackCard text={blackCard.text}/>}
      </main>
  );

}