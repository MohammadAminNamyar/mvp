(function () {
    const boardContainer = document.getElementById('board');
    const boardId = boardContainer.getAttribute('data-board-id');
    const sessionKey = 'squares.sessionId';
    const usernameKey = 'squares.username';
    const displayNameKey = 'squares.displayName';
    const ticketKey = 'squares.serviceTicket';
    let sessionId = localStorage.getItem(sessionKey);
    if (!sessionId) {
        sessionId = crypto.randomUUID();
        localStorage.setItem(sessionKey, sessionId);
    }
    const username = localStorage.getItem(usernameKey);
    let displayName = username;
    if (!displayName) {
        displayName = localStorage.getItem(displayNameKey);
        if (!displayName) {
            displayName = `Guest-${sessionId.slice(0, 6)}`;
            localStorage.setItem(displayNameKey, displayName);
        }
    }
    const loginNav = document.getElementById('login-nav');
    const serviceTicket = localStorage.getItem(ticketKey);
    if (loginNav) {
        loginNav.textContent = displayName;
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
        confettiLayer: document.getElementById('confetti-layer'),
        purchaseModal: document.getElementById('purchase-modal'),
        purchaseCount: document.getElementById('purchase-count'),
        purchaseTotal: document.getElementById('purchase-total'),
        purchaseConfirm: document.getElementById('purchase-confirm'),
        purchaseCancel: document.getElementById('purchase-cancel'),
        purchaseBackdrop: document.getElementById('purchase-backdrop')
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
        let customerName = displayName.trim();
        if (!customerName) {
            displayName = `Guest-${sessionId.slice(0, 6)}`;
            localStorage.setItem(displayNameKey, displayName);
            customerName = displayName;
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
        elements.customerName.textContent = displayName;
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
        cell.classList.remove(
            'empty',
            'reserved-me',
            'reserved-other',
            'reserved',
            'taken',
            'taken-me',
            'taken-other',
            'house',
            'current-winner',
            'winner',
            'flash'
        );
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
            cell.classList.add('reserved');
            cell.textContent = 'Reserved';
            if (isReservedByMe) {
                cell.classList.add('reserved-me');
                cell.addEventListener('click', () => {
                    unreserve(square.idx).catch(err => {
                        elements.message.textContent = err.message;
                    });
                }, { once: true });
            } else {
                cell.classList.add('reserved-other');
                if (isNewlyReservedByOther(square)) {
                    cell.classList.add('flash');
                    setTimeout(() => {
                        cell.classList.remove('flash');
                    }, 400);
                }
            }
        } else if (square.status === 'TAKEN') {
            cell.classList.add('taken');
            const ownedByMe = square.ownerSessionId
                ? square.ownerSessionId === sessionId
                : (square.ownerName && square.ownerName === displayName);
            if (ownedByMe) {
                cell.classList.add('taken-me');
                cell.textContent = 'Mine';
            } else {
                cell.classList.add('taken-other');
                cell.textContent = 'Taken';
            }
        } else if (square.status === 'HOUSE') {
            cell.classList.add('house');
            cell.textContent = 'HOUSE';
        }

        if (square.wonQ1 || square.wonQ2 || square.wonQ3 || square.wonFinal) {
            cell.classList.add('winner');
        }

        if (snapshot.currentWinnerIdx === square.idx) {
            cell.classList.add('current-winner');
        }

        addBadge(cell, square);
    }

    function addBadge(cell, square) {
        const badges = [];
        if (square.wonQ1) badges.push({ label: '1st', position: 'top-left', color: 'rgba(56, 189, 248, 0.95)' });
        if (square.wonQ2) badges.push({ label: 'half', position: 'top-right', color: 'rgba(167, 139, 250, 0.95)' });
        if (square.wonQ3) badges.push({ label: '3rd', position: 'bottom-left', color: 'rgba(52, 211, 153, 0.95)' });
        if (square.wonFinal) badges.push({ label: 'full', position: 'bottom-right', color: 'rgba(251, 191, 36, 0.95)' });
        if (badges.length === 0) {
            return;
        }
        badges.forEach(entry => {
            const badge = document.createElement('div');
            badge.className = `badge badge-${entry.position}`;
            badge.textContent = entry.label;
            badge.style.setProperty('--badge-color', entry.color);
            cell.appendChild(badge);
        });
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
            .filter(square => square.status === 'TAKEN' && square.ownerSessionId === sessionId)
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
            entry.textContent = `#${square.idx} • Mine`;
            elements.historyList.appendChild(entry);
        });
    }

    function renderPrizeBoard() {
        if (!elements.prizeGrid) {
            return;
        }
        const ownedCount = snapshot.squares
            .filter(square => square.status === 'TAKEN' && square.ownerSessionId === sessionId).length;
        const prizes = [
<<<<<<< HEAD
            { key: 'Q1', label: '1ST', period: 'QUARTER', cents: snapshot.prizeQ1Cents },
            { key: 'Q2', label: '1ST', period: 'HALF', cents: snapshot.prizeQ2Cents },
            { key: 'Q3', label: '3RD', period: 'QUARTER', cents: snapshot.prizeQ3Cents },
            { key: 'Q4', label: 'FULL', period: 'GAME', cents: snapshot.prizeQ4Cents }
=======
            {
                label: '1ST',
                period: 'QUARTER',
                cents: snapshot.prizeQ1Cents,
                perSquare: snapshot.prizePerSquareQ1Cents,
                rolled: snapshot.prizeQ1RolledOver
            },
            {
                label: '1ST',
                period: 'HALF',
                cents: snapshot.prizeQ2Cents,
                perSquare: snapshot.prizePerSquareQ2Cents,
                rolled: snapshot.prizeQ2RolledOver
            },
            {
                label: '3RD',
                period: 'QUARTER',
                cents: snapshot.prizeQ3Cents,
                perSquare: snapshot.prizePerSquareQ3Cents,
                rolled: snapshot.prizeQ3RolledOver
            },
            {
                label: 'FULL',
                period: 'GAME',
                cents: snapshot.prizeQ4Cents,
                perSquare: snapshot.prizePerSquareQ4Cents,
                rolled: false
            }
>>>>>>> origin/dev
        ];
        const adjusted = applyRollover(prizes);
        elements.prizeGrid.innerHTML = '';
        prizes.forEach(prize => {
            const display = adjusted[prize.key];
            const card = document.createElement('div');
            card.className = 'grid-prize';
            if (snapshot.currentQuarter === prize.key) {
                card.classList.add('is-current');
            }
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
<<<<<<< HEAD
            amount.textContent = display.vacant ? 'VACANT' : formatMoney(display.cents);
=======
            if (prize.rolled) {
                amount.textContent = 'Rolled';
            } else {
                amount.textContent = formatMoney(prize.cents);
            }
>>>>>>> origin/dev
            card.appendChild(period);
            card.appendChild(amount);
            const share = document.createElement('div');
            share.className = 'prize-share';
            if (snapshot.finalPrizeRefunded && prize.period === 'GAME') {
                share.textContent = `Refund per player: ${formatMoney(snapshot.finalRefundPerPlayerCents)}`;
            } else if (!prize.rolled) {
                share.textContent = `Your share: ${formatMoney(prize.perSquare * ownedCount)}`;
            }
            card.appendChild(share);
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
        triggerScoreCelebration();
        const ownedCount = snapshot.squares
            .filter(square => square.status === 'TAKEN' && square.ownerSessionId === sessionId).length;
        const prizes = [
<<<<<<< HEAD
            { key: 'Q1', label: '1ST', period: 'QUARTER', cents: snapshot.prizeQ1Cents },
            { key: 'Q2', label: '1ST', period: 'HALF', cents: snapshot.prizeQ2Cents },
            { key: 'Q3', label: '3RD', period: 'QUARTER', cents: snapshot.prizeQ3Cents },
            { key: 'Q4', label: 'FULL', period: 'GAME', cents: snapshot.prizeQ4Cents }
=======
            {
                label: '1ST',
                period: 'QUARTER',
                cents: snapshot.prizeQ1Cents,
                perSquare: snapshot.prizePerSquareQ1Cents,
                rolled: snapshot.prizeQ1RolledOver
            },
            {
                label: '1ST',
                period: 'HALF',
                cents: snapshot.prizeQ2Cents,
                perSquare: snapshot.prizePerSquareQ2Cents,
                rolled: snapshot.prizeQ2RolledOver
            },
            {
                label: '3RD',
                period: 'QUARTER',
                cents: snapshot.prizeQ3Cents,
                perSquare: snapshot.prizePerSquareQ3Cents,
                rolled: snapshot.prizeQ3RolledOver
            },
            {
                label: 'FULL',
                period: 'GAME',
                cents: snapshot.prizeQ4Cents,
                perSquare: snapshot.prizePerSquareQ4Cents,
                rolled: false
            }
>>>>>>> origin/dev
        ];
        const adjusted = applyRollover(prizes);
        elements.scoreboardPrizes.innerHTML = '';
        prizes.forEach(prize => {
            const display = adjusted[prize.key];
            const item = document.createElement('div');
            item.className = 'scoreboard-prize';
<<<<<<< HEAD
            if (snapshot.currentQuarter === prize.key) {
                item.classList.add('is-current');
            }
            const displayText = display.vacant ? 'VACANT' : formatMoney(display.cents);
            item.textContent = `${prize.label} ${prize.period} ${displayText}`;
=======
            if (prize.rolled) {
                item.textContent = `${prize.label} ${prize.period} Rolled`;
            } else if (snapshot.finalPrizeRefunded && prize.period === 'GAME') {
                item.textContent = `${prize.label} ${prize.period} Refund ${formatMoney(snapshot.finalRefundPerPlayerCents)}`;
            } else {
                const share = formatMoney(prize.perSquare * ownedCount);
                item.textContent = `${prize.label} ${prize.period} ${formatMoney(prize.cents)} • Yours ${share}`;
            }
>>>>>>> origin/dev
            elements.scoreboardPrizes.appendChild(item);
        });
    }

    function applyRollover(prizes) {
        const result = {};
        prizes.forEach(prize => {
            result[prize.key] = { cents: prize.cents, vacant: false };
        });
        if (!snapshot.rolloverOnNoWinner || !snapshot.rolloverCents) {
            return result;
        }
        if (result.Q4) {
            result.Q4.cents += snapshot.rolloverCents;
        }
        const rolloverQuarters = snapshot.rolloverQuarters || [];
        rolloverQuarters.forEach(quarter => {
            if (result[quarter]) {
                result[quarter].cents = 0;
                result[quarter].vacant = true;
            }
        });
        return result;
    }

    function nextQuarter(quarter) {
        switch (quarter) {
            case 'Q1':
                return 'Q2';
            case 'Q2':
                return 'Q3';
            case 'Q3':
                return 'Q4';
            default:
                return null;
        }
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

    function triggerScoreCelebration() {
        if (!previousSnapshot) {
            return;
        }
        const awayDelta = snapshot.awayScore - previousSnapshot.awayScore;
        const homeDelta = snapshot.homeScore - previousSnapshot.homeScore;
        if (awayDelta > 0) {
            celebrateTeam('away');
        }
        if (homeDelta > 0) {
            celebrateTeam('home');
        }
    }

    function celebrateTeam(side) {
        const isAway = side === 'away';
        const logo = isAway ? elements.scoreboardAwayLogo : elements.scoreboardHomeLogo;
        const color = isAway ? '#ef4444' : '#3b82f6';
        if (logo) {
            logo.classList.remove('score-celebrate');
            void logo.offsetWidth;
            logo.style.setProperty('--glow-color', color);
            logo.classList.add('score-celebrate');
            setTimeout(() => {
                logo.classList.remove('score-celebrate');
            }, 2000);
        }
        spawnConfetti(color);
    }

    function spawnConfetti(color) {
        const layer = elements.confettiLayer;
        if (!layer) {
            return;
        }
        const count = 40;
        for (let i = 0; i < count; i++) {
            const piece = document.createElement('span');
            piece.className = 'confetti-piece';
            piece.style.backgroundColor = color;
            piece.style.left = `${Math.random() * 100}%`;
            piece.style.animationDelay = `${Math.random() * 0.4}s`;
            piece.style.animationDuration = `${2.1 + Math.random() * 0.7}s`;
            layer.appendChild(piece);
            setTimeout(() => piece.remove(), 3200);
        }
    }

    function openPurchaseModal() {
        if (!elements.purchaseModal) {
            purchase();
            return;
        }
        const count = selected.size;
        if (count === 0) {
            elements.message.textContent = 'Select at least one square.';
            return;
        }
        const total = (snapshot.priceCents * count) / 100;
        elements.purchaseCount.textContent = count;
        elements.purchaseTotal.textContent = total.toFixed(2);
        elements.purchaseModal.classList.add('open');
    }

    function closePurchaseModal() {
        if (elements.purchaseModal) {
            elements.purchaseModal.classList.remove('open');
        }
    }

    elements.confirm.addEventListener('click', openPurchaseModal);
    if (elements.purchaseConfirm) {
        elements.purchaseConfirm.addEventListener('click', () => {
            closePurchaseModal();
            purchase();
        });
    }
    if (elements.purchaseCancel) {
        elements.purchaseCancel.addEventListener('click', () => {
            closePurchaseModal();
        });
    }
    if (elements.purchaseBackdrop) {
        elements.purchaseBackdrop.addEventListener('click', () => {
            closePurchaseModal();
        });
    }
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
