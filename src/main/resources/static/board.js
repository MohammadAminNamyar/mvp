(function () {
    const boardContainer = document.getElementById('board');
    const boardId = boardContainer.getAttribute('data-board-id');
    const sessionKey = 'squares.sessionId';
    const usernameKey = 'squares.username';
    let sessionId = localStorage.getItem(sessionKey);
    if (!sessionId) {
        sessionId = crypto.randomUUID();
        localStorage.setItem(sessionKey, sessionId);
    }
    const username = localStorage.getItem(usernameKey);
    if (!username) {
        const redirect = encodeURIComponent(window.location.pathname);
        window.location.href = `/login?redirect=${redirect}`;
        return;
    }

    let snapshot = null;
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
        historyList: document.getElementById('history-list')
    };

    function fetchSnapshot() {
        return fetch(`/boards/${boardId}`)
            .then(response => response.json())
            .then(data => {
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
            headers: { 'Content-Type': 'application/json' },
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

    function getTakenLabel(square) {
        if (!square.ownerName) {
            return 'Taken';
        }
        const normalizedOwner = square.ownerName.trim().toLowerCase();
        const normalizedUser = username.trim().toLowerCase();
        return normalizedOwner === normalizedUser ? square.ownerName : 'Taken';
    }

    function applySquareState(cell, square) {
        if (!square) {
            return;
        }
        cell.classList.remove('empty', 'reserved-me', 'reserved-other', 'taken', 'house', 'current-winner');
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
                cell.textContent = 'X';
            }
        } else if (square.status === 'TAKEN') {
            cell.classList.add('taken');
            cell.textContent = getTakenLabel(square);
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
            .filter(square => square.status === 'TAKEN' || square.status === 'HOUSE')
            .sort((a, b) => a.idx - b.idx)
            .slice(0, 12);

        elements.historyList.innerHTML = '';
        if (takenSquares.length === 0) {
            const empty = document.createElement('li');
            empty.textContent = 'No purchases yet. Be the first to claim a square.';
            elements.historyList.appendChild(empty);
            return;
        }

        takenSquares.forEach(square => {
            const entry = document.createElement('li');
            const isHouse = square.status === 'HOUSE';
            const label = isHouse ? 'House' : getTakenLabel(square);
            const statusLabel = isHouse ? 'House' : 'Buyer';
            entry.textContent = `#${square.idx} • ${label} (${statusLabel})`;
            elements.historyList.appendChild(entry);
        });
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
            snapshot = JSON.parse(message.body);
            render();
        });
        stomp.subscribe(`/topic/boards/${boardId}/presence`, message => {
            elements.viewerCount.textContent = message.body;
        });
    });
})();
