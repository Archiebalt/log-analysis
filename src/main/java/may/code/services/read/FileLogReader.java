package may.code.services.read;

import may.code.services.parse.LogParser;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileLogReader implements OperationReadingLogs {

    @Override
    public Map<String, List<String>> readLogs(String directoryPath) throws IOException {

        if (directoryPath == null || directoryPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Directory path cannot be null or empty");
        }

        Path targetFolder = Path.of(directoryPath).normalize();

        if (!Files.exists(targetFolder)) {
            throw new IOException("Directory does not exist: " + targetFolder);
        }

        if (Files.isRegularFile(targetFolder)) {
            throw new IOException("Path is not a directory: " + targetFolder);
        }

        Map<String, List<String>> userLogs = new HashMap<>();

        try (DirectoryStream<Path> files = Files.newDirectoryStream(targetFolder)) {
            for (Path path : files) {

                if (!Files.isReadable(path)) {
                    throw new AccessDeniedException("No read permission for file " + path);
                }

                if (Files.isRegularFile(path)) {
                    Files.lines(path).forEach(line -> processLine(line, userLogs));
                }
            }
        } catch (IOException e) {
            throw new IOException("Failed to read directory: " + targetFolder, e);
        }
        return userLogs;
    }

    private void processLine(String line, Map<String, List<String>> userLogs) {
        String user = LogParser.getUser(line);
        String operation = LogParser.getUserOperation(line);

        List<String> logs = userLogs.getOrDefault(user, new ArrayList<>());
        logs.add(line);
        userLogs.put(user, logs);

        if (operation.contains("transferred")) {
            String theUserToWhomTheMoneyWasTransferred = getReceiptFromAnotherUser(operation);

            logs = userLogs.getOrDefault(theUserToWhomTheMoneyWasTransferred, new ArrayList<>());
            logs.add(createTransferLog(line));

            userLogs.put(theUserToWhomTheMoneyWasTransferred, logs);
        }
    }

    private String getReceiptFromAnotherUser(String str) {
        return LogParser.getUserOperation(str).substring(LogParser.getUserOperation(str).indexOf("to") + 2).trim();
    }

    private String createTransferLog(String str) {
        String dataTransfer = LogParser.getOperationTime(str);
        String userWhoTransferred = LogParser.getUser(str);
        String userToWhomTheTransferWasMade = getReceiptFromAnotherUser(str);
        String transferAmount = getTransferAmount(LogParser.getUserOperation(str));

        return "[" + dataTransfer + "]" + " " + userToWhomTheTransferWasMade + " recived " + transferAmount + " from " + userWhoTransferred;
    }

    private String getTransferAmount(String str) {
        Pattern pattern = Pattern.compile("\\d+\\.\\d+\\b");
        Matcher matcher = pattern.matcher(str);
        return matcher.find() ? matcher.group() : null;
    }

}
