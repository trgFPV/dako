package edu.hm.dako.chat.AuditLogServer;


import java.io.FileWriter;
import java.io.IOException;

import edu.hm.dako.chat.common.AuditLogPDU;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CSVAuditLogWriter {
    static String FILENAME = "output.csv";

    private FileWriter out;
    private CSVPrinter csvout;

    public CSVAuditLogWriter() {

        try {
            out = new FileWriter(FILENAME);
            CSVPrinter csvout = new CSVPrinter(out, CSVFormat.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeAuditLogPDU(AuditLogPDU alp) {
        String [] record = {
                alp.getClientThreadName().toString(),
                alp.getMessage().toString(),
                alp.getServerThreadName().toString(),
                alp.getUserName().toString(),
                String.valueOf(alp.getAuditTime()),
                String.valueOf(alp.getPduType())
        };
        try {
            csvout.printRecord(record);
            csvout.flush();
        } catch (IOException ie) {
            System.out.println("Fehler beim CSV schreiben");
        }
         catch (NullPointerException npe) {
            System.out.println("NPE");
         }
    }

}

