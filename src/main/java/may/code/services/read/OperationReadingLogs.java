package may.code.services.read;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface OperationReadingLogs {

    Map<String, List<String>> readLogs(String directoryPath) throws IOException;

}
