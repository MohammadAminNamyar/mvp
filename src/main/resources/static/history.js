(function () {
    const historyGrid = document.getElementById('history-grid');
    const userPill = document.getElementById('history-user');
    const sessionKey = 'squares.sessionId';
    const usernameKey = 'squares.username';
    const displayNameKey = 'squares.displayName';

    let sessionId = localStorage.getItem(sessionKey);
    if (!sessionId) {
        sessionId = crypto.randomUUID();
        localStorage.setItem(sessionKey, sessionId);
    }
    let displayName = localStorage.getItem(usernameKey) || localStorage.getItem(displayNameKey);
    if (!displayName) {
        displayName = `Guest-${sessionId.slice(0, 6)}`;
        localStorage.setItem(displayNameKey, displayName);
    }
    if (userPill) {
        userPill.textContent = displayName;
    }

    function formatMoney(cents) {
        const value = Number(cents || 0) / 100;
        return `$ ${value.toFixed(2)}`;
    }

    function renderEmpty() {
        historyGrid.innerHTML = `
            <div class="card empty-history">
                <h3>No history yet</h3>
                <p class="muted">Once a board finishes, your wins and losses will appear here.</p>
            </div>
        `;
    }

    function renderHistory(boards) {
        historyGrid.innerHTML = '';
        if (!boards.length) {
            renderEmpty();
            return;
        }
        boards.forEach(board => {
            const card = document.createElement('div');
            card.className = 'card history-entry';

            const header = document.createElement('div');
            header.className = 'history-entry-header';
            header.innerHTML = `
                <div>
                    <h3>${board.name || 'Board'}</h3>
                    <p class="muted small">${board.sportType || 'Sport'} • ${board.gameName || `${board.awayTeam} at ${board.homeTeam}`}</p>
                </div>
                <span class="status-pill">Finished</span>
            `;

            const metrics = document.createElement('div');
            metrics.className = 'history-metrics';
            metrics.innerHTML = `
                <div><span class="muted small">Squares</span><strong>${board.squares.length}</strong></div>
                <div><span class="muted small">Spent</span><strong>${formatMoney(board.totalSpentCents)}</strong></div>
                <div><span class="muted small">Winnings</span><strong>${formatMoney(board.totalWinningsCents)}</strong></div>
                <div><span class="muted small">Net</span><strong class="${board.netCents >= 0 ? 'positive' : 'negative'}">${formatMoney(board.netCents)}</strong></div>
            `;

            const squares = document.createElement('div');
            squares.className = 'history-squares';
            board.squares.forEach(square => {
                const chip = document.createElement('span');
                chip.className = 'history-chip';
                chip.textContent = `#${square.idx}`;
                const wins = [];
                if (square.wonQ1) wins.push('1st');
                if (square.wonQ2) wins.push('Half');
                if (square.wonQ3) wins.push('3rd');
                if (square.wonFinal) wins.push('Final');
                if (wins.length) {
                    chip.classList.add('winner');
                    chip.title = `Won ${wins.join(', ')}`;
                }
                squares.appendChild(chip);
            });

            card.appendChild(header);
            card.appendChild(metrics);
            card.appendChild(squares);

            if (board.finalPrizeRefunded) {
                const note = document.createElement('div');
                note.className = 'history-note';
                note.textContent = `Final square empty — refund per player: ${formatMoney(board.finalRefundPerPlayerCents)}.`;
                card.appendChild(note);
            }

            historyGrid.appendChild(card);
        });
    }

    fetch('/api/history', {
        headers: { 'X-Session-Id': sessionId }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Unable to load history.');
            }
            return response.json();
        })
        .then(renderHistory)
        .catch(() => {
            historyGrid.innerHTML = `
                <div class="card empty-history">
                    <h3>Unable to load history</h3>
                    <p class="muted">Please try again later.</p>
                </div>
            `;
        });
})();
