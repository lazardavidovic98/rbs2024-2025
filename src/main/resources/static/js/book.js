'use strict';

(function () {
    const bookId = Number(document.getElementById('book').dataset.bookId);

    function postJson(url, body) {
        return secureFetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
    }

    const rateButton = document.getElementById('rateBook');
    if (rateButton) {
        rateButton.addEventListener('click', function () {
            const selected = document.querySelector('input[name="rating"]:checked');
            if (!selected) {
                return;
            }
            postJson('/ratings', { bookId: bookId, rating: Number(selected.value) })
                .then(function () {
                    window.location.reload();
                });
        });
    }

    const commentButton = document.getElementById('createComment');
    if (commentButton) {
        commentButton.addEventListener('click', function () {
            const comment = document.getElementById('addComment').value;
            const error = document.getElementById('commentError');
            error.classList.remove('visible');

            postJson('/comments', { bookId: bookId, comment: comment })
                .then(function (response) {
                    if (response.ok) {
                        window.location.reload();
                        return;
                    }
                    error.textContent = 'The comment must contain between 1 and 500 characters.';
                    error.classList.add('visible');
                });
        });
    }
})();
