var user = {
    name : "",
    isComputer : false,
    turnsUpdated : 0,
    color : ""
};

var game = {
    boardHeight : 0,
    boardWidth : 0,
    isPopoutGame : false,
    isInProgress : false,
    isOver : false
};

var canPopinObjectArr = [];
var canPopoutObjectArr = [];

var promptWaitMessage = "Waiting for your turn...";
var promptPlayMessage;

var updateGameStateIntervalId;

var updater = {
    updatesQueue : [],
    isShowingMessage : false,

    addToUpdatesQueue : function (message) {
      this.updatesQueue.push(message);
      if (!this.isShowingMessage) {
          this.displayMessage(this.updatesQueue.shift());
      }
    },

    displayMessage : function (message) {
        this.isShowingMessage = true;

        let updatesDiv = $("#updatesDiv");
        $("#updatesText").text(message);
        updatesDiv.css("visibility","visible");
        updatesDiv.css("opacity", "1");

        setTimeout(function(){updater.hideUpdatesDiv()}, 4500);
    },

    hideUpdatesDiv : function () {
        $("#updatesDiv").css("opacity", "0");
        setTimeout(function(){updater.hideUpdatesDivFinalStep()}, 1500);
    },

    hideUpdatesDivFinalStep : function () {
        $("#updatesDiv").css("visibility", "hidden");
        $("#updatesText").text("");
        if(this.updatesQueue.length === 0){
            this.isShowingMessage = false;

        } else {
            this.displayMessage(this.updatesQueue.shift());
        }
    }
};

$(function () {
    getUserData();
    loadGameData();
    refreshGameStatus();
    refreshPlayersList();
    $("#leaveLink").click(onLeaveLinkClick);
});

function getUserData() {
    $.ajax({
        url: userStatusServletURL,
        dataType: 'json',
        success: function(userStatusResponse){
            /*
            userStatusResponse is of the form:
            {
                "loggedInName" : "Mushon",
                "isConnected" : true,
                "isPlaying" : true,
                "isComputer" : false,
                "currentGameTitle": "small game"
             }
            */
            user.name = userStatusResponse.loggedInName;
            user.isComputer = userStatusResponse.isComputer;
            promptPlayMessage = user.name + ": It's your turn!";
            $("#userNameText").text(`"${user.name}"`);
        }
    });
}

function loadGameData() {
    $.ajax({
        url: gameDataServletURL,
        dataType: "json",
        success: function(gameDataResponse){
            /*
                gameDataResponse is of the form:
                {
                    "title" :" sample game",
                    "variant" : "Circular",
                    "target" : 4,
                    "boardWidth":7,
                    "boardHeight":6,
                    "nameOfTheCreator" : "Mushon",
                    "gameInProgress" : false,
                    "currentPlayerCount" : 1,
                    "requiredPlayerCount" : 2
                }
            */
            $("#gameTitleText").text(gameDataResponse.title);
            $("#gameVariantText").text(gameDataResponse.variant);
            $("#gameTargetText").text(gameDataResponse.target);

            game.isPopoutGame = gameDataResponse.variant === "Popout";
            game.boardHeight = gameDataResponse.boardHeight;
            game.boardWidth = gameDataResponse.boardWidth;

            loadBoard(game.boardHeight, game.boardWidth);

            setCanPopinObjectArr(game.boardWidth);
            if(game.isPopoutGame){
                setCanPopoutObjectArr(game.boardWidth);
            }
        }
    })
}

function setCanPopinObjectArr(boardWidth) {
    for(let col = 0; col < boardWidth; col++){

        let canPopinObject = {
            booleanValue : false,
            set value (booleanValue) {
                this.booleanValue = booleanValue;
                $(`#popinButton-${col}`).attr("disabled", !booleanValue);
            }
        };

        canPopinObjectArr.push(canPopinObject);
    }

}

