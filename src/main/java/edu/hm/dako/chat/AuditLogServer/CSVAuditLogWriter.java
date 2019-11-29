package edu.hm.dako.chat.AuditLogServer;


import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.hm.dako.chat.common.AuditLogPDU;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 *
 */
public class CSVAuditLogWriter {
    private String filename = "AuditLogOutputTCP.csv";
    private static String[] HEADER = {"ThreadName", "Message", "ServerThreadName", "UserName", "AuditTime", "PduType", "LogTime"};
    private final DateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

    public CSVAuditLogWriter() {
        writeHeader(HEADER);
    }

    public CSVAuditLogWriter(String filename) {
        this.filename = filename;
        writeHeader(HEADER);
    }

    public void writeHeader(String[] header) {
        BufferedWriter writer;
        CSVPrinter csvout;
        try {
            writer = Files.newBufferedWriter(
                    Paths.get(filename),
                    StandardOpenOption.CREATE);

            csvout = new CSVPrinter(writer, CSVFormat.DEFAULT);

            csvout.printRecord(header);
            csvout.flush();
            csvout.close();

        } catch (IOException ie) {
            System.out.println("Fehler beim CSV schreiben");
            ie.printStackTrace();
        } catch (NullPointerException npe) {
            System.out.println("NPE in CSV");
        }
    }

    public void writeAuditLogPDU(AuditLogPDU alp) {
        BufferedWriter writer;
        CSVPrinter csvout;
        try {
            writer = Files.newBufferedWriter(
                    Paths.get(filename),
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE);

            csvout = new CSVPrinter(writer, CSVFormat.DEFAULT);
            Date msgDate = new Date(alp.getAuditTime());
            Date logDate = new Date();

            String[] record = {
                    alp.getClientThreadName(),
                    alp.getMessage(),
                    alp.getServerThreadName(),
                    alp.getUserName(),
                    simple.format(msgDate),
                    String.valueOf(alp.getPduType()),
                    simple.format(logDate)
            };

            for (String s : record) {
                System.out.print(s + " | ");
            }
            System.out.println("");

            csvout.printRecord(record);
            csvout.flush();
            csvout.close();

        } catch (IOException ie) {
            System.out.println("Error while writing CSV");
            ie.printStackTrace();
        }

    }
}

