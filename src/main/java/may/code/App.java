package may.code;

import may.code.services.TransactionProcessor;
import may.code.services.calculate.BalanceCalculator;
import may.code.services.read.FileLogReader;
import may.code.services.sort.LogSorter;
import may.code.services.write.FileLogWriter;

import java.io.IOException;

public class App {

    public static void main(String[] args) throws IOException {

        TransactionProcessor transactionProcessor = new TransactionProcessor(
                new FileLogReader(),
                new LogSorter(),
                new BalanceCalculator(),
                new FileLogWriter()
        );

        // Вставить абсолютный путь к директории
        transactionProcessor.process("C:\\Users\\ARTHUR\\Desktop\\Logs");
    }

}
