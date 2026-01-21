(function () {
    const boardContainer = document.getElementById('board');
    const boardId = boardContainer.getAttribute('data-board-id');
    const sessionKey = 'squares.sessionId';
    const usernameKey = 'squares.username';
    const ticketKey = 'squares.serviceTicket';
    let sessionId = localStorage.getItem(sessionKey);
    if (!sessionId) {
        sessionId = crypto.randomUUID();
        localStorage.setItem(sessionKey, sessionId);
    }
    const username = localStorage.getItem(usernameKey);
    const loginNav = document.getElementById('login-nav');
    const serviceTicket = localStorage.getItem(ticketKey);
    if (!username) {
        const redirect = encodeURIComponent(window.location.pathname);
        window.location.href = `/login?redirect=${redirect}`;
        return;
    }
    if (loginNav) {
        loginNav.textContent = username;
    }

    let snapshot = null;
    let previousSnapshot = null;
    let selected = new Set();

    const elements = {
        name: document.getElementById('board-name'),
        status: document.getElementById('board-status'),
        activation: document.getElementById('activation-status'),
        price: document.getElementById('price'),
        house: document.getElementById('house'),
        score: document.getElementById('score'),
        quarter: document.getElementById('quarter'),
        selected: document.getElementById('selected'),
        confirm: document.getElementById('confirm-btn'),
        customerName: document.getElementById('customer-name'),
        changeUser: document.getElementById('change-user'),
        message: document.getElementById('ticket-message'),
        viewerCount: document.getElementById('viewer-count'),
        requirementPrice: document.getElementById('requirement-price'),
        requirementHouse: document.getElementById('requirement-house'),
        minRequired: document.getElementById('min-required'),
        digitsStatus: document.getElementById('digits-status'),
        digitsStatusDot: document.getElementById('digits-status-dot'),
        lockStatus: document.getElementById('lock-status'),
        lockStatusDot: document.getElementById('lock-status-dot'),
        historyList: document.getElementById('history-list'),
        prizeGrid: document.getElementById('grid-prizes'),
        scoreboardPrizes: document.getElementById('scoreboard-prizes'),
        scoreboardAwayTeam: document.getElementById('scoreboard-away-team'),
        scoreboardHomeTeam: document.getElementById('scoreboard-home-team'),
        scoreboardAwayScore: document.getElementById('scoreboard-away-score'),
        scoreboardHomeScore: document.getElementById('scoreboard-home-score'),
        scoreboardClock: document.getElementById('scoreboard-clock'),
        scoreboardAwayLogo: document.getElementById('scoreboard-away-logo'),
        scoreboardHomeLogo: document.getElementById('scoreboard-home-logo'),
        loginNav
    };

    function fetchSnapshot() {
        return fetch(`/boards/${boardId}`)
            .then(response => response.json())
            .then(data => {
                previousSnapshot = snapshot;
                snapshot = data;
                render();
            });
    }

    function reserve(idx) {
        return fetch(`/boards/${boardId}/reserve/${idx}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ sessionId })
        }).then(handleResponse);
    }

    function unreserve(idx) {
        return fetch(`/boards/${boardId}/unreserve/${idx}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ sessionId })
        }).then(handleResponse);
    }

    function purchase() {
        const customerName = username.trim();
        if (!customerName) {
            elements.message.textContent = 'Enter a display name.';
            return;
        }
        const indices = Array.from(selected);
        if (indices.length === 0) {
            elements.message.textContent = 'Select at least one square.';
            return;
        }
        fetch(`/boards/${boardId}/purchase`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...(serviceTicket ? { 'X-Service-Ticket': serviceTicket } : {})
            },
            body: JSON.stringify({ sessionId, customerName, indices })
        })
            .then(handleResponse)
            .then(() => {
                elements.message.textContent = 'Purchase confirmed.';
                selected.clear();
                renderSelected();
            })
            .catch(err => {
                elements.message.textContent = err.message;
            });
    }

    function handleResponse(response) {
        if (!response.ok) {
            return response.json().then(data => {
                throw new Error(data.error || 'Request failed');
            });
        }
        return response;
    }

    function render() {
        if (!snapshot) {
            return;
        }
        elements.customerName.textContent = username;
        elements.name.textContent = snapshot.name;
        elements.status.textContent = snapshot.status;
        elements.activation.textContent = snapshot.active ? 'Active' : 'Not Active';
        elements.price.textContent = (snapshot.priceCents / 100).toFixed(2);
        elements.house.textContent = snapshot.housePercent;
        elements.score.textContent = `${snapshot.homeScore} - ${snapshot.awayScore}`;
        elements.quarter.textContent = snapshot.currentQuarter;
        elements.requirementPrice.textContent = (snapshot.priceCents / 100).toFixed(2);
        elements.requirementHouse.textContent = snapshot.housePercent;
        elements.minRequired.textContent = snapshot.minSquaresToActivate;
        selected = new Set(snapshot.squares
            .filter(square => square.status === 'RESERVED' && square.reservedBySessionId === sessionId)
            .map(square => square.idx));
        renderSelected();
        renderChecklist();
        renderHistory();
        renderPrizeBoard();
        renderScoreboard();
        renderGrid();
    }

    function renderSelected() {
        if (selected.size === 0) {
            elements.selected.textContent = 'None';
        } else {
            elements.selected.textContent = Array.from(selected).sort((a, b) => a - b).join(', ');
        }
    }

    function renderGrid() {
        const table = document.createElement('table');
        table.className = 'grid';
        const titleRow = document.createElement('tr');
        const titleCell = document.createElement('th');
        titleCell.className = 'team-header';
        titleCell.colSpan = 12;
        titleCell.textContent = snapshot.homeTeam;
        titleRow.appendChild(titleCell);
        table.appendChild(titleRow);

        const headerRow = document.createElement('tr');
        const verticalTeam = document.createElement('th');
        verticalTeam.className = 'team-header vertical-team';
        verticalTeam.rowSpan = 11;
        const verticalLabel = document.createElement('span');
        verticalLabel.textContent = snapshot.awayTeam;
        verticalTeam.appendChild(verticalLabel);
        headerRow.appendChild(verticalTeam);

        const corner = document.createElement('th');
        corner.className = 'team-header row-digit-corner';
        corner.textContent = '';
        headerRow.appendChild(corner);

        for (let col = 0; col < 10; col++) {
            const th = document.createElement('th');
            th.className = 'digit-header';
            th.textContent = snapshot.digitsRevealed ? snapshot.colDigits[col] : '•';
            if (!snapshot.digitsRevealed) {
                th.classList.add('hidden-digit');
            }
            headerRow.appendChild(th);
        }
        table.appendChild(headerRow);

        for (let row = 0; row < 10; row++) {
            const tr = document.createElement('tr');
            const rowHeader = document.createElement('th');
            rowHeader.className = 'digit-header row-header';
            rowHeader.textContent = snapshot.digitsRevealed ? snapshot.rowDigits[row] : '•';
            if (!snapshot.digitsRevealed) {
                rowHeader.classList.add('hidden-digit');
            }
            tr.appendChild(rowHeader);

            for (let col = 0; col < 10; col++) {
                const idx = row * 10 + col;
                const square = snapshot.squares.find(item => item.idx === idx);
                const td = document.createElement('td');
                td.classList.add('square');
                td.dataset.idx = idx;
                applySquareState(td, square);
                tr.appendChild(td);
            }
            table.appendChild(tr);
        }

        boardContainer.innerHTML = '';
        boardContainer.appendChild(table);
    }

    function isNewlyReservedByOther(square) {
        if (!previousSnapshot) return false;
        if (square.status !== 'RESERVED') return false;
        if (square.reservedBySessionId === sessionId) return false;
        
        const prevSquare = previousSnapshot.squares.find(s => s.idx === square.idx);
        if (!prevSquare) return true;
        
        // Check if this square was NOT reserved before, or was reserved by a different session
        return prevSquare.status !== 'RESERVED' || 
               prevSquare.reservedBySessionId !== square.reservedBySessionId;
    }

    function applySquareState(cell, square) {
        if (!square) {
            return;
        }
        cell.classList.remove('empty', 'reserved-me', 'reserved-other', 'taken', 'house', 'current-winner', 'flash');
        cell.textContent = '';
        const isReservedByMe = square.status === 'RESERVED' && square.reservedBySessionId === sessionId;
        if (square.status === 'EMPTY') {
            cell.classList.add('empty');
            cell.addEventListener('click', () => {
                reserve(square.idx).catch(err => {
                    elements.message.textContent = err.message;
                });
            }, { once: true });
        } else if (square.status === 'RESERVED') {
            if (isReservedByMe) {
                cell.classList.add('reserved-me');
                cell.textContent = 'Reserved';
                cell.addEventListener('click', () => {
                    unreserve(square.idx).catch(err => {
                        elements.message.textContent = err.message;
                    });
                }, { once: true });
            } else {
                cell.classList.add('reserved-other');
                // Add flash animation only for newly reserved squares by others
                if (isNewlyReservedByOther(square)) {
                    cell.classList.add('flash');
                    // Remove flash class after animation completes
                    setTimeout(() => {
                        cell.classList.remove('flash');
                    }, 400);
                }
                // X is displayed via CSS ::after pseudo-element
            }
        } else if (square.status === 'TAKEN') {
            cell.classList.add('taken');
            cell.textContent = square.ownerName || 'Taken';
        } else if (square.status === 'HOUSE') {
            cell.classList.add('house');
            cell.textContent = 'HOUSE';
        }

        if (snapshot.currentWinnerIdx === square.idx) {
            cell.classList.add('current-winner');
        }

        addBadge(cell, square);
    }

    function addBadge(cell, square) {
        const labels = [];
        if (square.wonQ1) labels.push('1st');
        if (square.wonQ2) labels.push('half');
        if (square.wonQ3) labels.push('3rd');
        if (square.wonFinal) labels.push('final');
        if (labels.length === 0) {
            return;
        }
        const badge = document.createElement('div');
        badge.className = 'badge';
        badge.textContent = labels.join(',');
        cell.appendChild(badge);
    }

    function renderChecklist() {
        const digitsLabel = snapshot.digitsRevealed ? 'Yes (board locked or started)' : 'Hidden until lock';
        elements.digitsStatus.textContent = digitsLabel;
        elements.digitsStatusDot.className = `status-dot ${snapshot.digitsRevealed ? 'locked' : 'warn'}`;

        const locked = snapshot.status !== 'OPEN';
        elements.lockStatus.textContent = locked ? 'Locked' : 'Open for purchase';
        elements.lockStatusDot.className = `status-dot ${locked ? 'locked' : 'ok'}`;
    }

    function renderHistory() {
        const takenSquares = snapshot.squares
            .filter(square => square.status === 'TAKEN' && square.ownerName === username)
            .sort((a, b) => a.idx - b.idx)
            .slice(0, 12);

        elements.historyList.innerHTML = '';
        if (takenSquares.length === 0) {
            const empty = document.createElement('li');
            empty.textContent = 'No purchases yet for you.';
            elements.historyList.appendChild(empty);
            return;
        }

        takenSquares.forEach(square => {
            const entry = document.createElement('li');
            const label = square.ownerName ? square.ownerName : 'Taken';
            entry.textContent = `#${square.idx} • ${label} (Buyer)`;
            elements.historyList.appendChild(entry);
        });
    }

    function renderPrizeBoard() {
        if (!elements.prizeGrid) {
            return;
        }
        const prizes = [
            { label: '1ST', period: 'QUARTER', cents: snapshot.prizeQ1Cents },
            { label: '1ST', period: 'HALF', cents: snapshot.prizeQ2Cents },
            { label: '3RD', period: 'QUARTER', cents: snapshot.prizeQ3Cents },
            { label: 'FULL', period: 'GAME', cents: snapshot.prizeQ4Cents }
        ];
        elements.prizeGrid.innerHTML = '';
        prizes.forEach(prize => {
            const card = document.createElement('div');
            card.className = 'grid-prize';
            const period = document.createElement('div');
            period.className = 'prize-period';
            const strong = document.createElement('strong');
            strong.textContent = prize.label;
            const sub = document.createElement('p');
            sub.textContent = prize.period;
            period.appendChild(strong);
            period.appendChild(sub);
            const amount = document.createElement('div');
            amount.className = 'prize-amount';
            amount.textContent = formatMoney(prize.cents);
            card.appendChild(period);
            card.appendChild(amount);
            elements.prizeGrid.appendChild(card);
        });
    }

    function renderScoreboard() {
        if (!elements.scoreboardPrizes) {
            return;
        }
        elements.scoreboardAwayTeam.textContent = snapshot.awayTeam;
        elements.scoreboardHomeTeam.textContent = snapshot.homeTeam;
        elements.scoreboardAwayScore.textContent = snapshot.awayScore;
        elements.scoreboardHomeScore.textContent = snapshot.homeScore;
        if (elements.scoreboardClock) {
            elements.scoreboardClock.textContent = snapshot.gameClock || '-';
        }
        setLogo(elements.scoreboardAwayLogo, snapshot.awayTeam);
        setLogo(elements.scoreboardHomeLogo, snapshot.homeTeam);
        const prizes = [
            { label: '1ST', period: 'QUARTER', cents: snapshot.prizeQ1Cents },
            { label: '1ST', period: 'HALF', cents: snapshot.prizeQ2Cents },
            { label: '3RD', period: 'QUARTER', cents: snapshot.prizeQ3Cents },
            { label: 'FULL', period: 'GAME', cents: snapshot.prizeQ4Cents }
        ];
        elements.scoreboardPrizes.innerHTML = '';
        prizes.forEach(prize => {
            const item = document.createElement('div');
            item.className = 'scoreboard-prize';
            item.textContent = `${prize.label} ${prize.period} ${formatMoney(prize.cents)}`;
            elements.scoreboardPrizes.appendChild(item);
        });
    }

    function formatMoney(cents) {
        const value = Number(cents || 0) / 100;
        return `$ ${value.toFixed(2)}`;
    }

    function setLogo(element, teamName) {
        if (!element) {
            return;
        }
        element.src = '/placeholder-shield.svg';
        element.alt = teamName ? `${teamName} logo` : 'Team logo';
    }

    elements.confirm.addEventListener('click', purchase);
    elements.changeUser.addEventListener('click', () => {
        const redirect = encodeURIComponent(window.location.pathname);
        window.location.href = `/login?redirect=${redirect}`;
    });

    fetchSnapshot();

    const socket = new SockJS('/ws');
    const stomp = Stomp.over(socket);
    stomp.debug = null;
    stomp.connect({}, () => {
        stomp.subscribe(`/topic/boards/${boardId}/snapshot`, message => {
            previousSnapshot = snapshot;
            snapshot = JSON.parse(message.body);
            render();
        });
        stomp.subscribe(`/topic/boards/${boardId}/presence`, message => {
            elements.viewerCount.textContent = message.body;
        });
    });
})();
