package servlets;

import utils.ServletUtils;
import OnlineClasses.UserStatus;
import utils.SessionUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

public class LogoutServlet extends HttpServlet {

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UserStatus userStatus = SessionUtils.getUserStatus(request);

        removeUserFromLoggedInUsers(userStatus.getLoggedInName());
        updateUserStatus(userStatus);
    }

    private void removeUserFromLoggedInUsers(String userName) {
        Set<String> loggedInUsers = ServletUtils.getLoggedInUsers(getServletContext());
        loggedInUsers.remove(userName);
    }

    private void updateUserStatus(UserStatus userStatus) {
        userStatus.setCurrentGameTitle(null);
        userStatus.setConnected(false);
        userStatus.setLoggedInName(null);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
