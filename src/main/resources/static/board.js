(function () {
    const boardContainer = document.getElementById('board');
    const boardId = boardContainer.getAttribute('data-board-id');
    const sessionKey = 'squares.sessionId';
    let sessionId = localStorage.getItem(sessionKey);
    if (!sessionId) {
        sessionId = crypto.randomUUID();
        localStorage.setItem(sessionKey, sessionId);
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
        message: document.getElementById('ticket-message'),
        viewerCount: document.getElementById('viewer-count')
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
        const customerName = elements.customerName.value.trim();
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
        elements.name.textContent = snapshot.name;
        elements.status.textContent = snapshot.status;
        elements.activation.textContent = snapshot.active ? 'Active' : 'Not Active';
        elements.price.textContent = (snapshot.priceCents / 100).toFixed(2);
        elements.house.textContent = snapshot.housePercent;
        elements.score.textContent = `${snapshot.homeScore} - ${snapshot.awayScore}`;
        elements.quarter.textContent = snapshot.currentQuarter;
        selected = new Set(snapshot.squares
            .filter(square => square.status === 'RESERVED' && square.reservedBySessionId === sessionId)
            .map(square => square.idx));
        renderSelected();
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
        titleCell.colSpan = 11;
        titleCell.textContent = snapshot.homeTeam;
        titleRow.appendChild(titleCell);
        table.appendChild(titleRow);

        const headerRow = document.createElement('tr');
        const corner = document.createElement('th');
        corner.className = 'team-header';
        corner.textContent = snapshot.awayTeam;
        headerRow.appendChild(corner);

        for (let col = 0; col < 10; col++) {
            const th = document.createElement('th');
            th.className = 'digit-header';
            th.textContent = snapshot.digitsRevealed ? snapshot.colDigits[col] : '';
            headerRow.appendChild(th);
        }
        table.appendChild(headerRow);

        for (let row = 0; row < 10; row++) {
            const tr = document.createElement('tr');
            const rowHeader = document.createElement('th');
            rowHeader.className = 'team-header';
            rowHeader.textContent = snapshot.digitsRevealed ? snapshot.rowDigits[row] : snapshot.awayTeam;
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

    elements.confirm.addEventListener('click', purchase);

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
