$(function () {
    $("#usernameTextbox").on('input', onTextBoxChange);
    $("#loginForm").submit(onSubmit);
});

function onTextBoxChange() {
    let textBoxContent = $(this).val();
    let loginButton = $("#loginButton");

    if(textBoxContent === "") {
        loginButton.attr("disabled", true);
        $(".placeHolder").css("visibility", "visible");

    } else {
        loginButton.attr("disabled", false);
        $(".placeHolder").css("visibility", "hidden");

    }
}

function onSubmit(){
    let parameters = $(this).serialize();

    $.ajax({
        data: parameters,
        url: loginServletURL,
        error: displayConnectionError,
        success: handleServerResponse
    });

    return false;
}

function handleServerResponse(jsonResponse) {
    let userNameTextBox = $("#usernameTextbox");
    let nameAlreadyExists = jsonResponse;
    let enteredUserName = userNameTextBox.val();

    userNameTextBox.val("");

    if(nameAlreadyExists){
        $("#errorMessage").text(` "${enteredUserName}" already exists. Please enter a different name.`);

    } else {
        window.location = lobbyPageURL;
    }
}

function displayConnectionError(){
    $("#errorMessage").text("Failed connecting to the server...");
}