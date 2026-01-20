(function () {
    const form = document.getElementById('login-form');
    const usernameInput = document.getElementById('login-username');
    const codeInput = document.getElementById('login-code');
    const message = document.getElementById('login-message');
    const usernameKey = 'squares.username';

    const params = new URLSearchParams(window.location.search);
    const redirect = params.get('redirect') || '/';

    form.addEventListener('submit', event => {
        event.preventDefault();
        const username = usernameInput.value.trim();
        const code = codeInput.value.trim();
        if (!username) {
            message.textContent = 'Enter a display name to continue.';
            return;
        }
        if (!code) {
            message.textContent = 'Enter any numeric access code.';
            return;
        }
        localStorage.setItem(usernameKey, username);
        window.location.href = redirect;
    });
})();
