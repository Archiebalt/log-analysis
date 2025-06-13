package may.code.services.write;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

public class FileLogWriter implements OperationWritingLogs {

    @Override
    public void writeLogs(Map<String, List<String>> userLogs, String directoryPath) throws IOException {

        Path userLogsDir = Path.of(directoryPath).resolve("transactions_by_users");

        if (!Files.exists(userLogsDir)) {
            Files.createDirectory(userLogsDir);
        }

        userLogs.forEach((user, logs) -> {

            try {
                Path userLogFile = userLogsDir.resolve(user + ".log");
                Files.writeString(userLogFile, "", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                for (String log : logs) {
                    Files.writeString(userLogFile, log + "\n", StandardOpenOption.APPEND);
                }
            } catch (IOException exc) {
                throw new RuntimeException("Failed to write logs for user: " + user, exc);
            }

        });

    }
}
