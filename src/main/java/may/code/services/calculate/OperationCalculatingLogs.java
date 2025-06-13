package may.code.services.calculate;

import java.util.List;
import java.util.Map;

public interface OperationCalculatingLogs {

    void calculateBalances(Map<String, List<String>> userLogs);

}
