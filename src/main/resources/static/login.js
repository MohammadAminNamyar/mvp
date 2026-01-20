(function () {
    const form = document.getElementById('login-form');
    const usernameInput = document.getElementById('login-username');
    const passwordInput = document.getElementById('login-password');
    const message = document.getElementById('login-message');
    const submitButton = form.querySelector('button[type="submit"]');
    const usernameKey = 'squares.username';
    const loginEndpoint = '/api/auth/login';

    const params = new URLSearchParams(window.location.search);
    const redirect = params.get('redirect') || '/';

    form.addEventListener('submit', async event => {
        event.preventDefault();
        const username = usernameInput.value.trim();
        const password = passwordInput.value.trim();
        if (!username) {
            message.textContent = 'Enter your username to continue.';
            return;
        }
        if (!password) {
            message.textContent = 'Enter your password to continue.';
            return;
        }

        message.textContent = 'Authenticating...';
        submitButton.disabled = true;

        try {
            const response = await fetch(loginEndpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, password }),
            });

            if (!response.ok) {
                message.textContent = 'Authentication failed. Check your credentials and try again.';
                return;
            }

            localStorage.setItem(usernameKey, username);
            window.location.href = redirect;
        } catch (error) {
            message.textContent = 'Unable to reach the authentication service right now.';
        } finally {
            submitButton.disabled = false;
        }
    });
})();
