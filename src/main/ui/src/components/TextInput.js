import './TextInput.css';

function TextInput({className, ...others}) {
  return <input type="text" className={"TextInput "+className} {...others}/>
}

export default TextInput;