function setCanPopoutObjectArr(boardWidth) {
    for(let col = 0; col < boardWidth; col++){

        let canPopoutObject = {
            booleanValue : false,
            set value (booleanValue) {
                this.booleanValue = booleanValue;
                $(`#popoutButton-${col}`).attr("disabled", !booleanValue);
            }
        };

        canPopoutObjectArr.push(canPopoutObject);
    }
}

function loadBoard(boardHeight, boardWidth) {
    loadPopinButtons(boardWidth);

    let tbodyHTML = $("<tbody id='boardTableBody'>");
    $("#boardTable").append(tbodyHTML);

    for(let row = 0; row < boardHeight; row++){
        let trHTML = $("<tr class='boardTableRow'>");

        for(let col = 0; col < boardWidth; col++){
            let tdHTML = $(`<td class="boardTableCell row-${row} col-${col}">`);
            trHTML.append(tdHTML);
        }

        tbodyHTML.append(trHTML);
    }

    if(game.isPopoutGame){
        loadPopoutButtons(boardWidth);
    }
}

function loadPopinButtons(boardWidth) {
    let theadHTML = $("<thead></thead>");
    $("#boardTable").append(theadHTML);

    for(let col = 0; col < boardWidth; col++){
        let tdHTML = $("<td></td>");
        let buttonHTML = $(`<button type="button" id="popinButton-${col}" class="popinButton gameButton">popin</button>`);
        buttonHTML.data("data-col", col);
        buttonHTML.attr("disabled", true);
        buttonHTML.click(onPopinClick);
        tdHTML.append(buttonHTML);
        theadHTML.append(tdHTML);
    }
}

function onPopinClick() {
    lockPlayButtons();
    lockLeaveLink();
    promptUserToWait();

    let buttonCol = $(this).data("data-col");
    makeMove("Popin", buttonCol);
}

function loadPopoutButtons(boardWidth) {
    let tfootHTML = $("<tfoot></tfoot>");
    $("#boardTable").append(tfootHTML);

    for(let col = 0; col < boardWidth; col++){
        let tdHTML = $("<td></td>");
        let buttonHTML = $(`<button type="button" id="popoutButton-${col}" class="popoutButton gameButton">popout</button>`);
        buttonHTML.data("data-col", col);
        buttonHTML.attr("disabled", true);
        buttonHTML.click(onPopoutClick);
        tdHTML.append(buttonHTML);
        tfootHTML.append(tdHTML);
    }
}

function onPopoutClick() {
    lockPlayButtons();
    lockLeaveLink();
    promptUserToWait();
    let buttonCol = $(this).data("data-col");
    makeMove("Popout" , buttonCol);
}

var loadingPlayersListForTheFirstTime = true;
function refreshPlayersList() {
    $.ajax({
        url: playersListServletURL,
        dataType: "json",
        success: function(playersList){
            $("#playersListDiv").empty();
            $.each(playersList || [], appendToPlayersDiv);
        }
    });

    if(!game.isInProgress){
        setTimeout(refreshPlayersList, refreshRate);
    }
}

function appendToPlayersDiv(index, playerData) {
    /*
        playerData is of the form:
        {
            "name" : "Mushon",
            "isComputer" : false,
            "discNum" : 1,
            "turnsPlayed" : 0
            "isTurnHolder" : true
        }
    */

    let playerDivID = "playerDiv" + index;

    let playerDiv = $(`<div id="playerDiv${index}" class="playerDetailsDiv"></div>`);

    if(game.isInProgress && !game.isOver && playerData.isTurnHolder) {
        playerDiv.addClass("turnHolder");
    }

    let nameAndTypeDiv = $(`<div class="nameAndTypeDiv"></div>`);
    let colorAndTurnsDiv = $(`<div class="colorAndTurnsDiv"></div>`);

    $("#playersListDiv").append(playerDiv);
    playerDiv.append(nameAndTypeDiv, colorAndTurnsDiv);

    let nameHTML = $(`<span>Player Name: </span><span class="playerNameText">${playerData.name}</span>`);
    let typeText = playerData.isComputer ? "Computer" : "Human";
    let typeHTML = $(`<span>Player type: </span><span class='playerTypeText'>${typeText}</span>`);
    nameAndTypeDiv.append(nameHTML, $(`<br><br>`), typeHTML);

    // Using playerData.discNum instead of index because the index of the player might change when other players leave

    let discClasses = `${colorArr[playerData.discNum]} disc`; // For example, 'red disc'
    let colorHTML = $(`<span>Color: </span><div class="${discClasses}"></div>`);
    let turnsHTML = $(`<span>Turns Played: </span><span class="turnsPlayedText">${playerData.turnsPlayed}</span>`);

    colorAndTurnsDiv.append(colorHTML, $(`<br><br>`), turnsHTML);

    if(loadingPlayersListForTheFirstTime && playerData.name === user.name) {
        user.color = colorArr[playerData.discNum];
        setUserDiscAtUserInfo(user.color);
        loadingPlayersListForTheFirstTime = false;
    }
}

