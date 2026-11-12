'use strict';

(function () {
    const form = document.getElementById('buyBookForm');
    if (!form) {
        return;
    }

    // Client-side check for convenience only; the server validates the request again
    form.addEventListener('submit', function (event) {
        const card = document.getElementById('card').value;
        const cardError = document.getElementById('cardError');
        const valid = /^\d{16}$/.test(card);

        cardError.classList.toggle('visible', !valid);
        if (!valid) {
            event.preventDefault();
        }
    });
})();
