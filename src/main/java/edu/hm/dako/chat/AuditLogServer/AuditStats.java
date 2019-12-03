package edu.hm.dako.chat.AuditLogServer;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Reads a CSV File and generates some statistics
 */
public class AuditStats {

    private static String fileName = "auditlogs/ChatAuditLog_TCP.csv";
    private static int COLUMNS = 7;

    public static void main(String... args) {

        if (args.length > 2) {
            System.out.println("Invalid Arguments. Usage: AuditStats <filename.csv>");
            System.exit(-1);
        }

        if (args.length == 1) {
          fileName = args[0];
        }


        System.out.println("File: " + fileName);
        Iterable<CSVRecord> records = getCSVRecord();
        ArrayList<Stats> stats = generateStats(records);
        printStats(stats);


    }

    private static Iterable<CSVRecord> getCSVRecord() {
        Reader in = null;
        try {
            in = new FileReader(fileName);
        } catch (FileNotFoundException e) {
            System.out.println("Could not find file: '" + fileName +"'");
            System.exit(-1);
        }

        Iterable<CSVRecord> records = null;
        try {
            records = CSVFormat.DEFAULT.parse(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return records;
    }

    private static ArrayList<Stats> generateStats(Iterable<CSVRecord> records) {

        ArrayList<Stats> stats = new ArrayList<>();
        int AuditLogPduCounter = 0;
        int loginPDUCounter = 0;
        int chatPDUCounter = 0;
        int logoutPDUCounter = 0;
        int chatMessageLength = 0;
        int invalidRecords = 0;
        long totalAuditTime = 0;
        long highestAuditTime = 0;
        long lowestAuditTime = 0;
        Date chatTime;
        Date logTime;


        ArrayList<String> seenUserNames = new ArrayList<>();

//      0           1        2                 3         4         5            6
//      Threadname, Message, ServerThreadname, UserName, Audit Time, PduType, LogTime
        for (CSVRecord record : records) {

            if (record.size() != COLUMNS) {
                invalidRecords = invalidRecords + 1;
                break;
            }

            AuditLogPduCounter = AuditLogPduCounter + 1;

            String userName = record.get(3);

            if (!seenUserNames.contains(userName))
                seenUserNames.add(userName);

            String pduType = record.get(5).replace("\"", "").trim();

            switch (pduType) {
                case "Login":
                    loginPDUCounter = loginPDUCounter + 1;
                    break;

                case "Chat":
                    chatPDUCounter = chatPDUCounter + 1;
                    chatMessageLength = chatMessageLength + record.get(1).length();
                    break;

                case "Logout":
                    logoutPDUCounter = logoutPDUCounter + 1;
                    break;

                default:
                    break;
            }

            SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
            try {
                chatTime = simple.parse(record.get(4));
                logTime = simple.parse(record.get(6));
                long currentAuditTime = Math.abs(chatTime.getTime() - logTime.getTime());

                if (currentAuditTime > highestAuditTime)
                    highestAuditTime = currentAuditTime;

                if (currentAuditTime < lowestAuditTime)
                    lowestAuditTime = currentAuditTime;

                totalAuditTime = totalAuditTime + currentAuditTime;
            } catch (ParseException pe) {
                System.out.println("Warning: could not parse Dates!");
            }
        }

        stats.add(new Stats("Number of PDUs", AuditLogPduCounter));
        stats.add(new Stats("Number of LoginPDUs", loginPDUCounter));
        stats.add(new Stats("Number of LogoutPDUs", logoutPDUCounter));
        stats.add(new Stats("Number of ChatPDUs", chatPDUCounter));
        stats.add(new Stats("Number of Invalid CSV Records", invalidRecords));
        stats.add(new Stats("Total Length of MSG", chatMessageLength));
        stats.add(new Stats("Avg. Chat message Length", chatMessageLength / chatPDUCounter));
        stats.add(new Stats("Total # of unique users", seenUserNames.size()));
        stats.add(new Stats("Average Audit Delay [ms]", totalAuditTime / AuditLogPduCounter));
        stats.add(new Stats("Highest delay [ms]", highestAuditTime));
        stats.add(new Stats("Lowest delay [ms]", lowestAuditTime));

        return stats;

    }

  /**
   * Print the stats in a beautiful way on the console.
   * @param stats Arraylist with Stats objects
   */
  private static void printStats(ArrayList<Stats> stats) {
        int longestDescription = 0;
        for (Stats stat : stats) {
            if (stat.getDescription().length() > longestDescription)
                longestDescription = stat.getDescription().length();
        }

        for (Stats stat : stats) {
            int padding = longestDescription - stat.getDescription().length();
            System.out.print(stat.getDescription());
            System.out.print(": ");
            while (padding > 0) {
                System.out.print(" ");
                padding = padding - 1;
            }
            System.out.print(stat.getValue() + "\n");
        }
    }

}

class Stats {
    private String description;
    private String value;

    public Stats(String description, String value) {
        this.description = description;
        this.value = value;
    }

    public Stats(String description, Integer value) {
        this.description = description;
        this.value = value.toString();
    }

    public Stats(String description, Long value) {
        this.description = description;
        this.value = value.toString();
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return description + " " + value;
    }
}

