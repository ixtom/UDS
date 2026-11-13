function formatAll(selector, options) {
    document.querySelectorAll(selector).forEach(function (element) {
        element.textContent = new Intl.DateTimeFormat(undefined, options)
            .format(new Date(element.dateTime));
    });
}

formatAll(".convert-to-local-date", {
    month: "short",
    day: "numeric",
    year: "numeric",
});

formatAll(".convert-to-local-date-and-time", {
    month: "short",
    day: "numeric",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
});