package may.code.services.calculate;

import may.code.services.parse.LogParser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BalanceCalculator implements OperationCalculatingLogs{

    @Override
    public void calculateBalances(Map<String, List<String>> userLogs) {
        userLogs.forEach((user, logs) -> {
            double currentSum = 0;

            for (String log : logs) {
                String operation = LogParser.getUserOperation(log).substring(0, LogParser.getUserOperation(log).indexOf(" ")).trim();

                Pattern pattern = Pattern.compile("(?<=\\s)\\d+\\.?\\d+");
                Matcher matcher = pattern.matcher(LogParser.getUserOperation(log));

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

}
