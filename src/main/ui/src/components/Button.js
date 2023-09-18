import './Button.css';

/**
 *
 * @param children
 * @param block {boolean} display as block or span
 * @param className {string} extra class name to apply
 * @param onClick {function(MouseEvent):void}
 * @param other all other properties will be placed on the HTML button
 * @returns {JSX.Element}
 * @constructor
 */
function Button({children, block, className='', onClick, ...other}) {
  let extraClasses = "";
  if (block) {
    extraClasses = "block";
  }
  return <button className={`Button ${className} ${extraClasses}`} onClick={onClick} {...other}>{children}</button>;
}

export default Button;