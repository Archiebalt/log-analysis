package may.code;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class App {

    public static void main(String[] args) throws IOException {

        Path targetFolder = Path.of(args[0]);

        if (!Files.isDirectory(targetFolder)) {
            throw new IOException(targetFolder + " - не является директорией");
        }

        Path userLogsDir = targetFolder.resolve("transactions_by_users");
        if (!Files.exists(userLogsDir)) {
            Files.createDirectory(userLogsDir);
        }

        HashMap<String, List<String>> userLogs = new HashMap<>();

        try (DirectoryStream<Path> files = Files.newDirectoryStream(targetFolder)) {

            for (Path path : files) {

                if (Files.isRegularFile(path)) {

                    Files.lines(path).forEach(line -> {
                        String user = getUser(line);
                        String operation = getUserOperation(line);

                        List<String> logs = userLogs.getOrDefault(user, new ArrayList<>());
                        logs.add(line);
                        userLogs.put(user, logs);

                        if (operation.contains("transferred")) {
                            String theUserToWhomTheMoneyWasTransferred = getReceiptFromAnotherUser(operation);

                            logs = userLogs.getOrDefault(theUserToWhomTheMoneyWasTransferred, new ArrayList<>());
                            logs.add(createTransferLog(line));

                            userLogs.put(theUserToWhomTheMoneyWasTransferred, logs);
                        }

                    });

                }

            }

        } catch (IOException exc) {
            exc.getStackTrace();
        }

        sortingLogsByTime(userLogs);
        calculatingUserBalanceValue(userLogs);
        writingLogsToFile(userLogs, userLogsDir);

        userLogs.forEach((user, logs) -> {
            System.out.println("Пользователь: " + user);
            logs.forEach(log -> System.out.println("  - " + log));
            System.out.println();
        });

    }

    private static void calculatingUserBalanceValue(Map<String, List<String>> userLogs) {

        userLogs.forEach((user, logs) -> {
            double currentSum = 0;

            for (String log : logs) {
                String operation = getUserOperation(log).substring(0, getUserOperation(log).indexOf(" ")).trim();

                Pattern pattern = Pattern.compile("(?<=\\s)\\d+\\.?\\d+");
                Matcher matcher = pattern.matcher(getUserOperation(log));

                double transactionAmount = 0;

                if (matcher.find()) {
                    transactionAmount = Double.parseDouble(matcher.group());
                }

                switch (operation) {
                    case "transferred":
                    case "withdrew":
                        currentSum -= transactionAmount;
                        break;
                    case "recived":
                        currentSum += transactionAmount;
                        break;
                    case "balance":
                        currentSum = transactionAmount;
                        break;
                }
            }

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String finalBalanceLog = "[" + formatter.format(new Date()) + "]" + " " + user + " final balance " + currentSum;

            logs = userLogs.getOrDefault(user, new ArrayList<>());
            logs.add(finalBalanceLog);
            userLogs.put(user, logs);

        });

    }

    private static void sortingLogsByTime(Map<String, List<String>> userLogs) {

        userLogs.forEach((user, logs) -> {

            Collections.sort(logs, new Comparator<String>() {

                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                @Override
                public int compare(String o1, String o2) {
                    try {
                        Date date1 = parser.parse(getOperationTime(o1));
                        Date date2 = parser.parse(getOperationTime(o2));
                        return date1.compareTo(date2);
                    } catch (ParseException exc) {
                        exc.getStackTrace();
                    }
                    return 0;
                }

            });

        });

    }

    private static void writingLogsToFile(Map<String, List<String>> userLogs, Path userLogsDir) {

        sortingLogsByTime(userLogs);

        userLogs.forEach((user, logs) -> {

            try {
                Path userLogFile = userLogsDir.resolve(user + ".log");

                Files.writeString(userLogFile, "", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                for (String log : logs) {
                    Files.writeString(userLogFile, log + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                }

            } catch (IOException exc) {
                throw new RuntimeException("Failed to write logs for user: " + user, exc);
            }

        });

    }

    private static String getReceiptFromAnotherUser(String str) {
        return getUserOperation(str).substring(getUserOperation(str).indexOf("to") + 2).trim();
    }

    private static String createTransferLog(String str) {
        String dataTransfer = getOperationTime(str);
        String userWhoTransferred = getUser(str);
        String userToWhomTheTransferWasMade = getReceiptFromAnotherUser(str);
        String transferAmount = getTransferAmount(getUserOperation(str));

        return "[" + dataTransfer + "]" + " " + userToWhomTheTransferWasMade + " recived " + transferAmount + " from " + userWhoTransferred;
    }

    private static String getTransferAmount(String str) {
        Pattern pattern = Pattern.compile("\\d+\\.\\d+\\b");
        Matcher matcher = pattern.matcher(str);

        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }

    private static String getUserOperation(String str) {
        int startIndex = str.indexOf(getUser(str)) + getUser(str).length();
        return str.substring(startIndex + 1);
    }

    private static String getUser(String str) {
        String bracket = str.substring(str.indexOf("]") + 1).trim();
        return bracket.substring(0, bracket.indexOf(" "));
    }

    private static String getOperationTime(String str) {
        return str.substring(str.indexOf("[") + 1, str.indexOf("]"));
    }

}
