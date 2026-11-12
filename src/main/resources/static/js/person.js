'use strict';

(function () {
    document.getElementById('deleteUser').addEventListener('click', function () {
        const personId = document.getElementById('id').value;

        // DELETE is state changing, so it carries the CSRF header like every other write
        secureFetch('/persons/' + encodeURIComponent(personId), { method: 'DELETE' })
            .then(function (response) {
                if (response.redirected) {
                    window.location.href = response.url;
                } else {
                    window.location.href = response.ok ? '/persons' : window.location.href;
                }
            });
    });
})();
