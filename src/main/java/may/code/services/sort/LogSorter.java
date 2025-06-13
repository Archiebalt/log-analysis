package may.code.services.sort;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LogSorter implements OperationSortingLogs {

    @Override
    public void sortLogs(Map<String, List<String>> userLogs) {

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

                private String getOperationTime(String str) {
                    return str.substring(str.indexOf("[") + 1, str.indexOf("]"));
                }

            });

        });

    }

}