function setUserDiscAtUserInfo(discColor) {
    $(`<div id="infoDisc" class="${discColor} disc"></div>`)
        .appendTo($("#userDiscDiv"));
}

function onLeaveLinkClick() {
    let userIsSure = confirm("Are you sure you want to leave?");

    if(userIsSure && game.isInProgress){
        makeQuitMove();

    } else if(userIsSure) {
        leaveToLobby();
    }
}

function makeMove(moveType, col) {
    $.ajax({
        url: makeMoveServletURL,
        data: {
            moveType: moveType,
            selectedCol : col,
        },
        success: function(){
            // Immediately update the game state, then return the interval
            clearInterval(updateGameStateIntervalId);
            checkAndUpdateGameStateChanges();
            updateGameStateIntervalId = setInterval(checkAndUpdateGameStateChanges, refreshRate);
        }
    });
}

function lockPlayButtons() {
    $(".gameButton").attr("disabled", true);
}

function refreshGameStatus() {
    $.ajax({
        url: gameDataServletURL,
        dataType: "json",
        success: function(gameDataResponse){
            /*
                gameDataResponse is of the form:
                {
                    "title" :" sample game",
                    "variant" : "Circular",
                    "target" : 4,
                    "boardWidth":7,
                    "boardHeight":6,
                    "nameOfTheCreator" : "Mushon",
                    "gameInProgress" : false,
                    "currentPlayerCount" : 1,
                    "requiredPlayerCount" : 2
                }
            */
            $("#gameStatusText").text
            (gameDataResponse.gameInProgress ? "Game in progress" : "Waiting for players...");

            $("#playerCountText").text
            (gameDataResponse.currentPlayerCount + "/" + gameDataResponse.requiredPlayerCount);

            let checkWhetherToStart = function() {
                if(gameDataResponse.gameInProgress && !game.isInProgress && !game.isOver) {
                    game.isInProgress = true;
                    startGame();
                }
            };

            // Allows the html to update before potentially starting the game
            setTimeout(checkWhetherToStart, 500);
        }
    });

    // Calls itself
    setTimeout(refreshGameStatus, refreshRate);
}

function startGame() {
    game.isInProgress = true;
    setUserPrompt();

    updater.addToUpdatesQueue("Everyone is here, let the game begin!");

    if(userIsFirstTurnHolder() && !user.isComputer) {
        unlockPopinButtons()

    } else {
        lockLeaveLink();
    }

    if (userIsFirstTurnHolder() && user.isComputer) {

        // This will set updateGameStateInterval when it's done
        setComputerMoveCountdown();

    } else {

        // Every call synchronizes the board and who's the turn holder.
        // If the user becomes the turn holder it will prompt a play and unlock the relevant buttons.
        // If the user that gets the turn is a computer - it will initiate a countdown to an automatic move.
        updateGameStateIntervalId = setInterval(checkAndUpdateGameStateChanges, refreshRate) ;
    }
}

