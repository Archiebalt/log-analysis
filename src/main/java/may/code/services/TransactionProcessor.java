package may.code.services;

import may.code.services.calculate.OperationCalculatingLogs;
import may.code.services.read.OperationReadingLogs;
import may.code.services.sort.OperationSortingLogs;
import may.code.services.write.OperationWritingLogs;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TransactionProcessor {

    private final OperationReadingLogs logReader;
    private final OperationSortingLogs logSorter;
    private final OperationCalculatingLogs balanceCalculator;
    private final OperationWritingLogs logWriter;

    public TransactionProcessor(OperationReadingLogs logReader,
                                OperationSortingLogs logSorter,
                                OperationCalculatingLogs balanceCalculator,
                                OperationWritingLogs logWriter) {

        this.logReader = logReader;
        this.logSorter = logSorter;
        this.balanceCalculator = balanceCalculator;
        this.logWriter = logWriter;

    }

    public void process(String directoryPath) throws IOException {

        Map<String, List<String>> userLogs = logReader.readLogs(directoryPath);

        logSorter.sortLogs(userLogs);

        balanceCalculator.calculateBalances(userLogs);

        logWriter.writeLogs(userLogs, directoryPath);
    }

}
