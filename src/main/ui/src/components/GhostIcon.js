import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faGhost} from '@fortawesome/free-solid-svg-icons'
import './GhostIcon.css'

function GhostIcon({size, className}) {
  return <FontAwesomeIcon icon={faGhost} className={"GhostIcon "+className} size={size} />
}

export default GhostIcon;