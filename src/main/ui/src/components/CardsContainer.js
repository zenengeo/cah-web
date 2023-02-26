import './CardsContainer.css'

export default function CardsContainer({children}) {
  return (
      <div className="CardsContainer">
        <div className="CardsContainerContent">
          {children}
        </div>
      </div>
  )
}