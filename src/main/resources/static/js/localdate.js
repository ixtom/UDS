document.querySelectorAll(".convert-to-local-date").forEach(function (element) {
    element.textContent = new Intl.DateTimeFormat(undefined, {
        month: "short",
        day: "numeric",
        year: "numeric",
        hour: "numeric",
        minute: "2-digit",
    }).format(new Date(element.dateTime));
});