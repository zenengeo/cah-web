import './Button.css';

function Button({children, block, className, onClick, ...other}) {
  let extraClasses = "";
  if (block) {
    extraClasses = "block";
  }
  return <button className={`Button ${className} ${extraClasses}`} onClick={onClick} {...other}>{children}</button>;
}

export default Button;