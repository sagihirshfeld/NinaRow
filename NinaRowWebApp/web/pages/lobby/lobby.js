$(function () {
    $("#logoutLink").click(onLogoutClicked);
    $("#uploadFileInput").change(onFileSelectedForUpload);
    $("#uploadGameForm").submit(onFileSubmit);
    displayUserDetails();

    // calls itself every 2 seconds
    refreshData();
});

function displayUserDetails() {
    $.ajax({
        url: userStatusServletURL,
        dataType: 'json',
        success: loadUserDetailsFromUserStatus
    });
}

function loadUserDetailsFromUserStatus(userStatus){
    /*
    userStatus is of the form:
    {
        "loggedInName": "Mushon",
        "isConnected": true
        "isPlaying": true
        "currentGameTitle": "small game"
     }
    */
    let userName =  userStatus.loggedInName;
    let isComputer = userStatus.isComputer;

    let isComputerMessage = isComputer ? " You're logged in as a computer player" : "";

    let userDetailsText = $("#userDetailsText");
    userDetailsText.text(`Welcome ${userName}! ${isComputerMessage}`);
}

function onLogoutClicked() {
    var userIsSure = confirm("Are you sure you want to logout?");

    if(userIsSure){
        logoutBackToLoginPage();
    }
}

function logoutBackToLoginPage() {
    $.ajax({
        url: logoutServletURL,
        error: displayConnectionError,
        success:function(){
            window.location = loginPageURL;
        }
    });
}

function refreshData() {
    refreshLoggedInUsersList();
    refreshGamesTable();
    setTimeout(refreshData, refreshRate);
}

function onFileSelectedForUpload() {
    $("#uploadFileSubmit").trigger("submit");
}

function onFileSubmit() {
    let uploadFileInput = document.getElementById("uploadFileInput");
    if(uploadFileInput.files.length !== 0){
        let xmlFile = document.getElementById("uploadFileInput").files[0];
        let formData = new FormData();
        formData.append("xmlFile", xmlFile);

        $.ajax({
            method: 'POST',
            url: uploadXmlServletURL,
            data: formData,
            processData: false, // Don't process the files
            contentType: false, // Set content type to false as jQuery will tell the server its a query string request
            timeout: 4000,
            error: onUploadError,
            success: refreshGamesTable
        });
    }

    return false;
}

function onUploadError(xhr, textStatus, errorThrown) {
    if(xhr.status === UNSUPPORTED_MEDIA_ERROR_CODE) {
        displayXmlErrorMessage(xhr.responseText);

    } else {
        displayConnectionError(xhr, textStatus, errorThrown);
    }
}

function displayXmlErrorMessage(xmlErrorMessage) {
    alert(xmlErrorMessage);
}

function refreshGamesTable() {
    $.ajax({
        url: gamesListServletURL,
        dataType: 'json',
        success: function(updatedGames){
            $("#gamesTableBody").empty();
            $.each(updatedGames || [], appendToGamesTable);
        }
    })
}

function appendToGamesTable(index, gameData) {
    /*
        gameData is of the form:
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

    $(`<tr id="${index}"</tr>`).appendTo($("#gamesTableBody"));
    $(`<td>${gameData.title}</td>`).appendTo(`#${index}`);
    $(`<td>${gameData.nameOfTheCreator}</td>`).appendTo(`#${index}`);
    $(`<td>${gameData.boardHeight}X${gameData.boardWidth}</td>`).appendTo(`#${index}`);
    $(`<td>${gameData.target}</td>`).appendTo(`#${index}`);
    $(`<td>${gameData.variant}</td>`).appendTo(`#${index}`);
    $(`<td>${gameData.currentPlayerCount}/${gameData.requiredPlayerCount}</td>`).appendTo(`#${index}`);

    let gameStatus = gameData.gameInProgress ? "In Progress" : "Waiting For Players ";
    $(`<td>${gameStatus}</td>`).appendTo(`#${index}`);

    $(`<td id="buttonTd${index}" class="buttonTds"></td>`).appendTo(`#${index}`);
    let joinButton = $("<button type='button' class='joinGameButtons'>Join Game</button>")
    joinButton.click(function(){
        joinGameButtonClick(gameData.title)
    });

    joinButton.attr("disabled", gameData.gameInProgress);
    joinButton.appendTo(`#buttonTd${index}`);
}

function joinGameButtonClick(gameTitle){
    $.ajax({
        url: joinGameServletURL,
        data: { gameTitleParameter : gameTitle },
        error: displayConnectionError,
        success: function() {
            window.location = gamePageURL;
        }
    });
}

function refreshLoggedInUsersList() {
    $.ajax({
        url: loggedUsersServletURL,
        dataTyp: 'json',
        success: loadUsersListFromJsonData
    });
}

function loadUsersListFromJsonData(users){
    $("#usersList").empty();
    /*
    users is of the form:
    [
        "firstName",
        "secondName,
        "thirdName"
    */
    $.each(users || [], function(index, username){
        $(`<li>${username}</li>`).appendTo($("#usersList"));
    });
}

function displayConnectionError(xhr, textStatus, errorThrown) {
    alert("Connection error...");
    console.log(`error code: ${xhr.status}: ${xhr.responseText}`);
}