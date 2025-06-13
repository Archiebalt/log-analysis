package may.code.services.write;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface OperationWritingLogs {

    void writeLogs(Map<String, List<String>> userLogs, String directoryPath) throws IOException;

}
