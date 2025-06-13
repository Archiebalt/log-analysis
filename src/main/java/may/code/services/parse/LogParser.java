package may.code.services.parse;

public class LogParser {

    private LogParser() {
    }

    public static String getUserOperation(String str) {
        int startIndex = str.indexOf(getUser(str)) + getUser(str).length();
        return str.substring(startIndex + 1);
    }


    public static String getUser(String str) {
        String bracket = str.substring(str.indexOf("]") + 1).trim();
        return bracket.substring(0, bracket.indexOf(" "));
    }


    public static String getOperationTime(String str) {
        return str.substring(str.indexOf("[") + 1, str.indexOf("]"));
    }

}
