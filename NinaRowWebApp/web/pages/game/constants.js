var userStatusServletURL = buildUrlWithContextPath("UserStatusServlet ");
var playersListServletURL = buildUrlWithContextPath("PlayersListServlet");
var gameDataServletURL = buildUrlWithContextPath("GameDataServlet");
var makeMoveServletURL = buildUrlWithContextPath("MakeMoveServlet");
var gameInProgressStateServletURL = buildUrlWithContextPath("GameInProgressStateServlet");
var leaveGameServletURL = buildUrlWithContextPath("LeaveGameServlet");
var resetGameEngineServletURL = buildUrlWithContextPath("ResetGameEngineServlet");

var lobbyPageURL = buildUrlWithContextPath("pages/lobby/lobby.html");

var celebrationImgURL = buildUrlWithContextPath("resources/images/celebration.jpg");
var stalemateImgURL = buildUrlWithContextPath("resources/images/stalemate.jpg");

var ONE_SECOND_IN_MS = 1000;
var COMPUTER_MOVE_COUNTDOWN_TIME_IN_SECONDS = 2;
var refreshRate = 1000; // mili seconds

var colorArr = ["none", "red", "blue", "green", "black", "silver", "purple"];

