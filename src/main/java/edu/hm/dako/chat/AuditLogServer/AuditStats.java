package edu.hm.dako.chat.AuditLogServer;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.ArrayList;

public class AuditStats {
    public static void  main (String...args) {
        Reader in = null;
        try {
            in = new FileReader("AuditLogOutput.csv");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Iterable<CSVRecord> records = null;
        try {
            records = CSVFormat.DEFAULT.parse(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int AuditLogPduCounter = 0;
        int loginPDUCounter = 0;
        int chatPDUCounter = 0;
        int logoutPDUCounter = 0;
        int chatMessageLength = 0;
        int invalidRecords = 0;
        ArrayList<String> seenUserNames = new ArrayList<>();
//      0           1        2                 3         4         5
//      Threadname, Message, ServerThreadname, UserName, Audit Time, PduType
        for (CSVRecord record : records) {

                if (record.size() != 6) {
                    invalidRecords = invalidRecords + 1;
                    break;
                }

                AuditLogPduCounter = AuditLogPduCounter + 1;

                String userName = record.get(3);

                if (!seenUserNames.contains(userName))
                    seenUserNames.add(userName);

                String pduType = record.get(5).replace("\"","").trim();

                switch(pduType)  {
                    case "Login": loginPDUCounter = loginPDUCounter + 1;
                    break;

                    case "Chat": chatPDUCounter = chatPDUCounter + 1;
                    chatMessageLength = chatMessageLength + record.get(1).length();
                    break;

                    case "Logout": logoutPDUCounter = logoutPDUCounter + 1;
                    break;

                    default: break;
                }



            System.out.println("");
        }

        System.out.println("Number of PDUs: " + AuditLogPduCounter);
        System.out.println("Number of LoginPDUs: " + loginPDUCounter);
        System.out.println("Number of LogoutPDUs: " + logoutPDUCounter);
        System.out.println("Number of ChatPDUs: " + chatPDUCounter);
        System.out.println("Number of invalid CSV-Records: " + invalidRecords);
        System.out.println("Total length of Message: " + chatMessageLength);
        System.out.println("Average Chat-Message Length " + chatMessageLength/chatPDUCounter);
        System.out.println("Total # of individual users: " + seenUserNames.size());

    }


}