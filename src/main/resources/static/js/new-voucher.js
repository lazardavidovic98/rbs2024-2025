'use strict';

(function () {
    // Client-side check for convenience only; the server enforces the same rules
    document.getElementById('voucherForm').addEventListener('submit', function (event) {
        const code = document.getElementById('code').value;
        const value = parseInt(document.getElementById('value').value, 10);

        const codeValid = /^\d{4}[a-zA-Z0-9._-]+\d{4}$/.test(code);
        const valueValid = !isNaN(value) && value >= 1 && value <= 95;

        document.getElementById('codeError').classList.toggle('visible', !codeValid);
        document.getElementById('valueError').classList.toggle('visible', !valueValid);

        if (!codeValid || !valueValid) {
            event.preventDefault();
        }
    });
})();