function unlockPopinButtons() {
    $(".popinButton").attr("disabled", false);
}

function setUserPrompt() {
    $("#userPromptDiv").css("visibility", "visible");

    if(userIsFirstTurnHolder()){
        promptUserToPlay();

    } else {
        promptUserToWait();
    }
}

function userIsFirstTurnHolder() {
    let firstTurnHolderName = $(".playerDetailsDiv:first .playerNameText").text();
    return user.name === firstTurnHolderName;
}

function promptUserToPlay() {
    $("#userPromptText").text(promptPlayMessage);

    let userPromptDiv = $("#userPromptDiv");
    userPromptDiv.removeClass(); // Removes all css classes
    userPromptDiv.addClass("playPrompt");
}

function promptUserToWait() {
    $("#userPromptText").text(promptWaitMessage);

    let userPromptDiv = $("#userPromptDiv");
    userPromptDiv.removeClass(); // Removes all css classes
    userPromptDiv.addClass("waitPrompt");
}

function checkAndUpdateGameStateChanges() {
    $.ajax({
        url: gameInProgressStateServletURL,
        dataType: 'json',
        data: { turnsUpdatedAtUserParameter : user.turnsUpdated },
        success: function(gameStateResponse) {
            /*
                gameStateResponse is of the form:
                {
                    "newTurnsAvailable" : true,
                    "movesToUpdate" :
                        [
                            {
                            "nestedMoveType" : "RegularMove",
                            "nestedMove" :
                                {
                                "restingRow" : 5,
                                "discNum" : 1,
                                "selectedCol" : 5
                                }
                            },
                            {
                            "nestedMoveType" : "PopoutMove",
                            "nestedMove" :
                                {
                                "discNum" : 1,
                                "selectedCol" : 5
                                }
                            }
                        ],
                    "turnHolder" :
                        {
                            "discNum" : 1,
                            "playerName" : "Ori",
                        },
                    "isGameOver":false,
                    "canPopinArr" :
                        [
                            true,
                            false,
                            ...
                        ],
                    "canPopoutArr" :
                        [
                            false,
                            false,
                            ...
                        ]
                }
             */
            if (gameStateResponse.newTurnsAvailable) {
                refreshPlayersList();
                updateBoard(gameStateResponse.movesToUpdate);

                if (gameStateResponse.isGameOver) {
                    endGame(gameStateResponse.nameOfTheWinner);
                }

                else if (gameStateResponse.turnHolder.playerName === user.name) {

                    if(game.isPopoutGame && noAvailableAction(gameStateResponse.canPopinArr, gameStateResponse.canPopoutArr)){
                        skipTurnDueToSpecialCase();
                    }

                    else if(!user.isComputer) {
                        unlockLeaveLink();
                        unlockRelevantPlayButtons(gameStateResponse.canPopinArr, gameStateResponse.canPopoutArr);
                        promptUserToPlay();
                    }

                    else {
                        setComputerMoveCountdown();
                    }
                }
            }
        }
    });
}

function noAvailableAction(canPopinResponseArr, canPopoutResponseArr) {
    let actionAvailable = false;

    // If canPopinResponseArr or canPopoutResponseArr contain one true
    // value, actionAvailable will be true.
    $.each(canPopinResponseArr || [], function (index, booleanValue) {
        actionAvailable = actionAvailable || booleanValue;
    });

    $.each( canPopoutResponseArr || [], function (index, booleanValue) {
        actionAvailable = actionAvailable || booleanValue;
    });

    return !actionAvailable;
}

function skipTurnDueToSpecialCase() {
    lockPlayButtons();
    lockLeaveLink();
    promptUserToWait();

    updater.addToUpdatesQueue("Your turn has been skipped because a play is not possible...");
    makeMove("Skip");
}


