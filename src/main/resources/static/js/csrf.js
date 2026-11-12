'use strict';

/**
 * Spring Security (CookieCsrfTokenRepository) stores the CSRF token in the XSRF-TOKEN cookie
 * and expects it back in the X-XSRF-TOKEN header on every state-changing request.
 */
function getCsrfToken() {
    const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/);
    return match ? decodeURIComponent(match[1]) : '';
}

/**
 * fetch() wrapper for same-origin JSON requests that always sends the CSRF header.
 */
function secureFetch(url, options) {
    const settings = Object.assign({ method: 'GET', credentials: 'same-origin' }, options || {});
    settings.headers = Object.assign({ 'X-XSRF-TOKEN': getCsrfToken() }, settings.headers || {});
    return fetch(url, settings);
}
