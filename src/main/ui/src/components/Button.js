import './Button.css';
import { forwardRef } from 'react';

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
const Button = forwardRef(function Button(
  {children, block, className='', onClick, ...other},
  ref
) {
  let extraClasses = "";
  if (block) {
    extraClasses = "block";
  }
  return <button className={`Button ${className} ${extraClasses}`} onClick={onClick}
                 {...other} ref={ref}>{children}</button>;
});

export default Button;