import './Lobby.css';
import {useState} from "react";
import TextInput from "../components/TextInput";
import Button from "../components/Button";

function Choosing({handleJoin, handleHost}) {
  return (
      <div className="Choices">
        <Button className="ChoiceButton" onClick={handleHost}>Host</Button>
        <div className="ChoiceDivider">or</div>
        <Button className="ChoiceButton" onClick={handleJoin}>Join</Button>
      </div>
  )
}

function EnterDetails({startAsPlayer}) {
  const [name, setName] = useState("");
  const [code, setCode] = useState("");

  function handleSubmit(evt) {
    evt.preventDefault();

    startAsPlayer(name, code);
  }

  function handleCodeChange(evt) {
    setCode(evt.target.value.toUpperCase());
  }

  return (
      <form className="Details" onSubmit={handleSubmit}>
        <div className="JoinField">
          <label className="JoinLabel block" htmlFor="name">Your name</label>
          <TextInput size={4} autoFocus className="JoinInput" id="name" required minLength={1} value={name} onChange={evt => setName(evt.target.value)}/>
        </div>
        <div className="JoinField">
          <label className="JoinLabel block" htmlFor="code">Room code</label>
          <TextInput size={4} className="JoinInput" id="code"
                     required pattern="[A-Z]{4}" maxLength={4}
                     autoComplete={false} autoCorrect={false} spellCheck={false}
                     value={code}
                     onChange={handleCodeChange}
          />
        </div>

        <Button className="JoinButton" type="submit">Join</Button>
      </form>
  )
}

function Lobby({startAsHost, startAsPlayer}) {
  const [joining, setJoining] = useState(false);

  const enterDetails = <EnterDetails startAsPlayer={startAsPlayer} />;
  const choosing = <Choosing handleHost={startAsHost} handleJoin={() => setJoining(true)}/>;

  return (
      <main className="Lobby">
        { joining ? enterDetails : choosing }
      </main>
  );
}

export default Lobby;