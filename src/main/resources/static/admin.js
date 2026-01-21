(function () {
    const usernameKey = 'squares.username';
    const roleKey = 'squares.role';
    const username = localStorage.getItem(usernameKey);
    const role = localStorage.getItem(roleKey);
    if (!username || role !== 'admin') {
        const redirect = encodeURIComponent('/admin');
        window.location.href = `/admin-login?redirect=${redirect}`;
        return;
    }

    const adminTokenInput = document.getElementById('admin-token');
    const boardNameInput = document.getElementById('board-name');
    const fixtureSelect = document.getElementById('fixture-select');
    const sportTypeInput = document.getElementById('sport-type');
    const gameNameInput = document.getElementById('game-name');
    const gameIdInput = document.getElementById('game-id');
    const homeTeamInput = document.getElementById('home-team');
    const awayTeamInput = document.getElementById('away-team');
    const priceCentsInput = document.getElementById('price-cents');
    const housePercentInput = document.getElementById('house-percent');
    const minActivateInput = document.getElementById('min-activate');
    const payoutQ1Input = document.getElementById('payout-q1');
    const payoutQ2Input = document.getElementById('payout-q2');
    const payoutQ3Input = document.getElementById('payout-q3');
    const payoutQ4Input = document.getElementById('payout-q4');
    const boardIdInput = document.getElementById('board-id');
    const actionGameIdInput = document.getElementById('action-game-id');
    const homeScoreInput = document.getElementById('home-score');
    const awayScoreInput = document.getElementById('away-score');
    const gameClockInput = document.getElementById('game-clock');
    const quarterSelect = document.getElementById('quarter-select');
    const message = document.getElementById('admin-message');
    const showPurchaserNamesToggle = document.getElementById('show-purchaser-names');
    const rolloverToggle = document.getElementById('rollover-no-winner');
    const boardList = document.getElementById('admin-board-list');
    const fixtureEndpoint = '/api/fixtures';
    let fixtures = [];
    let boardsById = new Map();

    function applyPayoutDefaults(sportType) {
        const normalized = (sportType || '').trim().toLowerCase();
        if (normalized === 'soccer') {
            payoutQ1Input.value = 33.33;
            payoutQ2Input.value = 0;
            payoutQ3Input.value = 0;
            payoutQ4Input.value = 66.66;
            return;
        }
        payoutQ1Input.value = 12.5;
        payoutQ2Input.value = 25;
        payoutQ3Input.value = 12.5;
        payoutQ4Input.value = 50;
    }

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

    function notifySuccess(text) {
        message.textContent = text;
        window.alert(text);
    }

    function loadSettings() {
        fetch('/admin/settings', {
            headers: tokenHeader()
        })
            .then(handleResponse)
            .then(res => res.json())
            .then(settings => {
                showPurchaserNamesToggle.checked = settings.showPurchaserNames;
                if (rolloverToggle) {
                    rolloverToggle.checked = settings.rolloverOnNoWinner;
                }
            })
            .catch(err => {
                message.textContent = err.message;
            });
    }

    function renderFixtures(list) {
        fixtureSelect.innerHTML = '<option value="">Select a fixture</option>';
        if (!list.length) {
            return;
        }
        list.forEach((fixture, index) => {
            const option = document.createElement('option');
            const title = fixture?.title || `Fixture ${index + 1}`;
            option.value = title;
            option.textContent = title;
            option.dataset.index = String(index);
            fixtureSelect.appendChild(option);
        });
    }

    function extractFixtureList(data) {
        if (Array.isArray(data)) {
            return data;
        }
        if (data && Array.isArray(data.fixtures)) {
            return data.fixtures;
        }
        if (data && Array.isArray(data.games)) {
            return data.games;
        }
        if (data && Array.isArray(data.data)) {
            return data.data;
        }
        return [];
    }

    function loadFixtures() {
        fetch(fixtureEndpoint)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Unable to load fixtures.');
                }
                return response.json();
            })
            .then(data => {
                fixtures = extractFixtureList(data);
                renderFixtures(fixtures);
            })
            .catch(err => {
                message.textContent = err.message;
            });
    }

    function populateForm(board) {
        boardIdInput.value = board.id;
        boardNameInput.value = board.name || '';
        sportTypeInput.value = board.sportType || '';
        gameNameInput.value = board.gameName || '';
        gameIdInput.value = board.gameId || '';
        if (actionGameIdInput) {
            actionGameIdInput.value = board.gameId || '';
        }
        homeTeamInput.value = board.homeTeam || '';
        awayTeamInput.value = board.awayTeam || '';
        priceCentsInput.value = board.priceCents || 0;
        housePercentInput.value = board.housePercent || 0;
        minActivateInput.value = board.minSquaresToActivate || 0;
        payoutQ1Input.value = board.payoutQ1Percent ?? 0;
        payoutQ2Input.value = board.payoutQ2Percent ?? 0;
        payoutQ3Input.value = board.payoutQ3Percent ?? 0;
        payoutQ4Input.value = board.payoutQ4Percent ?? 0;
    }

    function boardHasWagers(board) {
        const totalSquares = board.totalSquares ?? 100;
        return (board.openSquares ?? totalSquares) < totalSquares;
    }

    function renderBoards(boards) {
        boardsById = new Map(boards.map(board => [String(board.id), board]));
        boardList.innerHTML = '';
        if (!boards.length) {
            boardList.innerHTML = '<p class="muted">No boards created yet.</p>';
            return;
        }
        boards.forEach(board => {
            const item = document.createElement('div');
            item.className = 'board-item-card';
            const openSquares = board.openSquares ?? 0;
            const hasWagers = boardHasWagers(board);
            item.innerHTML = `
                <div class="board-item-header">
                    <strong>${board.name}</strong>
                    <span class="status-pill">${board.status}</span>
                </div>
                <div class="muted small">${board.sportType || 'Sport'} • ${board.gameName || 'Game'}</div>
                <div class="muted small">Open squares: ${openSquares}</div>
                <div class="board-item-actions">
                    <button type="button" data-action="select">Select</button>
                    <button type="button" data-action="delete" class="secondary-btn" ${hasWagers ? 'disabled' : ''}>
                        ${hasWagers ? 'Remove (locked)' : 'Remove'}
                    </button>
                </div>
            `;
            item.querySelector('[data-action="select"]').addEventListener('click', () => {
                populateForm(board);
            });
            item.querySelector('[data-action="delete"]').addEventListener('click', () => {
                if (boardHasWagers(board)) {
                    message.textContent = 'Boards with wagers cannot be removed.';
                    return;
                }
                if (window.confirm(`Remove ${board.name}? This cannot be undone.`)) {
                    deleteBoard(board.id);
                }
            });
            boardList.appendChild(item);
        });
    }

    function loadBoards() {
        fetch('/admin/boards', { headers: tokenHeader() })
            .then(handleResponse)
            .then(res => res.json())
            .then(renderBoards)
            .catch(err => {
                message.textContent = err.message;
            });
    }

    function deleteBoard(boardId) {
        fetch(`/admin/boards/${boardId}`, {
            method: 'DELETE',
            headers: tokenHeader()
        })
            .then(handleResponse)
            .then(() => {
                notifySuccess('Board removed.');
                loadBoards();
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
                showPurchaserNames: showPurchaserNamesToggle.checked,
                rolloverOnNoWinner: rolloverToggle ? rolloverToggle.checked : false
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
        const sportType = (sportTypeInput.value || '').trim() || 'Football';
        const gameName = (gameNameInput.value || '').trim() || 'Matchup';
        fetch('/admin/boards', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', ...tokenHeader() },
            body: JSON.stringify({
                name: boardNameInput.value,
                sportType,
                gameName,
                gameId: gameIdInput.value,
                homeTeam: homeTeamInput.value,
                awayTeam: awayTeamInput.value,
                priceCents: Number(priceCentsInput.value),
                housePercent: Number(housePercentInput.value),
                minSquaresToActivate: Number(minActivateInput.value),
                payoutQ1Percent: Number(payoutQ1Input.value),
                payoutQ2Percent: Number(payoutQ2Input.value),
                payoutQ3Percent: Number(payoutQ3Input.value),
                payoutQ4Percent: Number(payoutQ4Input.value)
            })
        })
            .then(handleResponse)
            .then(res => res.json())
            .then(board => {
                notifySuccess(`Board created with ID ${board.id}.`);
                boardIdInput.value = board.id;
                gameIdInput.value = board.gameId || gameIdInput.value;
                if (actionGameIdInput) {
                    actionGameIdInput.value = board.gameId || actionGameIdInput.value;
                }
                loadBoards();
            })
            .catch(err => {
                message.textContent = err.message;
            });
    });

    document.getElementById('update-board').addEventListener('click', () => {
        const boardId = boardIdInput.value;
        if (!boardId) {
            message.textContent = 'Enter a board ID to update.';
            return;
        }
        fetch(`/admin/boards/${boardId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', ...tokenHeader() },
            body: JSON.stringify({
                name: boardNameInput.value,
                sportType: sportTypeInput.value,
                gameName: gameNameInput.value,
                gameId: gameIdInput.value,
                homeTeam: homeTeamInput.value,
                awayTeam: awayTeamInput.value,
                priceCents: Number(priceCentsInput.value),
                housePercent: Number(housePercentInput.value),
                minSquaresToActivate: Number(minActivateInput.value),
                payoutQ1Percent: Number(payoutQ1Input.value),
                payoutQ2Percent: Number(payoutQ2Input.value),
                payoutQ3Percent: Number(payoutQ3Input.value),
                payoutQ4Percent: Number(payoutQ4Input.value)
            })
        })
            .then(handleResponse)
            .then(res => res.json())
            .then(board => {
                notifySuccess('Board updated.');
                populateForm(board);
                if (actionGameIdInput) {
                    actionGameIdInput.value = board.gameId || actionGameIdInput.value;
                }
                loadBoards();
            })
            .catch(err => {
                message.textContent = err.message;
            });
    });

    if (sportTypeInput) {
        applyPayoutDefaults(sportTypeInput.value);
        sportTypeInput.addEventListener('change', () => applyPayoutDefaults(sportTypeInput.value));
        sportTypeInput.addEventListener('blur', () => applyPayoutDefaults(sportTypeInput.value));
    }

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
                awayScore: Number(awayScoreInput.value),
                gameClock: gameClockInput.value
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
        const quarterMap = {
            Q1: '1st',
            Q2: 'half',
            Q3: '3rd',
            Q4: 'final'
        };
        const quarterLabel = quarterMap[quarterSelect.value] || quarterSelect.value;
        const homeLabel = homeTeamInput.value || 'Home';
        const awayLabel = awayTeamInput.value || 'Away';
        const confirmMessage = [
            `You are about to payout ${quarterLabel} quarter for game ${homeLabel} vs ${awayLabel}.`,
            `Home: ${homeScoreInput.value}`,
            `Away: ${awayScoreInput.value}`
        ].join('\n');
        if (!window.confirm(confirmMessage)) {
            return;
        }
        fetch(`/admin/boards/${boardId}/confirm-quarter`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', ...tokenHeader() },
            body: JSON.stringify({
                quarter: quarterSelect.value
            })
        })
            .then(handleResponse)
            .then(() => {
                notifySuccess('Payout confirmed.');
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

    document.getElementById('delete-board').addEventListener('click', () => {
        const boardId = boardIdInput.value;
        if (!boardId) {
            message.textContent = 'Enter a board ID to delete.';
            return;
        }
        const board = boardsById.get(String(boardId));
        if (board && boardHasWagers(board)) {
            message.textContent = 'Boards with wagers cannot be removed.';
            return;
        }
        if (window.confirm(`Delete board ${boardId}? This cannot be undone.`)) {
            deleteBoard(boardId);
        }
    });

    fixtureSelect.addEventListener('change', () => {
        const selectedOption = fixtureSelect.options[fixtureSelect.selectedIndex];
        if (!selectedOption || selectedOption.value === '') {
            return;
        }
        const fixtureIndex = Number(selectedOption.dataset.index);
        const fixture = fixtures[fixtureIndex];
        if (!fixture) {
            return;
        }
        const homeTitle = fixture.homeTeam?.title || '';
        const awayTitle = fixture.visitingTeam?.title || '';
        const gameTitle = fixture.title || fixture.name || '';
        const sportTitle = fixture.sport?.title || fixture.sportType || '';
        homeTeamInput.value = homeTitle;
        awayTeamInput.value = awayTitle;
        if (gameTitle) {
            gameNameInput.value = gameTitle;
        }
        if (sportTitle) {
            sportTypeInput.value = sportTitle;
        }
    });

    loadSettings();
    loadBoards();
    loadFixtures();
})();
