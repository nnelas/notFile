package utils;

public class GlobalConfig {

    public static final String SERVER_NAME = "localhost";
    public static final String CONFIG_FOLDER = "config/";
    public static final String USER_FILES = "user-files";
    public static final String PUBSUB_LOG = "pub-sub.log";

    public static final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    public static final String SERVER_CONFIG_FOLDER = "server";
    public static final String TABLE_USERS = "table-users";
    public static final String TABLE_TEAMS = "table-teams";
    public static final int SERVER_PORT = 9000;

    public static final String USER_LOGIN = "login";
    public static final String USER_REGISTER = "register";

    public static final String REQUEST_DATA_SET = "request";
    public static final String ALERT_REQUESTED_DATA_SET = "alert";
    public static final String ADVERTISE_DATA_SET = "advertise";
    public static final String NOTIFY_INTERESTED_DATA_SET = "notify";

    public static void clearConsole() {
        try {
            final String os = System.getProperty("os.name");

            if (os.contains("Windows")) {
                Runtime.getRuntime().exec("cls");
            } else {
                Runtime.getRuntime().exec("clear");
            }
        } catch (final Exception e) {
            //  Handle any exceptions.
        }
    }
}
