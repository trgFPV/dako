package edu.hm.dako.chat.AuditLogServer;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import edu.hm.dako.chat.common.AuditLogPDU;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CSVAuditLogWriter {
    private static String FILENAME = "AuditLogOutput.csv";


    public CSVAuditLogWriter() {


    }

    public void writeAuditLogPDU(AuditLogPDU alp) {
        BufferedWriter writer;
        CSVPrinter csvout;
        try {
             writer = Files.newBufferedWriter(
                    Paths.get(FILENAME),
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE);
            //out = new FileWriter(FILENAME);

            csvout = new CSVPrinter(writer, CSVFormat.DEFAULT);


            String[] record = {
                    alp.getClientThreadName(),
                    alp.getMessage(),
                    alp.getServerThreadName(),
                    alp.getUserName(),
                    String.valueOf(alp.getAuditTime()),
                    String.valueOf(alp.getPduType())
            };


            for (String s : record) {
                System.out.print(s + " ");
            }
            System.out.println("");

            csvout.printRecord(record);
            csvout.flush();
            csvout.close();
            writer.flush();
            writer.close();

        } catch (IOException ie) {
            System.out.println("Fehler beim CSV schreiben");
        } catch (NullPointerException npe) {
            System.out.println("NPE in CSV");
        }

}

}