var computerMoveCountdownIntervalID;
var secondsToComputerMoveCountdown = COMPUTER_MOVE_COUNTDOWN_TIME_IN_SECONDS;
function setComputerMoveCountdown() {
    let userPromptDiv = $("#userPromptDiv");

    userPromptDiv.removeClass(); // Removes all css classes
    userPromptDiv.addClass("playPrompt");

    computerMoveCountdownIntervalID = setInterval(computerMoveCountdown, ONE_SECOND_IN_MS);
    $("#userPromptText").text(`Making computer move in ${secondsToComputerMoveCountdown}...`);
    secondsToComputerMoveCountdown--;
}

function computerMoveCountdown(){
    if(secondsToComputerMoveCountdown === 0){
        clearInterval(computerMoveCountdownIntervalID);
        makeMove("ComputerMove");
        promptUserToWait();
        secondsToComputerMoveCountdown = COMPUTER_MOVE_COUNTDOWN_TIME_IN_SECONDS;

    } else {
        $("#userPromptText").text(`Making computer move in ${secondsToComputerMoveCountdown}...`);
        secondsToComputerMoveCountdown--;
    }
}

function lockLeaveLink() {
    let leaveLink = $("#leaveLink");
    leaveLink.removeClass("activeLink");
    leaveLink.unbind("click");
}

function unlockLeaveLink() {
    let leaveLink = $("#leaveLink");
    leaveLink.addClass("activeLink");
    leaveLink.click(onLeaveLinkClick);
}

function makeQuitMove() {
    $.ajax({
       url: makeMoveServletURL,
        data: { moveType: "Quit"},
        success: function(){
           leaveToLobby();
        }
    });
}

function updateBoard(movesToUpdate) {
    /*
        movesToUpdate is of the form:
        [
            {
            "nestedMoveType" : "RegularMove",
            "nestedMove" :
                {
                    "restingRow" : 5,
                    "discNum" : 1,
                    "selectedCol" : 5
                }
            },
            {
                "nestedMoveType" : "PopoutMove",
                "nestedMove" :
                    {
                        "discNum" : 1,
                        "selectedCol" : 5
                    }
            },
            {
                "nestedMoveType" : "QuitMove",
                "nestedMove" :
                    {
                        "discNum" : 1,
                        "removedDiscsPositions" :
                        [
                            {
                                "x" : 2,
                                "y" : 1
                            },
                            {
                                "x" : 1
                                "y" : 1
                            }
                        ]
                    }
            }
        ]
     */
    $.each(movesToUpdate || [], function(index, moveWrapper){
        let nestedMove = moveWrapper.nestedMove;

        switch(moveWrapper.nestedMoveType){
            case "RegularMove":
                addNewDiscAt(nestedMove.restingRow, nestedMove.selectedCol, colorArr[nestedMove.discNum]);
                break;
            case "PopoutMove":
                removeDiscAtLocationAndCollapseDiscsAbove(game.boardHeight - 1, nestedMove.selectedCol);
                break;
            case "QuitMove":
                removeDiscsAtPositionsAndCloseSpaces(nestedMove.removedDiscsPositions);
                announcePlayerLeaving(nestedMove);
                break;
            case "SkipMove":
                announceTurnWasSkipped(moveWrapper.nestedMove.discNum);
                break;
        }

        user.turnsUpdated++;
    });
}

function announceTurnWasSkipped(discNum) {
    let colorOfPlayerWhosTurnWasSkipped = colorArr[discNum];
    if(user.color !== colorOfPlayerWhosTurnWasSkipped) {
        updater.addToUpdatesQueue(`The ${colorOfPlayerWhosTurnWasSkipped} player's turn was skipped because a move was not possible...`);
    }
}

function announcePlayerLeaving(quitMove) {
    /*
    quitMove is of the form:
    {
        "discNum" : 1,
        "removedDiscsPositions" :
        [
            {
                "x" : 2,
                "y" : 1
            },
            {
                "x" : 1
                "y" : 1
            }
        ]
    }
    */

    let colorOfLeaver = colorArr[quitMove.discNum];

    updater.addToUpdatesQueue(`The ${colorOfLeaver} player has quit!`);
}

