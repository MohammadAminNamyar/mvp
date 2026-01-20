(function () {
    const adminTokenInput = document.getElementById('admin-token');
    const boardNameInput = document.getElementById('board-name');
    const homeTeamInput = document.getElementById('home-team');
    const awayTeamInput = document.getElementById('away-team');
    const priceCentsInput = document.getElementById('price-cents');
    const housePercentInput = document.getElementById('house-percent');
    const minActivateInput = document.getElementById('min-activate');
    const boardIdInput = document.getElementById('board-id');
    const homeScoreInput = document.getElementById('home-score');
    const awayScoreInput = document.getElementById('away-score');
    const quarterSelect = document.getElementById('quarter-select');
    const message = document.getElementById('admin-message');

    function tokenHeader() {
        return { 'X-Admin-Token': adminTokenInput.value.trim() };
    }

    function handleResponse(response) {
        if (!response.ok) {
            return response.json().then(data => {
                throw new Error(data.error || 'Request failed');
            });
        }
        return response;
    }

    document.getElementById('create-board').addEventListener('click', () => {
        fetch('/admin/boards', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', ...tokenHeader() },
            body: JSON.stringify({
                name: boardNameInput.value,
                homeTeam: homeTeamInput.value,
                awayTeam: awayTeamInput.value,
                priceCents: Number(priceCentsInput.value),
                housePercent: Number(housePercentInput.value),
                minSquaresToActivate: Number(minActivateInput.value)
            })
        })
            .then(handleResponse)
            .then(res => res.json())
            .then(board => {
                message.textContent = `Board created with ID ${board.id}`;
                boardIdInput.value = board.id;
            })
            .catch(err => {
                message.textContent = err.message;
            });
    });

    document.getElementById('start-game').addEventListener('click', () => {
        const boardId = boardIdInput.value;
        fetch(`/admin/boards/${boardId}/start`, {
            method: 'POST',
            headers: tokenHeader()
        })
            .then(handleResponse)
            .then(() => {
                message.textContent = 'Game started.';
            })
            .catch(err => {
                message.textContent = err.message;
            });
    });

    document.getElementById('update-score').addEventListener('click', () => {
        const boardId = boardIdInput.value;
        fetch(`/admin/boards/${boardId}/score`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', ...tokenHeader() },
            body: JSON.stringify({
                homeScore: Number(homeScoreInput.value),
                awayScore: Number(awayScoreInput.value)
            })
        })
            .then(handleResponse)
            .then(() => {
                message.textContent = 'Score updated.';
            })
            .catch(err => {
                message.textContent = err.message;
            });
    });

    document.getElementById('confirm-quarter').addEventListener('click', () => {
        const boardId = boardIdInput.value;
        fetch(`/admin/boards/${boardId}/confirm-quarter`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', ...tokenHeader() },
            body: JSON.stringify({
                quarter: quarterSelect.value
            })
        })
            .then(handleResponse)
            .then(() => {
                message.textContent = 'Quarter confirmed.';
            })
            .catch(err => {
                message.textContent = err.message;
            });
    });

    document.getElementById('reset-board').addEventListener('click', () => {
        const boardId = boardIdInput.value;
        fetch(`/admin/boards/${boardId}/reset`, {
            method: 'POST',
            headers: tokenHeader()
        })
            .then(handleResponse)
            .then(() => {
                message.textContent = 'Board reset.';
            })
            .catch(err => {
                message.textContent = err.message;
            });
    });
})();
