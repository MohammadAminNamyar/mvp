(() => {
    const sportList = document.getElementById('sport-list');
    const gamesList = document.getElementById('games-list');
    const gamesEmpty = document.getElementById('games-empty');

    const state = {
        sports: [],
        bets: [],
        selectedSport: null,
        selectedBets: new Map()
    };

    function fetchJson(url) {
        return fetch(url).then(response => {
            if (!response.ok) {
                throw new Error('Request failed');
            }
            return response.json();
        });
    }

    function clearChildren(element) {
        while (element.firstChild) {
            element.removeChild(element.firstChild);
        }
    }

    function renderSports() {
        clearChildren(sportList);
        if (state.sports.length === 0) {
            sportList.textContent = 'No sports are available yet.';
            return;
        }
        state.sports.forEach((sport, index) => {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'chip';
            button.textContent = sport.name;
            if (sport.name === state.selectedSport || (!state.selectedSport && index === 0)) {
                button.classList.add('active');
            }
            button.addEventListener('click', () => selectSport(sport.name));
            sportList.appendChild(button);
        });
    }

    function selectSport(sport) {
        state.selectedSport = sport;
        state.selectedBets.clear();
        renderSports();
        loadGames();
    }

    function renderGames(games) {
        clearChildren(gamesList);
        gamesEmpty.textContent = '';

        if (games.length === 0) {
            gamesEmpty.textContent = "We’re sorry, the buzzer has sounded and there are no available boards at this time.";
            return;
        }

        games.forEach(game => {
            const card = document.createElement('div');
            card.className = 'game-card';

            const header = document.createElement('div');
            header.className = 'game-header';

            const titleWrap = document.createElement('div');
            const title = document.createElement('h3');
            title.textContent = game.name;
            const subtitle = document.createElement('p');
            subtitle.className = 'muted small';
            const awayTeam = game.awayTeam || 'Away';
            const homeTeam = game.homeTeam || 'Home';
            subtitle.textContent = `${awayTeam} at ${homeTeam}`;
            titleWrap.appendChild(title);
            titleWrap.appendChild(subtitle);
            header.appendChild(titleWrap);

            const betRow = document.createElement('div');
            betRow.className = 'bet-options';
            const betLabel = document.createElement('div');
            betLabel.className = 'muted small';
            betLabel.textContent = 'Select a bet amount';
            betRow.appendChild(betLabel);

            const betList = document.createElement('div');
            betList.className = 'chip-list';
            state.bets.forEach(bet => {
                const betButton = document.createElement('button');
                betButton.type = 'button';
                betButton.className = 'chip';
                betButton.textContent = bet.label;
                betButton.dataset.amountCents = String(bet.amountCents);
                betButton.addEventListener('click', () => {
                    state.selectedBets.set(game.name, bet.amountCents);
                    highlightSelectedBet(betList, bet.amountCents);
                    loadBoards(game.name, bet.amountCents, boardList);
                });
                betList.appendChild(betButton);
            });
            betRow.appendChild(betList);

            const boardList = document.createElement('div');
            boardList.className = 'board-list';

            card.appendChild(header);
            card.appendChild(betRow);
            card.appendChild(boardList);

            gamesList.appendChild(card);
        });
    }

    function highlightSelectedBet(betList, amountCents) {
        Array.from(betList.children).forEach(child => {
            child.classList.toggle('active', child.dataset.amountCents === String(amountCents));
        });
    }

    function renderBoards(container, boards) {
        clearChildren(container);
        if (boards.length === 0) {
            const empty = document.createElement('p');
            empty.className = 'notice';
            empty.textContent = 'No boards yet. Check back soon.';
            container.appendChild(empty);
            return;
        }

        const grid = document.createElement('div');
        grid.className = 'board-grid';
        boards.forEach(board => {
            const card = document.createElement('a');
            card.className = 'board-card';
            card.href = `/boards/${board.id}/view`;

            const name = document.createElement('div');
            name.className = 'board-title';
            name.textContent = board.name;

            const status = document.createElement('div');
            status.className = 'board-status';
            status.textContent = board.full
                ? 'Full'
                : `${board.openSquares} open`;

            const pill = document.createElement('span');
            pill.className = board.full ? 'status-pill warn' : 'status-pill glow';
            pill.textContent = board.full ? 'FULL' : 'OPEN';

            card.appendChild(name);
            card.appendChild(status);
            card.appendChild(pill);
            grid.appendChild(card);
        });
        container.appendChild(grid);
    }

    function loadBoards(gameName, betAmount, container) {
        if (!state.selectedSport) {
            return;
        }
        const params = new URLSearchParams({
            sport: state.selectedSport,
            game: gameName,
            bet: betAmount
        });
        fetchJson(`/api/lobby/boards?${params.toString()}`)
            .then(boards => renderBoards(container, boards))
            .catch(() => {
                container.innerHTML = '<p class="notice">Unable to load boards right now.</p>';
            });
    }

    function loadGames() {
        if (!state.selectedSport) {
            gamesList.innerHTML = '';
            return;
        }
        const params = new URLSearchParams({ sport: state.selectedSport });
        fetchJson(`/api/lobby/games?${params.toString()}`)
            .then(renderGames)
            .catch(() => {
                gamesEmpty.textContent = 'Unable to load games right now.';
            });
    }

    function loadInitialData() {
        Promise.all([fetchJson('/api/lobby/sports'), fetchJson('/api/lobby/bets')])
            .then(([sports, bets]) => {
                state.sports = sports;
                state.bets = bets;
                state.selectedSport = sports[0]?.name || null;
                renderSports();
                loadGames();
            })
            .catch(() => {
                sportList.textContent = 'Unable to load lobby right now.';
            });
    }

    setInterval(() => {
        if (!state.selectedSport) {
            return;
        }
        const gameCards = Array.from(gamesList.querySelectorAll('.game-card'));
        gameCards.forEach(card => {
            const title = card.querySelector('h3');
            const boardList = card.querySelector('.board-list');
            if (!title || !boardList) {
                return;
            }
            const selectedBet = state.selectedBets.get(title.textContent);
            if (selectedBet) {
                loadBoards(title.textContent, selectedBet, boardList);
            }
        });
    }, 15000);

    loadInitialData();
})();
