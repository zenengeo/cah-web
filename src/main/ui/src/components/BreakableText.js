import {useMemo} from "react";

function BreakableText({text}) {
  const parts = useMemo(() => {
    return text.split('\n');
  }, [text]);

  return (
      <>
        {
          parts.map((part, index) =>
              <span key={index}>
              {part}
                {index < parts.length - 1 && <br/>}
            </span>
          )
        }
      </>
  )
}

export default BreakableText;