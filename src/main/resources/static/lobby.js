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

    function updateSlidingPill() {
        const activeButton = sportList.querySelector('.sport-button.active');
        const slider = sportList.querySelector('.sport-slider');
        
        if (!activeButton || !slider) return;
        
        const containerRect = sportList.getBoundingClientRect();
        const buttonRect = activeButton.getBoundingClientRect();
        
        const left = buttonRect.left - containerRect.left;
        const width = buttonRect.width;
        
        slider.style.transform = `translateX(${left}px)`;
        slider.style.width = `${width}px`;
    }

    function renderSports(sports) {
        // Find the index of the previously selected sport
        const oldActiveButton = sportList.querySelector('.sport-button.active');
        let previousSportIndex = -1;
        if (oldActiveButton) {
            const previousSport = oldActiveButton.textContent;
            previousSportIndex = sports.indexOf(previousSport);
        }
        
        sportList.innerHTML = '';
        if (!sports.length) {
            sportList.innerHTML = '<p class="muted">No sports are available yet.</p>';
            return;
        }
        
        // Create sliding pill element
        const slider = document.createElement('div');
        slider.className = 'sport-slider';
        sportList.appendChild(slider);
        
        // First, create all buttons to measure their natural widths
        const buttons = [];
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
            buttons.push(button);
        });
        
        // Calculate maximum width and apply to all buttons
        requestAnimationFrame(() => {
            let maxWidth = 0;
            buttons.forEach(button => {
                const width = button.getBoundingClientRect().width;
                if (width > maxWidth) {
                    maxWidth = width;
                }
            });
            
            // Apply the maximum width to all buttons
            buttons.forEach(button => {
                button.style.width = `${maxWidth}px`;
            });
            
            // Wait for layout to settle, then handle animation
            requestAnimationFrame(() => {
                // If we had a previous selection, set slider to that button's position first (without transition)
                if (previousSportIndex >= 0 && previousSportIndex < buttons.length) {
                    const previousButton = buttons[previousSportIndex];
                    const containerRect = sportList.getBoundingClientRect();
                    const buttonRect = previousButton.getBoundingClientRect();
                    const oldLeft = buttonRect.left - containerRect.left;
                    const oldWidth = buttonRect.width;
                    
                    slider.style.transition = 'none';
                    slider.style.transform = `translateX(${oldLeft}px)`;
                    slider.style.width = `${oldWidth}px`;
                    
                    // Force a reflow to ensure the initial position is set
                    slider.offsetHeight;
                    
                    // Re-enable transition and animate to new position
                    requestAnimationFrame(() => {
                        slider.style.transition = '';
                        updateSlidingPill();
                    });
                } else {
                    // First render, no animation needed
                    updateSlidingPill();
                }
                
                // Scroll active button into view, then update pill position again
                const activeButton = sportList.querySelector('.sport-button.active');
                if (activeButton) {
                    activeButton.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'center' });
                    // Update pill position after scroll animation completes
                    setTimeout(() => {
                        updateSlidingPill();
                    }, 300);
                }
            });
        });
    }

    function renderEmptyGamesMessage() {
        gamesList.innerHTML =
            '<p class="empty-state">We’re sorry, the buzzer has sounded and there are no available boards at this time.</p>';
    }

    function renderGames(games) {
        const hasExistingContent = gamesList.children.length > 0;
        
        if (hasExistingContent) {
            // Fade out existing content
            gamesList.classList.add('fade-out');
        }
        
        // Wait for fade out (if needed), then update content and fade in
        const updateContent = () => {
            gamesList.innerHTML = '';
            // Start with content hidden
            gamesList.classList.remove('fade-in');
            gamesList.classList.add('fade-out');
            
            if (!games.length) {
                renderEmptyGamesMessage();
            } else {
                games.forEach((game, index) => {
                    // Create VS banner for this game
                    const vsBanner = document.createElement('div');
                    vsBanner.className = 'games-vs-banner';
                    vsBanner.setAttribute('data-game-id', game.gameId || `game-${index}`);
                    vsBanner.innerHTML = `
                        <div class="vs-team vs-team-away">
                            <div class="vs-team-name">${game.awayTeam}</div>
                            <div class="vs-team-label">AWAY</div>
                        </div>
                        <div class="vs-divider">
                            <span class="vs-text">VS</span>
                        </div>
                        <div class="vs-team vs-team-home">
                            <div class="vs-team-name">${game.homeTeam}</div>
                            <div class="vs-team-label">HOME</div>
                        </div>
                    `;
                    gamesList.appendChild(vsBanner);

                    // Add date below the VS banner
                    const vsDate = document.createElement('div');
                    vsDate.className = 'vs-date';
                    vsDate.textContent = '03 - March - 2026';
                    gamesList.appendChild(vsDate);

                    // Create a wrapper to ensure proper isolation
                    const card = document.createElement('div');
                    card.className = 'game-card';
                    card.setAttribute('data-game-id', game.gameId || index);
                    card.setAttribute('data-game-index', index);
                    // Stagger animation for each card
                    card.style.animationDelay = `${index * 0.1}s`;

                    // Create content area
                    const contentArea = document.createElement('div');
                    contentArea.className = 'game-content';

                    const betRow = document.createElement('div');
                    betRow.className = 'bet-row';
                    const betLabel = document.createElement('span');
                    betLabel.className = 'muted small';
                    betLabel.textContent = 'Select bet amount:';
                    betRow.appendChild(betLabel);

                    const betButtons = document.createElement('div');
                    betButtons.className = 'bet-buttons';
                    betButtons.setAttribute('data-game-id', game.gameId || `game-${index}`);

                    const boardsContainer = document.createElement('div');
                    boardsContainer.className = 'boards-container';
                    boardsContainer.setAttribute('data-game-id', game.gameId || `game-${index}`);

                    betRow.appendChild(betButtons);
                    contentArea.appendChild(betRow);
                    contentArea.appendChild(boardsContainer);
                    
                    card.appendChild(contentArea);
                    gamesList.appendChild(card);

                    // Use a closure to ensure each game uses its own containers
                    // Store gameId for verification
                    const gameId = game.gameId || `game-${index}`;
                    (function(currentGame, currentGameId, currentBetButtons, currentBoardsContainer) {
                        loadBetOptions(currentGame, currentGameId, currentBetButtons, currentBoardsContainer);
                    })(game, gameId, betButtons, boardsContainer);
                });
            }
            
            // Force a reflow, then fade in new content
            gamesList.offsetHeight;
            requestAnimationFrame(() => {
                requestAnimationFrame(() => {
                    gamesList.classList.remove('fade-out');
                    gamesList.classList.add('fade-in');
                });
            });
        };
        
        if (hasExistingContent) {
            setTimeout(updateContent, 150);
        } else {
            updateContent();
        }
    }

    function renderBoards(boards, container, expectedGameId) {
        // Verify container before rendering
        if (expectedGameId && container.getAttribute('data-game-id') !== expectedGameId) {
            console.error('renderBoards: Container mismatch!', { expectedGameId, currentId: container.getAttribute('data-game-id') });
            return;
        }

        const hasExistingContent = container.children.length > 0;
        
        if (hasExistingContent) {
            // Fade out existing content
            container.classList.add('fade-out');
        }
        
        const updateContent = () => {
            // Final verification before updating
            if (expectedGameId && container.getAttribute('data-game-id') !== expectedGameId) {
                console.error('renderBoards: Container changed during update!', { expectedGameId, currentId: container.getAttribute('data-game-id') });
                return;
            }

            container.innerHTML = '';
            // Start with content hidden
            container.classList.remove('fade-in');
            container.classList.add('fade-out');
            
            if (!boards.length) {
                container.innerHTML = '<p class="muted">No boards for this bet level yet.</p>';
            } else {
                const list = document.createElement('div');
                list.className = 'board-list';
                boards.forEach((board, index) => {
                    const isLastBoard = index === boards.length - 1;
                    const isOpen = isLastBoard;
                    const item = document.createElement('a');
                    item.href = `/boards/${board.id}/view`;
                    item.className = 'board-row';
                    if (!isOpen) {
                        item.classList.add('board-full');
                    }
                    // Stagger animation for each board
                    item.style.animationDelay = `${index * 0.05}s`;
                    item.innerHTML = `
                        <div>
                            <strong>${board.name}</strong>
                            <span class="muted small">Open squares: ${board.openSquares}</span>
                        </div>
                        <span class="status-pill ${isOpen ? 'success' : 'danger'}">
                            ${isOpen ? 'Open' : 'Full'}
                        </span>
                    `;
                    list.appendChild(item);
                });
                container.appendChild(list);
            }
            
            // Force a reflow, then fade in new content
            container.offsetHeight;
            requestAnimationFrame(() => {
                requestAnimationFrame(() => {
                    container.classList.remove('fade-out');
                    container.classList.add('fade-in');
                });
            });
        };
        
        if (hasExistingContent) {
            setTimeout(updateContent, 150);
        } else {
            updateContent();
        }
    }

    function loadBoards(gameId, betCents, container, expectedGameId) {
        // Verify container matches expected game
        const containerGameId = container.getAttribute('data-game-id');
        if (expectedGameId && containerGameId !== expectedGameId) {
            console.error('loadBoards: Container mismatch!', { gameId, expectedGameId, containerGameId });
            return;
        }

        fetchJson(`/api/lobby/games/${encodeURIComponent(gameId)}/boards?betCents=${betCents}`)
            .then(boards => {
                // Final check before rendering
                if (expectedGameId && container.getAttribute('data-game-id') !== expectedGameId) {
                    console.error('loadBoards: Container changed before render!', { gameId, expectedGameId, currentId: container.getAttribute('data-game-id') });
                    return;
                }
                renderBoards(boards, container, expectedGameId);
            })
            .catch(() => {
                if (expectedGameId && container.getAttribute('data-game-id') !== expectedGameId) {
                    return;
                }
                const hasExistingContent = container.children.length > 0;
                if (hasExistingContent) {
                    container.classList.add('fade-out');
                    setTimeout(() => {
                        if (container.getAttribute('data-game-id') === expectedGameId) {
                            container.innerHTML = '<p class="muted">Unable to load boards right now.</p>';
                            container.classList.remove('fade-out');
                            container.classList.add('fade-in');
                        }
                    }, 150);
                } else {
                    container.innerHTML = '<p class="muted">Unable to load boards right now.</p>';
                }
            });
    }

    function loadBetOptions(game, gameId, betButtons, boardsContainer) {
        // Verify we're using the correct container
        const expectedGameId = gameId;
        const containerGameId = boardsContainer.getAttribute('data-game-id');
        if (containerGameId !== expectedGameId) {
            console.error('Container mismatch!', { expectedGameId, containerGameId });
            return;
        }

        fetchJson(`/api/lobby/games/${encodeURIComponent(game.gameId)}/bets`)
            .then(options => {
                // Double-check container is still correct
                if (boardsContainer.getAttribute('data-game-id') !== expectedGameId) {
                    console.error('Container changed during async operation!', { expectedGameId, currentId: boardsContainer.getAttribute('data-game-id') });
                    return;
                }

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
                        // Verify container before loading boards
                        if (boardsContainer.getAttribute('data-game-id') !== expectedGameId) {
                            console.error('Container mismatch on click!', { expectedGameId, currentId: boardsContainer.getAttribute('data-game-id') });
                            return;
                        }
                        betButtons.querySelectorAll('.bet-button').forEach(btn => btn.classList.remove('active'));
                        betButton.classList.add('active');
                        loadBoards(game.gameId, option.cents, boardsContainer, expectedGameId);
                    });
                    if (index === 0) {
                        betButton.classList.add('active');
                    }
                    betButtons.appendChild(betButton);
                });
                loadBoards(game.gameId, options[0].cents, boardsContainer, expectedGameId);
            })
            .catch(() => {
                if (boardsContainer.getAttribute('data-game-id') === expectedGameId) {
                    boardsContainer.innerHTML = '<p class="muted">Unable to load bet options right now.</p>';
                }
            });
    }

    function loadGames() {
        if (!selectedSport) {
            gamesList.classList.add('fade-out');
            setTimeout(() => {
                gamesList.innerHTML = '';
                gamesList.classList.remove('fade-out');
            }, 150);
            return;
        }
        fetchJson(`/api/lobby/sports/${encodeURIComponent(selectedSport)}/games`)
            .then(renderGames)
            .catch(() => {
                gamesList.classList.add('fade-out');
                setTimeout(() => {
                    renderEmptyGamesMessage();
                    gamesList.classList.remove('fade-out');
                    gamesList.classList.add('fade-in');
                }, 150);
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
                sportList.innerHTML = '<p class="muted">Unable to load lobby data.</p>';
            });
        
        // Update pill position on window resize
        let resizeTimeout;
        window.addEventListener('resize', () => {
            clearTimeout(resizeTimeout);
            resizeTimeout = setTimeout(() => {
                updateSlidingPill();
            }, 100);
        });
        
        // Update pill position on scroll
        sportList.addEventListener('scroll', () => {
            updateSlidingPill();
        });
    }

    init();
})();
