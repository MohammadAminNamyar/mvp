(function () {
    const sportList = document.getElementById('sport-list');
    const gamesList = document.getElementById('games-list');

    let selectedSport = null;

    function fetchJson(url) {
        return fetch(url).then(response => {
            if (!response.ok) {
                throw new Error('Request failed');
            }
            return response.json();
        });
    }

    function formatBetLabel(option) {
        if (option.label) {
            return option.label;
        }
        return `$${(option.cents / 100).toFixed(0)}`;
    }

    function renderSports(sports) {
        sportList.innerHTML = '';
        if (!sports.length) {
            sportList.innerHTML = '<p class="muted">No sports are available yet.</p>';
            return;
        }
        sports.forEach(sport => {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'sport-button';
            button.textContent = sport;
            if (sport === selectedSport) {
                button.classList.add('active');
            }
            button.addEventListener('click', () => {
                selectedSport = sport;
                renderSports(sports);
                loadGames();
            });
            sportList.appendChild(button);
        });
    }

    function renderEmptyGamesMessage() {
        gamesList.innerHTML =
            '<p class="empty-state">We’re sorry, the buzzer has sounded and there are no available boards at this time.</p>';
    }

    function renderGames(games) {
        gamesList.innerHTML = '';
        if (!games.length) {
            renderEmptyGamesMessage();
            return;
        }
        games.forEach(game => {
            const card = document.createElement('div');
            card.className = 'game-card';

            const header = document.createElement('div');
            header.className = 'game-header';
            header.innerHTML = `
                <div>
                    <h3>${game.name}</h3>
                    <p class="muted small">${game.awayTeam} at ${game.homeTeam}</p>
                </div>
            `;

            const betRow = document.createElement('div');
            betRow.className = 'bet-row';
            const betLabel = document.createElement('span');
            betLabel.className = 'muted small';
            betLabel.textContent = 'Select bet amount:';
            betRow.appendChild(betLabel);

            const betButtons = document.createElement('div');
            betButtons.className = 'bet-buttons';

            const boardsContainer = document.createElement('div');
            boardsContainer.className = 'boards-container';

            betRow.appendChild(betButtons);
            card.appendChild(header);
            card.appendChild(betRow);
            card.appendChild(boardsContainer);
            gamesList.appendChild(card);

            loadBetOptions(game, betButtons, boardsContainer);
        });
    }

    function renderBoards(boards, container) {
        container.innerHTML = '';
        if (!boards.length) {
            container.innerHTML = '<p class="muted">No boards for this bet level yet.</p>';
            return;
        }
        const list = document.createElement('div');
        list.className = 'board-list';
        boards.forEach(board => {
            const item = document.createElement('a');
            item.href = `/boards/${board.id}/view`;
            item.className = 'board-row';
            item.innerHTML = `
                <div>
                    <strong>${board.name}</strong>
                    <span class="muted small">Open squares: ${board.openSquares}</span>
                </div>
                <span class="status-pill ${board.full ? 'danger' : 'success'}">
                    ${board.full ? 'Full' : 'Open'}
                </span>
            `;
            list.appendChild(item);
        });
        container.appendChild(list);
    }

    function loadBoards(gameId, betCents, container) {
        fetchJson(`/api/lobby/games/${encodeURIComponent(gameId)}/boards?betCents=${betCents}`)
            .then(boards => renderBoards(boards, container))
            .catch(() => {
                container.innerHTML = '<p class="muted">Unable to load boards right now.</p>';
            });
    }

    function loadBetOptions(game, betButtons, boardsContainer) {
        fetchJson(`/api/lobby/games/${encodeURIComponent(game.gameId)}/bets`)
            .then(options => {
                betButtons.innerHTML = '';
                if (!options.length) {
                    boardsContainer.innerHTML = '<p class="muted">No bet amounts configured for this game.</p>';
                    return;
                }
                options.forEach((option, index) => {
                    const betButton = document.createElement('button');
                    betButton.type = 'button';
                    betButton.className = 'bet-button';
                    betButton.textContent = formatBetLabel(option);
                    betButton.addEventListener('click', () => {
                        betButtons.querySelectorAll('.bet-button').forEach(btn => btn.classList.remove('active'));
                        betButton.classList.add('active');
                        loadBoards(game.gameId, option.cents, boardsContainer);
                    });
                    if (index === 0) {
                        betButton.classList.add('active');
                    }
                    betButtons.appendChild(betButton);
                });
                loadBoards(game.gameId, options[0].cents, boardsContainer);
            })
            .catch(() => {
                boardsContainer.innerHTML = '<p class="muted">Unable to load bet options right now.</p>';
            });
    }

    function loadGames() {
        if (!selectedSport) {
            gamesList.innerHTML = '';
            return;
        }
        fetchJson(`/api/lobby/sports/${encodeURIComponent(selectedSport)}/games`)
            .then(renderGames)
            .catch(() => {
                renderEmptyGamesMessage();
            });
    }

    function init() {
        fetchJson('/api/lobby/sports')
            .then(sports => {
                selectedSport = sports[0] || null;
                renderSports(sports);
                if (selectedSport) {
                    loadGames();
                }
            })
            .catch(() => {
                renderSports([]);
                renderEmptyGamesMessage();
            });
    }

    init();
})();
