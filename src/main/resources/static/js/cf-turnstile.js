
function onCfTurnstileSuccess(token) {
  getSubmitButton().disabled = false
}

function onCfTurnstileExpired() {
  getSubmitButton().disabled = true
}

function getSubmitButton() {
  return document.querySelector(".cf-turnstile")
    ?.closest("form")
    ?.querySelector('[type="submit"]') ?? null
}

// getSubmitButton().disabled = true
