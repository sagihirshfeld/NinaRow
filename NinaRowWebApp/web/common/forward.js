var loginPageURL = buildUrlWithContextPath("pages/login/login.html");
var lobbyPageURL = buildUrlWithContextPath("pages/lobby/lobby.html");
var gamePageURL = buildUrlWithContextPath("pages/game/game.html");

forwardUserIfLogged();

function forwardUserIfLogged() {
    $.ajax({
        url: buildUrlWithContextPath("UserStatusServlet"),
        dataType: 'json',
        success: function(userStatus) {
            /*
             userStatus is of the form:
             {
                    "loggedInName": "Mushon",
                    "isConnected": true
                    "isPlaying": false
                    "gameIndex": 3
             }
            */
            let forwardToURL;

            if(userStatus.isConnected && userStatus.isPlaying){
                forwardToURL = gamePageURL;

            } else if(userStatus.isConnected){
                forwardToURL = lobbyPageURL;

            } else {
                forwardToURL = loginPageURL;
            }

            if(window.location.pathname !== forwardToURL){
                window.location = forwardToURL;
            }
        },

        // Makes sure the forward will happen before the page loads
        async: false
    });
}
