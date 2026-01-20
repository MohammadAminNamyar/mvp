(function () {
    const usernameKey = 'squares.username';
    const username = localStorage.getItem(usernameKey);
    if (!username) {
        const redirect = encodeURIComponent('/admin');
        window.location.href = `/admin-login?redirect=${redirect}`;
        return;
    }

    const adminTokenInput = document.getElementById('admin-token');
    const boardNameInput = document.getElementById('board-name');
    const sportTypeInput = document.getElementById('sport-type');
    const gameNameInput = document.getElementById('game-name');
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
    const showPurchaserNamesToggle = document.getElementById('show-purchaser-names');
    const boardList = document.getElementById('board-list');
    const refreshBoardsButton = document.getElementById('refresh-boards');
    const updateBoardButton = document.getElementById('update-board');
    const deleteBoardButton = document.getElementById('delete-board');

    function tokenHeader() {
        return { 'X-Admin-Token': adminTokenInput.value.trim() };
    }

    function clearChildren(element) {
        while (element.firstChild) {
            element.removeChild(element.firstChild);
        }
    }

    function handleResponse(response) {
        if (!response.ok) {
            return response.json().then(data => {
                throw new Error(data.error || 'Request failed');
            });
        }
        return response;
    }

    function notifySuccess(text) {
        message.textContent = text;
        window.alert(text);
    }

    function selectBoard(board) {
        boardIdInput.value = board.id;
        boardNameInput.value = board.name || '';
        sportTypeInput.value = board.sportType || '';
        gameNameInput.value = board.gameName || '';
        homeTeamInput.value = board.homeTeam || '';
        awayTeamInput.value = board.awayTeam || '';
        priceCentsInput.value = board.priceCents || 0;
        housePercentInput.value = board.housePercent || 0;
        minActivateInput.value = board.minSquaresToActivate || 0;
        const cards = boardList.querySelectorAll('.admin-board-card');
        cards.forEach(card => {
            card.classList.toggle('active', card.dataset.boardId === String(board.id));
        });
    }

    function renderBoards(boards) {
        clearChildren(boardList);
        if (boards.length === 0) {
            boardList.textContent = 'No boards have been created yet.';
            return;
        }
        const grid = document.createElement('div');
        grid.className = 'admin-board-grid';
        boards.forEach(board => {
            const card = document.createElement('button');
            card.type = 'button';
            card.className = 'admin-board-card';
            card.dataset.boardId = board.id;

            const title = document.createElement('div');
            title.className = 'admin-board-title';
            title.textContent = `${board.name} (#${board.id})`;

            const meta = document.createElement('div');
            meta.className = 'muted small';
            const gameName = board.gameName || 'Matchup';
            const sport = board.sportType || 'General';
            meta.textContent = `${sport} • ${gameName} • $${(board.priceCents / 100).toFixed(2)}`;

            const status = document.createElement('div');
            status.className = 'admin-board-status';
            status.textContent = board.status;

            card.appendChild(title);
            card.appendChild(meta);
            card.appendChild(status);
            card.addEventListener('click', () => selectBoard(board));
            grid.appendChild(card);
        });
        boardList.appendChild(grid);
    }

    function loadBoards() {
        fetch('/admin/boards', {
            headers: tokenHeader()
        })
            .then(handleResponse)
            .then(res => res.json())
            .then(renderBoards)
            .catch(err => {
                boardList.textContent = err.message;
            });
    }

    function loadSettings() {
        fetch('/admin/settings', {
            headers: tokenHeader()
        })
            .then(handleResponse)
            .then(res => res.json())
            .then(settings => {
                showPurchaserNamesToggle.checked = settings.showPurchaserNames;
            })
            .catch(err => {
                message.textContent = err.message;
            });
    }

    document.getElementById('save-settings').addEventListener('click', () => {
        fetch('/admin/settings', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', ...tokenHeader() },
            body: JSON.stringify({
                showPurchaserNames: showPurchaserNamesToggle.checked
            })
        })
            .then(handleResponse)
            .then(() => {
                notifySuccess('Visibility settings saved.');
            })
            .catch(err => {
                message.textContent = err.message;
            });
    });

    document.getElementById('create-board').addEventListener('click', () => {
        fetch('/admin/boards', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', ...tokenHeader() },
            body: JSON.stringify({
                name: boardNameInput.value,
                sportType: sportTypeInput.value,
                gameName: gameNameInput.value,
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
                notifySuccess(`Board created with ID ${board.id}.`);
                boardIdInput.value = board.id;
                loadBoards();
            })
            .catch(err => {
                message.textContent = err.message;
            });
    });

    updateBoardButton.addEventListener('click', () => {
        const boardId = boardIdInput.value;
        if (!boardId) {
            message.textContent = 'Select a board to update.';
            return;
        }
        fetch(`/admin/boards/${boardId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', ...tokenHeader() },
            body: JSON.stringify({
                name: boardNameInput.value,
                sportType: sportTypeInput.value,
                gameName: gameNameInput.value,
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
                notifySuccess(`Board ${board.id} updated.`);
                loadBoards();
                selectBoard(board);
            })
            .catch(err => {
                message.textContent = err.message;
            });
    });

    deleteBoardButton.addEventListener('click', () => {
        const boardId = boardIdInput.value;
        if (!boardId) {
            message.textContent = 'Select a board to remove.';
            return;
        }
        if (!window.confirm('Remove this board and all of its squares? This cannot be undone.')) {
            return;
        }
        fetch(`/admin/boards/${boardId}`, {
            method: 'DELETE',
            headers: tokenHeader()
        })
            .then(handleResponse)
            .then(() => {
                notifySuccess(`Board ${boardId} removed.`);
                boardIdInput.value = '';
                boardNameInput.value = '';
                sportTypeInput.value = '';
                gameNameInput.value = '';
                homeTeamInput.value = '';
                awayTeamInput.value = '';
                priceCentsInput.value = '';
                housePercentInput.value = '';
                minActivateInput.value = '';
                loadBoards();
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
                notifySuccess('Game started.');
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
                notifySuccess('Score updated.');
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
                notifySuccess('Quarter confirmed.');
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
                notifySuccess('Board reset.');
            })
            .catch(err => {
                message.textContent = err.message;
            });
    });

    refreshBoardsButton.addEventListener('click', () => {
        loadBoards();
    });

    loadSettings();
    loadBoards();
})();
