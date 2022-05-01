/**
 * @param {string} url
 */
export function getJson(url) {
  return fetch(url, {
    headers: {
      'Accept': 'application/json'
    }
  })
      .then(resp => {
        if (resp.ok) {
          return resp.json();
        } else {
          return resp.json()
              .then(json => {
                throw json;
              });
        }
      });
}

export function postJson(url, obj) {
  const payloadOptions = obj ? {
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(obj)
  } : {};

  return fetch(url, {
    method: "POST",
    ...payloadOptions
  })
      .then(response => {
        if (response.ok) {
          if (response.status === 204) {
            return {};
          }
          return response.json();
        }
        else {
          // get error details provided by Spring Boot
          return response.json()
              .then(json => {
                throw json
              });
        }
      });
}