function addNewDiscAt(row, col, colorStr) {
    let tdContainer = $(`td.row-${row}.col-${col}`);
    tdContainer.empty();
    tdContainer.append($(`<div class="${colorStr} disc"></div>`));
}

function removeDiscAtLocationAndCollapseDiscsAbove(selectedRow, selectedCol) {

    // Remove the specified disc
    $(`td.row-${selectedRow}.col-${selectedCol}`).empty();

    // Collapse the discs above it
    let done = false;
    let currentTd;
    let tdAbove;
    for(let row = selectedRow; row > 0 && !done; row--){
        currentTd = $(`td.row-${row}.col-${selectedCol}`);
        tdAbove = $(`td.row-${row - 1}.col-${selectedCol}`);

        if(tdAbove.children().length === 0){
            done = true;

        } else {
            let discToMove = tdAbove.find('div:first');
            currentTd.append(discToMove);
        }
    }
}

function removeDiscsAtPositionsAndCloseSpaces(positionsToRemoveDiscFrom) {
    /*
    positionsToRemoveDiscFrom is of the form:
    [
        {
            "x" : 1,
            "y" : 2
        },
        {
            "x" : 2,
            "y" : 2
        }
    ]
     */
    $.each(positionsToRemoveDiscFrom || [],
        (inex, pos) => removeDiscAtLocationAndCollapseDiscsAbove(pos.y, pos.x));
}

function endGame(nameOfTheWinner) {
    clearInterval(updateGameStateIntervalId);
    $(".turnHolder").removeClass("turnHolder");
    game.isOver = true;
    lockPlayButtons();
    hideUserPlayPrompt();

    if(nameOfTheWinner === undefined || nameOfTheWinner === null){
        displayStalemate();

    } else {
        displayWinner(nameOfTheWinner);
    }

    setCountdownToLobby();
}

function hideUserPlayPrompt() {
    $("#userPromptDiv").css("visibility", "hidden");
}

function displayStalemate() {
    $("#endGameText").text("Stalemate! Everyone's a loser!");
    $("#endGameImg").attr("src", stalemateImgURL);
    $("#endGameDiv").css("visibility", "visible");
}

function displayWinner(nameOfTheWinner) {
    $("#endGameText").text(nameOfTheWinner + " is the winner!!!");
    $("#endGameImg").attr("src", celebrationImgURL);
    $("#endGameDiv").css("visibility", "visible");
}

let secondsLeftToCountdown;
function setCountdownToLobby() {
    $("#overPageMask").css("visibility", "visible");
    $("#countdownPromptDiv").css("visibility", "visible");
    secondsLeftToCountdown = 10;
    $("#countdownTimerText").text(secondsLeftToCountdown);

    setInterval(countDownToLobby, ONE_SECOND_IN_MS);
}

function countDownToLobby(){
    if(secondsLeftToCountdown > 0){
        secondsLeftToCountdown--;
        $("#countdownTimerText").text(secondsLeftToCountdown);

    } else if(secondsLeftToCountdown === 0){

        // So only one player will send a reset request to the server
        if(userIsFirstTurnHolder()){
            resetGameEngineThenLeaveToLobby();

        } else {
            leaveToLobby();
        }
    }
}

function resetGameEngineThenLeaveToLobby() {
    $.ajax({
        url: resetGameEngineServletURL,
        dataType: 'json',
        success : leaveToLobby
    });
}

function leaveToLobby() {
    $.ajax({
        url: leaveGameServletURL,
        success : function(){
            window.location = lobbyPageURL;
        }
    });
}

function unlockRelevantPlayButtons(canPopinArr, canPopoutArr) {
    for(let col = 0 ; col < game.boardWidth; col++){
        canPopinObjectArr[col].value = canPopinArr[col];
    }

    if(game.isPopoutGame){
        for(let col = 0 ; col < game.boardWidth; col++){
            canPopoutObjectArr[col].value = canPopoutArr[col];
        }
    }
}