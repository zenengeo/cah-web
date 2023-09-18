import './Lobby.css';
import { useEffect, useState } from 'react';
import TextInput from '../components/TextInput';
import Button from '../components/Button';
import { getJson, useClickOnce } from '../utils/fetchWrappers';

function CrossFade({text}) {
  const [textToRender, setTextToRender] = useState(text);
  const [show, setShow] = useState(text != null);

  useEffect(() => {
    if (text !== textToRender) {
      setShow(false);
    }
  }, [text, textToRender]);

  function transitionEnded() {
    if (text) {
      setTextToRender(text);
      setShow(true);
    }
  }

  let classes = "CrossFade " + (show ? "CrossFadeShow" : "CrossFadeHide");

  return <div className={classes} onTransitionEnd={transitionEnded}>{textToRender}</div>
}

function useTimedNameSwap(name, setName) {
  useEffect(() => {
    const id = setTimeout(() => {
      getJson('/cards/randomNameCards?count=1')
          .then(resp => {
            setName(resp.contents[0].text);
          })
    }, Math.random()*4000 + 6000);

    return () => {
      clearTimeout(id);
    }
  }, [name, setName])
}

/**
 *
 * @param handleJoin {function():void}
 * @param handleHost {function():void}
 * @param handleBrowseCards {function():void}
 * @returns {JSX.Element}
 * @constructor
 */
function Choosing({handleJoin, handleHost, handleBrowseCards}) {
  const [clicked, clickWrapper] = useClickOnce();
  const [upper, setUpper] = useState("Clones");
  const [lower, setLower] = useState("Humanity");
  useTimedNameSwap(upper, setUpper);
  useTimedNameSwap(lower, setLower);

  return (
      <div className="Choosing">
        <h1 className="Title">
          <CrossFade text={upper} />
          <div className="NamesAgainst">Against</div>
          <CrossFade text={lower} />
        </h1>
        <div className="Choices">
          <Button disabled={clicked} className="ChoiceButton" onClick={clickWrapper(handleHost)}>Host</Button>
          <div className="ChoiceDivider">or</div>
          <Button disabled={clicked} className="ChoiceButton" onClick={clickWrapper(handleJoin)}>Join</Button>
        </div>
        <Button block={true} className="BrowseButton" onClick={handleBrowseCards}>Browse the Cards</Button>
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
          <TextInput size={6} autoFocus className="JoinInput" id="name" required minLength={1} value={name} onChange={evt => setName(evt.target.value)}/>
        </div>
        <div className="JoinField">
          <label className="JoinLabel block" htmlFor="code">Room code</label>
          <TextInput size={6} className="JoinInput" id="code"
                     required pattern="[A-Z]{4}" maxLength={4}
                     autoComplete="false" autoCorrect="false" spellCheck="false"
                     value={code}
                     onChange={handleCodeChange}
          />
        </div>

        <Button className="JoinButton" type="submit">Join</Button>
      </form>
  )
}

function Lobby({startAsHost, startAsPlayer, startBrowsingCards}) {
  const [joining, setJoining] = useState(false);

  const enterDetails = <EnterDetails startAsPlayer={startAsPlayer} />;
  const choosing = <Choosing
    handleHost={startAsHost}
    handleJoin={() => setJoining(true)}
    handleBrowseCards={startBrowsingCards}
  />;

  return (
      <main className="Lobby">
        { joining ? enterDetails : choosing }
      </main>
  );
}

export default Lobby;