package utils;

public class GlobalConfig {

    public static final String SERVER_NAME = "localhost";
    public static final String CONFIG_FOLDER = "config/";
    public static final String USER_FILES = "user-files";
    public static final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

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
