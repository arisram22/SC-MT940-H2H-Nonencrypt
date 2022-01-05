package main.h2h.mt;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.RFC4180ParserBuilder;
import com.opencsv.exceptions.CsvException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

import org.joda.time.Days;
import org.joda.time.LocalDate;

public class ConvertMT940 {

//    private Logger logger;
    private Properties prop;
    private boolean status;
    private DecimalFormat decimalFormat;
    private static BCPGPFileAndFolder fileAndFolder = new BCPGPFileAndFolder();

    public void moveFileSplit(String name, File first, File end, String folderIn, String folderOut) {
        try {
            //Move In file to History
//            Files.move(Paths.get(prop.getProperty(first) + name), Paths.get(prop.getProperty(end) + name), StandardCopyOption.REPLACE_EXISTING);
            String[] nameSplit = name.split("\\.");
            Files.move(Paths.get(first.toString() + "\\" + name + ".txt"), Paths.get(end.toString() + "\\" + nameSplit[0] + "." + nameSplit[1]), StandardCopyOption.REPLACE_EXISTING);
            fileAndFolder.getLogger().log(Level.INFO, "File {0} moved to {1}", new Object[]{name, folderOut});
//            System.out.println(end.toString() + nameSplit[0] + "." + nameSplit[1] + " Split");
        } catch (IOException e) {
            String[] nameSplit = name.split("\\.");
            System.out.println(first.toString() + "\\" + name + ".txt");
            System.out.println(end.toString() + "\\" + nameSplit[0] + "." + nameSplit[1] + " Split");
            fileAndFolder.getLogger().severe(e.toString());
            fileAndFolder.getLogger().log(Level.SEVERE, "Failed to move file in folder {0} to {1}", new Object[]{folderIn, folderOut});
            System.exit(0);
        }
    }

    public void moveFile(String name, File first, File end, String folderIn, String folderOut) {
        try {
            //Move In file to History
            System.out.println(name + first + end + folderOut);
            Files.copy(Paths.get(first.toString() + "\\" + name), Paths.get(end.toString() + "\\" + name), StandardCopyOption.REPLACE_EXISTING);

            fileAndFolder.getLogger().log(Level.INFO, "File {0} moved to {1}", new Object[]{name, folderOut});
        } catch (IOException e) {
            System.out.println(first.toString() + "\\" + name + " sss");
            fileAndFolder.getLogger().severe(e.toString());
            fileAndFolder.getLogger().log(Level.SEVERE, "Failed to move file in folder {0} to {1}", new Object[]{folderIn, folderOut});
            System.exit(0);
        }
    }

    private String getCurrency(String code) {
        ArrayList<String[]> currencys = new ArrayList<>();
        currencys.add(new String[]{"AUD", "036"});
        currencys.add(new String[]{"NZD", "554"});
        currencys.add(new String[]{"EUR", "978"});
        currencys.add(new String[]{"GBP", "826"});
        currencys.add(new String[]{"JPY", "392"});
        currencys.add(new String[]{"SGD", "702"});
        currencys.add(new String[]{"HKD", "344"});
        currencys.add(new String[]{"CAD", "124"});
        currencys.add(new String[]{"CHF", "756"});
        currencys.add(new String[]{"IDR", "360"});

        for (String[] currency : currencys) {
            if (currency[1].equals(code)) {
                fileAndFolder.getLogger().info("Currency found");
                return currency[0];
            }
        }
        fileAndFolder.getLogger().warning("Currenct not found");
        return "";
    }

    private String dateConverter(String stringDate) {
        String tempDate = "";
        for (int i = 0; i < stringDate.length(); i++) {
            tempDate += stringDate.charAt(i);
            if (i == 3) {
                tempDate += "-";
            }
            if (i == 5) {
                tempDate += "-";
            }
        }
        return tempDate;
    }

    private String getAmount(String record) {
        Double tempAmmount = Double.parseDouble(record);
        String[] tempDecimal = decimalFormat.format(Math.round(tempAmmount)).split("\\.");
        return tempDecimal[0];
    }

//    convert  MT start
    public void convertMt(String mnemonic, File in, File MT, File backup) throws CsvException, IOException {
        fileAndFolder.createLog();
        String fileMnemonic = "MNEMONIC/MT9xx Mnemonic - Agung.csv";
        System.out.println(fileMnemonic);
        ArrayList<String> codeName1 = new ArrayList<>();
        List<List<String>> dataFileMnemonic = new ArrayList<>();

        try ( BufferedReader reader = new BufferedReader(new InputStreamReader(ConvertMT940.class.getResourceAsStream(fileMnemonic)));  CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build()) {
            fileAndFolder.getLogger().info("Start mapping file mnemonic");
            List<String[]> records = csvReader.readAll();
            for (String[] record : records) {
                codeName1.add(record[0]);
                dataFileMnemonic.add(Arrays.asList(record));
            }
            fileAndFolder.getLogger().info("Mapping file mnemonic done");

        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
            fileAndFolder.getLogger().severe(e.toString());
            JOptionPane.showMessageDialog(null, e);
            fileAndFolder.getLogger().severe("File mnemonic not found");
            System.exit(0);
        }

        File dir = in;
        System.out.println(in);
        if (!dir.exists()) {
            fileAndFolder.getLogger().severe("Folder IN not found");
            System.exit(0);
        }
        String[] fileList = dir.list();
        if (fileList.length == 0) {
            fileAndFolder.getLogger().warning("Nothing file to convert");
        }

        for (String name : fileList) {
            String fileIn = in.toString() + "\\" + name;
            System.out.println(fileIn);
            String[] outName = name.split("\\.");
            String fileOut = MT.toString() + "\\" + outName[0] + "_Converted.txt";
            String fileOutNoRecord = MT.toString() + "\\" + outName[0] + "_NoRecord.txt";
            File file = new File(fileIn);
            String fileInName = file.getName();

            status = false;
            try ( BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileIn), StandardCharsets.UTF_8));  CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1)
                    .withCSVParser(new RFC4180ParserBuilder().build())
                    .build();  OutputStream outputStream = new FileOutputStream(fileOut);  Writer writer = new OutputStreamWriter(outputStream);  CSVWriter csvWriter = new CSVWriter(writer, '\u0000', '\u0000', '\u0000', "");) {
                fileAndFolder.getLogger().log(Level.INFO, "Starting convert file {0}", name);

                String[] fileName = fileInName.split("_");
                String[] date = fileName[2].split("\\.");
                String dateBeforeString = "2020-01-01";
                String dateAfterString = dateConverter(date[0]);
                LocalDate dateBefore = LocalDate.parse(dateBeforeString);
                LocalDate dateAfter = LocalDate.parse(dateAfterString);
                long days = Days.daysBetween(dateBefore, dateAfter).getDays() + 1;
                List<String[]> records = csvReader.readAll();

                status = true;
                int count = 0;
                for (String[] record : records) {
                    count++;
//                Checking date
                    String[] trxDate = record[0].split(" ");
                    if (!date[0].equals(trxDate[0]) && status) {
                        fileAndFolder.getLogger().log(Level.SEVERE, "Transaction date file {0} not same", name);
                        fileAndFolder.getLogger().log(Level.SEVERE, "Failed converting file {0}", name);
//                        System.exit(0);
                        status = false;
                    }
//                    CHEKING RECOD LENGHT
                    if (record.length != 14 && status) {
                        fileAndFolder.getLogger().log(Level.SEVERE, "Error file structure {0}", name);
                        fileAndFolder.getLogger().log(Level.SEVERE, "Failed converting file {0}", name);
//                        System.exit(0);
                        status = false;
                    }
                }
                if (count == 0) {
                    fileAndFolder.getLogger().log(Level.WARNING, "No Record {0}", name);
                    status = false;
                    outputStream.close();
                    File oldfile = new File(fileOut);
                    File newfile = new File(fileOutNoRecord);
                    System.out.println(oldfile.getName());
                    oldfile.renameTo(newfile);

                    if (newfile.exists() && newfile.isFile()) {
                        oldfile.delete();

                    }

                }
                if (status) {

                    List<String[]> data = new ArrayList<String[]>();
                    data.add(new String[]{"\u0001{1:F01BANK DANAMON}{2:" + fileName[0] + "}{4:" + "\r\n"});
                    data.add(new String[]{":20:" + date[0].substring(2, 8) + "\r\n"});
                    data.add(new String[]{":25:" + fileName[1] + "\r\n"});
                    data.add(new String[]{":28C:" + String.format("%05d", Integer.parseInt(String.valueOf(days))) + "\r\n"});
                    String[] firstElement = records.get(0);
//            System.out.println(firstElement[1]);
                    String currency = getCurrency(firstElement[7]);
                    System.out.println(currency);
                    if (currency == null) {
                        fileAndFolder.getLogger().log(Level.WARNING, "Currency mot found {0}", name);
                    }

                    decimalFormat = new DecimalFormat("000000000000.00");
                    Double tempAmount = Double.parseDouble(firstElement[12]);
                    String amount = decimalFormat.format(tempAmount).replace(".", ",");
                    if (currency.equals("IDR") || currency.equals("JPY")) {
                        amount = getAmount(firstElement[12]);
//                        System.out.println("TES AMOUNT " + firstElement[12]);
                    }
                    data.add(new String[]{":60F:" + firstElement[9] + date[0].substring(2, 8) + currency + amount + "\r\n"});
                    for (String[] record : records) {
                        String[] code = record[4].split(":");
                        int indexCodeName = codeName1.indexOf(code[0]);
                        List<String> dataMnemonic = dataFileMnemonic.get(indexCodeName);
                        String codeName = dataMnemonic.get(1);

                        tempAmount = Double.parseDouble(record[8]);
                        amount = decimalFormat.format(tempAmount).replace(".", ",");
                        if (currency.equals("IDR") || currency.equals("JPY")) {
                            amount = getAmount(record[8]);
                        }

                        String[] dateTrx = record[0].split(" ");

                        data.add(new String[]{":61:" + dateTrx[0].substring(2, 8) + record[9]
                            + amount + codeName + record[11] + "\r\n"});
                        data.add(new String[]{":86:" + record[10] + "\r\n"});
                    }

                    String[] lastElement = records.get(records.size() - 1);
                    currency = getCurrency(lastElement[7]);

                    tempAmount = Double.parseDouble(lastElement[13]);
                    amount = decimalFormat.format(tempAmount).replace(".", ",");
                    if (currency.equals("IDR") || currency.equals("JPY")) {
                        amount = getAmount(lastElement[13]);
                    }

                    data.add(new String[]{":62F:" + lastElement[9] + date[0].substring(2, 8) + currency + amount + "\r\n"});
                    data.add(new String[]{"-}" + "\u0003\r\n"});
                    for (int i = 0; i < 2; i++) {
                        data.add(new String[]{"\r\n"});
                    }
                    csvWriter.writeAll(data, true);
                    csvWriter.flush();
                    fileAndFolder.getLogger().log(Level.INFO, "File {0} converted", name);
                    status = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
                fileAndFolder.getLogger().severe(e.toString());
                fileAndFolder.getLogger().log(Level.SEVERE, "Failed converting file {0}", name);
                status = false;
                System.exit(0);
            }

//            if (status == true) {
//                moveFile(name, in, backup, "IN", "BACKUP");
//                moveFileSplit(name, decryptResult, backup, "DECRYPT", "BACKUP");
//            }
        }
    }

    public void convertMtAdmf(String mnemonic, File in, File MT, File backup) throws CsvException, IOException {
        fileAndFolder.createLog();
        String fileMnemonic = "MNEMONIC/MT9xx Mnemonic - Agung.csv";
        System.out.println(fileMnemonic);
        ArrayList<String> codeName1 = new ArrayList<>();
        List<List<String>> dataFileMnemonic = new ArrayList<>();

        try ( BufferedReader reader = new BufferedReader(new InputStreamReader(ConvertMT940.class.getResourceAsStream(fileMnemonic)));  CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build()) {
            fileAndFolder.getLogger().info("Start mapping file mnemonic");
            List<String[]> records = csvReader.readAll();
            for (String[] record : records) {
                codeName1.add(record[0]);
                dataFileMnemonic.add(Arrays.asList(record));
            }
            fileAndFolder.getLogger().info("Mapping file mnemonic done");
        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
            fileAndFolder.getLogger().severe(e.toString());
            JOptionPane.showMessageDialog(null, e);
            fileAndFolder.getLogger().severe("File mnemonic not found");
            System.exit(0);
        }

        File dir = in;
        if (!dir.exists()) {
            fileAndFolder.getLogger().severe("Folder IN not found");
            System.exit(0);
        }
        String[] fileList = dir.list();
        if (fileList.length == 0) {
            fileAndFolder.getLogger().warning("Nothing file to convert");
        }

        for (String name : fileList) {
            String fileIn = in.toString() + "\\" + name;
            System.out.println(fileIn + "");
            String[] outName = name.split("\\.");
//            String fileOut = prop.getProperty("app.pathMt") + outName[0] + "_Converted.txt";
            String fileOut = MT.toString() + "\\" + outName[0] + "_Converted.txt";
            String fileOutNoRecord = MT.toString() + "\\" + outName[0] + "_NoRecord.txt";

            File file = new File(fileIn);
            String fileInName = file.getName();

            status = false;
            try ( BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileIn), StandardCharsets.UTF_8));  CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1)
                    .withCSVParser(new RFC4180ParserBuilder().build())
                    .build();  OutputStream outputStream = new FileOutputStream(fileOut);  Writer writer = new OutputStreamWriter(outputStream);  CSVWriter csvWriter = new CSVWriter(writer, '\u0000', '\u0000', '\u0000', "");) {
                fileAndFolder.getLogger().log(Level.INFO, "Starting convert file {0}", name);

                String[] fileName = fileInName.split("_");
                String[] date = fileName[2].split("\\.");
                System.out.println(date[0]);
                String dateBeforeString = "2020-01-01";
                String dateAfterString = dateConverter(date[0]);
                LocalDate dateBefore = LocalDate.parse(dateBeforeString);
                LocalDate dateAfter = LocalDate.parse(dateAfterString);
                long days = Days.daysBetween(dateBefore, dateAfter).getDays() + 1;

                List<String[]> records = csvReader.readAll();

                status = true;
                int count = 0;
                for (String[] record : records) {
                    count++;
//                    String[] trxDate = record[0].split(" ");
//                Checking date
//                    System.out.println(date[0]);
//                    System.out.println(trxDate[0]);
//                    if (!date[0].equals(trxDate[0]) && status) {
//                        fileAndFolder.getLogger().log(Level.SEVERE, "Transaction date file {0} not same", name);
//                        fileAndFolder.getLogger().log(Level.SEVERE, "Failed converting file {0}", name);
////                        System.exit(0);
//                        status = false;
//                    }
//
                    if (record.length != 14 && status) {
                        fileAndFolder.getLogger().log(Level.SEVERE, "Error file structure {0}", name);
                        fileAndFolder.getLogger().log(Level.SEVERE, "Failed converting file {0}", name);
//                        System.exit(0);
                        status = false;
                    }
                }
                if (count == 0) {
                    fileAndFolder.getLogger().log(Level.WARNING, "No Record {0}", name);
                    status = false;
                    outputStream.close();
                    File oldfile = new File(fileOut);
                    File newfile = new File(fileOutNoRecord);
                    System.out.println(oldfile.getName());
                    oldfile.renameTo(newfile);

                    if (newfile.exists() && newfile.isFile()) {
                        oldfile.delete();
                    }

                }

                if (status) {

                    List<String[]> data = new ArrayList<String[]>();
                    data.add(new String[]{"\u0001{1:F01BANK DANAMON}{2:" + fileName[0] + "}{4:" + "\r\n"});
                    data.add(new String[]{":20:" + date[0].substring(2, 8) + "\r\n"});
                    data.add(new String[]{":25:" + fileName[1] + "\r\n"});
                    data.add(new String[]{":28C:" + String.format("%05d", Integer.parseInt(String.valueOf(days))) + "\r\n"});

//                    csvWriter.writeNext(new String[]{"\u0001{1:F01BANK DANAMON}{2:" + fileName[0] + "}{4:"+"\r\n"});
//                    csvWriter.writeNext(new String[]{":20:" + date[0].substring(2,8)+"\r\n"});
//                    csvWriter.writeNext(new String[]{":25:" + fileName[1]+"\r\n"});
//                    csvWriter.writeNext(new String[]{":28C:" + String.format("%05d", Integer.parseInt(String.valueOf(days)))+"\r\n"});
                    String[] firstElement = records.get(0);
//            System.out.println(firstElement[1]);
                    String currency = getCurrency(firstElement[7]);

                    if (currency == null) {
                        fileAndFolder.getLogger().log(Level.WARNING, "Currency mot found {0}", name);
                    }

                    decimalFormat = new DecimalFormat("000000000000.00");
                    Double tempAmount = Double.parseDouble(firstElement[12]);
                    String amount = decimalFormat.format(tempAmount).replace(".", ",");
                    if (currency.equals("JPY")) {
//                        if (currency.equals("IDR") || currency.equals("JPY")) {
                        amount = getAmount(firstElement[12]);
                        System.out.println("TES AMOUNT " + firstElement[12]);
                    }
                    data.add(new String[]{":60F:" + firstElement[9] + date[0].substring(2, 8) + currency + amount + "\r\n"});

//                    csvWriter.writeNext(new String[]{":60F:" + firstElement[9] + date[0].substring(2,8) + currency + String.format("%015.2f",
//                            Float.parseFloat(firstElement[12])).replace(".", ",")+"\r\n"});
                    for (String[] record : records) {
                        String[] code = record[4].split(":");
                        int indexCodeName = codeName1.indexOf(code[0]);
                        List<String> dataMnemonic = dataFileMnemonic.get(indexCodeName);
                        String codeName = dataMnemonic.get(1);

                        tempAmount = Double.parseDouble(record[8]);
                        amount = decimalFormat.format(tempAmount).replace(".", ",");
                        if (currency.equals("JPY")) {
//                            if (currency.equals("IDR") || currency.equals("JPY")) {
                            amount = getAmount(record[8]);
                        }

                        String[] dateTrx = record[0].split(" ");

                        data.add(new String[]{":61:" + dateTrx[0].substring(2, 8) + record[9] + amount + codeName + record[11] + "\r\n"});
                        if (record[10].contains("ADKUPK")) {
                            String[] nameSplit = record[10].split("ADKUPK");
                            String sb = nameSplit[1];
//                            System.out.println(sb);
                            data.add(new String[]{":86:" + "ADKUPK" + sb + "\r\n"});
                        } else {
//                            System.out.println(":86:" + record[10] + "\r\n");
                            data.add(new String[]{":86:" + record[10] + "\r\n"});
                        }
//                        csvWriter.writeNext(new String[]{":61:" + dateTrx[0].substring(2,8) + record[9] +
//                                ammount + codeName + record[11]+"\r\n"});
//                        csvWriter.writeNext(new String[]{":86:" + record[10]+"\r\n"});

                    }

                    String[] lastElement = records.get(records.size() - 1);
                    currency = getCurrency(lastElement[7]);

                    tempAmount = Double.parseDouble(lastElement[13]);
                    amount = decimalFormat.format(tempAmount).replace(".", ",");
                    if (currency.equals("JPY")) {
//                        if (currency.equals("IDR") || currency.equals("JPY")) {
                        amount = getAmount(lastElement[13]);
                    }

                    data.add(new String[]{":62F:" + lastElement[9] + date[0].substring(2, 8) + currency + amount + "\r\n"});
                    data.add(new String[]{"-}" + "\u0003\r\n"});
//                    csvWriter.writeNext(new String[]{":62F:" + lastElement[9] + date[0].substring(2,8) + currency + String.format("%015.2f",
//                            Float.parseFloat(lastElement[13])).replace(".", ",")+"\r\n"});
//                    csvWriter.writeNext(new String[]{"-}"+"\u0003\r\n"});

                    for (int i = 0; i < 2; i++) {
                        data.add(new String[]{"\r\n"});
//                        csvWriter.writeNext(new String[]{"\r\n"});
                    }

//            System.out.println("File converted");
                    csvWriter.writeAll(data, true);
                    csvWriter.flush();
                    fileAndFolder.getLogger().log(Level.INFO, "File {0} converted", name);
                    status = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
                fileAndFolder.getLogger().severe(e.toString());
                fileAndFolder.getLogger().log(Level.SEVERE, "Failed converting file {0}", name);
                status = false;
                System.exit(0);
            }

//            if (status == true) {
//                moveFile(name, in, backup, "IN", "BACKUP");
//                moveFileSplit(name, decryptResult, backup, "DECRYPT", "BACKUP");
//            }
        }
    }

    public void convertMtGodrej(String mnemonic, File in, File MT, File backup) throws CsvException, IOException {
        fileAndFolder.createLog();
        String fileMnemonic = "MNEMONIC/MT9xx Mnemonic - Agung.csv";
        System.out.println(fileMnemonic);
        ArrayList<String> codeName1 = new ArrayList<>();
        List<List<String>> dataFileMnemonic = new ArrayList<>();

        try ( BufferedReader reader = new BufferedReader(new InputStreamReader(ConvertMT940.class.getResourceAsStream(fileMnemonic)));  CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build()) {
            fileAndFolder.getLogger().info("Start mapping file mnemonic");
            List<String[]> records = csvReader.readAll();
//                        records.forEach(x -> System.out.println(Arrays.toString(x)));
            for (String[] record : records) {
                codeName1.add(record[0]);
                dataFileMnemonic.add(Arrays.asList(record));
            }
            fileAndFolder.getLogger().info("Mapping file mnemonic done");

        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
            fileAndFolder.getLogger().severe(e.toString());
            JOptionPane.showMessageDialog(null, e);
            fileAndFolder.getLogger().severe("File mnemonic not found");
            System.exit(0);
        }

        File dir = in;
        System.out.println(in);
        if (!dir.exists()) {
            fileAndFolder.getLogger().severe("Folder IN not found");
            System.exit(0);
        }
        String[] fileList = dir.list();
        if (fileList.length == 0) {
            fileAndFolder.getLogger().warning("Nothing file to convert");
        }

        for (String name : fileList) {
            String fileIn = in.toString() + "\\" + name;
            System.out.println(fileIn + "");
            String[] outName = name.split("\\.");
//            String fileOut = prop.getProperty("app.pathMt") + outName[0] + "_Converted.txt";
            String fileOut = MT.toString() + "\\" + outName[0] + "_Converted.txt";
            String fileOutNoRecord = MT.toString() + "\\" + outName[0] + "_NoRecord.txt";

            //READ FILE 
            File file = new File(fileIn);
            String fileInName = file.getName();

            status = false;
            try ( BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileIn), StandardCharsets.UTF_8)); //                    .withCSVParser(new RFC4180ParserBuilder().build()) fix error /"
                      CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1)
                            .withCSVParser(new RFC4180ParserBuilder().build())
                            .build();  OutputStream outputStream = new FileOutputStream(fileOut); //                    Writer writer = new OutputStreamWriter(outputStream);
                    //                    CSVWriter csvWriter = new CSVWriter(writer, '\u0000', '\u0000', '\u0000', "");
                    ) {
                fileAndFolder.getLogger().log(Level.INFO, "Starting convert file {0}", name);

                String[] fileName = fileInName.split("_");
                System.out.println(fileName[1]);
                String[] date = fileName[2].split("\\.");
                String dateBeforeString = "2020-01-01";
                String dateAfterString = dateConverter(date[0]);
                LocalDate dateBefore = LocalDate.parse(dateBeforeString);
                LocalDate dateAfter = LocalDate.parse(dateAfterString);
                long days = Days.daysBetween(dateBefore, dateAfter).getDays() + 1;

                List<String[]> records = csvReader.readAll();

                int count = 0;
                status = true;
                for (String[] record : records) {
                    String[] trxDate = record[0].split(" ");

                    //                Checking date
//                    if (!date[0].equals(trxDate[0]) && status) {
//                        fileAndFolder.getLogger().log(Level.SEVERE, "Transaction date file {0} not same", name);
//                        fileAndFolder.getLogger().log(Level.SEVERE, "Failed converting file {0}", name);
////                        System.exit(0);
//                        status = false;
//                    }
                    count++;

                    if (record.length != 14 && status) {
                        fileAndFolder.getLogger().log(Level.SEVERE, "Error file structure {0}", name);
                        fileAndFolder.getLogger().log(Level.SEVERE, "Failed converting file {0}", name);
//                        System.exit(0);
                        status = false;
                    }
                }

                if (count == 0) {
                    fileAndFolder.getLogger().log(Level.SEVERE, "Error no record {0}", name);
                    status = false;
//                        OutputStream outputStream = new FileOutputStream(fileOutNoRecord);

                    outputStream.close();

                    File oldfile = new File(fileOut);
                    File newfile = new File(fileOutNoRecord);
                    System.out.println(oldfile.getName());
                    oldfile.renameTo(newfile);
                    if (newfile.exists() && newfile.isFile()) {
                        oldfile.delete();
                    }

                }

                if (status) {

                    Writer writer = new OutputStreamWriter(outputStream);
                    CSVWriter csvWriter = new CSVWriter(writer, '\u0000', '\u0000', '\u0000', "");

                    List<String[]> data = new ArrayList<String[]>();
                    data.add(new String[]{"\u0001{1:F01BANK DANAMON}{2:" + fileName[0] + "}{4:" + "\r\n"});
                    data.add(new String[]{":20:" + date[0].substring(2, 8) + "\r\n"});
                    data.add(new String[]{":25:" + fileName[1] + "\r\n"});
                    data.add(new String[]{":28C:" + String.format("%05d", Integer.parseInt(String.valueOf(days))) + "\r\n"});

//                    csvWriter.writeNext(new String[]{"\u0001{1:F01BANK DANAMON}{2:" + fileName[0] + "}{4:"+"\r\n"});
//                    csvWriter.writeNext(new String[]{":20:" + date[0].substring(2,8)+"\r\n"});
//                    csvWriter.writeNext(new String[]{":25:" + fileName[1]+"\r\n"});
//                    csvWriter.writeNext(new String[]{":28C:" + String.format("%05d", Integer.parseInt(String.valueOf(days)))+"\r\n"});
                    String[] firstElement = records.get(0);
//            System.out.println(firstElement[1]);
                    String currency = getCurrency(firstElement[7]);
                    System.out.println(currency);
                    if (currency == null) {
                        fileAndFolder.getLogger().log(Level.WARNING, "Currency mot found {0}", name);
                    }

                    decimalFormat = new DecimalFormat("000000000000.00");
                    Double tempAmount = Double.parseDouble(firstElement[12]);
                    String amount = decimalFormat.format(tempAmount).replace(".", ",");
//                    if (currency.equals("JPY")) {
                    if (currency.equals("IDR") || currency.equals("JPY")) {
                        amount = getAmount(firstElement[12]);
                        System.out.println("TES AMOUNT " + firstElement[12]);
                    }
                    data.add(new String[]{":60F:" + firstElement[9] + date[0].substring(2, 8) + currency + amount + "\r\n"});

//                    csvWriter.writeNext(new String[]{":60F:" + firstElement[9] + date[0].substring(2,8) + currency + String.format("%015.2f",
//                            Float.parseFloat(firstElement[12])).replace(".", ",")+"\r\n"});
                    for (String[] record : records) {
                        String[] code = record[4].split(":");
                        int indexCodeName = codeName1.indexOf(code[0]);
                        List<String> dataMnemonic = dataFileMnemonic.get(indexCodeName);
                        String codeName = dataMnemonic.get(1);

                        tempAmount = Double.parseDouble(record[8]);
                        amount = decimalFormat.format(tempAmount).replace(".", ",");
//                        if (currency.equals("JPY")) {
                        if (currency.equals("IDR") || currency.equals("JPY")) {
                            amount = getAmount(record[8]);
                        }

                        String[] dateTrx = record[0].split(" ");

                        data.add(new String[]{":61:" + dateTrx[0].substring(2, 8) + record[9]
                            + amount + codeName + record[11] + "\r\n"});
                        data.add(new String[]{":86:" + record[10] + "\r\n"});
//                        csvWriter.writeNext(new String[]{":61:" + dateTrx[0].substring(2,8) + record[9] +
//                                ammount + codeName + record[11]+"\r\n"});
//                        csvWriter.writeNext(new String[]{":86:" + record[10]+"\r\n"});
                    }

                    String[] lastElement = records.get(records.size() - 1);
                    currency = getCurrency(lastElement[7]);

                    tempAmount = Double.parseDouble(lastElement[13]);
                    amount = decimalFormat.format(tempAmount).replace(".", ",");
//                    if (currency.equals("JPY")) {
                    if (currency.equals("IDR") || currency.equals("JPY")) {
                        amount = getAmount(lastElement[13]);
                    }

                    data.add(new String[]{":62F:" + lastElement[9] + date[0].substring(2, 8) + currency + amount + "\r\n"});
                    data.add(new String[]{"-}" + "\u0003\r\n"});
//                    csvWriter.writeNext(new String[]{":62F:" + lastElement[9] + date[0].substring(2,8) + currency + String.format("%015.2f",
//                            Float.parseFloat(lastElement[13])).replace(".", ",")+"\r\n"});
//                    csvWriter.writeNext(new String[]{"-}"+"\u0003\r\n"});

                    for (int i = 0; i < 2; i++) {
                        data.add(new String[]{"\r\n"});
//                        csvWriter.writeNext(new String[]{"\r\n"});
                    }

//            System.out.println("File converted");
                    csvWriter.writeAll(data, true);
                    csvWriter.flush();

                    fileAndFolder.getLogger().log(Level.INFO, "File {0} converted", name);
                    status = true;
                    outputStream.close();

                    File oldfile = new File(fileOut);
                    File newfile = new File(fileOutNoRecord);
                    System.out.println(oldfile.getName());
                    oldfile.renameTo(newfile);
                }
            } catch (IOException e) {
                e.printStackTrace();
                fileAndFolder.getLogger().severe(e.toString());
                fileAndFolder.getLogger().log(Level.SEVERE, "Failed converting file {0}", name);
                status = false;
                System.exit(0);
            }

//            if (status == true) {
//                moveFile(name, in, backup, "IN", "BACKUP");
//                moveFileSplit(name, decryptResult, backup, "DECRYPT", "BACKUP");
//            }
        }
    }

    public void convertDecrypt(String mnemonic, File in, File decryptResult, File MT, File backup) throws CsvException, IOException {
        fileAndFolder.createLog();
        String fileMnemonic = "MNEMONIC/MT9xx Mnemonic - Agung.csv";
        System.out.println(fileMnemonic);
        ArrayList<String> codeName1 = new ArrayList<>();
        List<List<String>> dataFileMnemonic = new ArrayList<>();

        try ( BufferedReader reader = new BufferedReader(new InputStreamReader(ConvertMT940.class.getResourceAsStream(fileMnemonic)));  CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build()) {
            fileAndFolder.getLogger().info("Start mapping file mnemonic");
            List<String[]> records = csvReader.readAll();
            for (String[] record : records) {
                codeName1.add(record[0]);
                dataFileMnemonic.add(Arrays.asList(record));
            }
            fileAndFolder.getLogger().info("Mapping file mnemonic done");
        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
            fileAndFolder.getLogger().severe(e.toString());
            JOptionPane.showMessageDialog(null, e);
            fileAndFolder.getLogger().severe("File mnemonic not found");
            System.exit(0);
        }

        File dir = decryptResult;
        if (!dir.exists()) {
            fileAndFolder.getLogger().severe("Folder DECRYPT not found");
            System.exit(0);
        }
        String[] fileList = dir.list();
        if (fileList.length == 0) {
            fileAndFolder.getLogger().warning("Nothing file to convert");
        }

        for (String name : fileList) {
            String fileIn = decryptResult.toString() + "\\" + name;
            System.out.println(fileIn + " ininini");
            String[] outName = name.split("\\.");
            //            String fileOut = prop.getProperty("app.pathMt") + outName[0] + "_Converted.txt";
            String fileOut = MT.toString() + "\\" + outName[0] + "_Converted.txt";
            String fileOutNoRecord = MT.toString() + "\\" + outName[0] + "_NoRecord.txt";

            File file = new File(fileIn);
            String fileInName = file.getName();

            status = false;
            try ( BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileIn), StandardCharsets.UTF_8));  CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();  OutputStream outputStream = new FileOutputStream(fileOut);  Writer writer = new OutputStreamWriter(outputStream);  CSVWriter csvWriter = new CSVWriter(writer, '\u0000', '\u0000', '\u0000', "");) {
                fileAndFolder.getLogger().log(Level.INFO, "Starting convert file {0}", name);

                String[] fileName = fileInName.split("_");
                String[] date = fileName[2].split("\\.");
                String dateBeforeString = "2020-01-01";
                String dateAfterString = dateConverter(date[0]);
                LocalDate dateBefore = LocalDate.parse(dateBeforeString);
                LocalDate dateAfter = LocalDate.parse(dateAfterString);
                long days = Days.daysBetween(dateBefore, dateAfter).getDays() + 1;

                List<String[]> records = csvReader.readAll();

                int count = 0;
                status = true;
                for (String[] record : records) {
                    count++;
                    //                Checking date
                    String[] trxDate = record[0].split(" ");
                    if (!date[0].equals(trxDate[0]) && status) {
                        fileAndFolder.getLogger().log(Level.SEVERE, "Transaction date file {0} not same", name);
                        fileAndFolder.getLogger().log(Level.SEVERE, "Failed converting file {0}", name);
                        //                        System.exit(0);
                        status = false;
                    }

                    if (record.length != 14 && status) {
                        fileAndFolder.getLogger().log(Level.SEVERE, "Error file structure {0}", name);
                        fileAndFolder.getLogger().log(Level.SEVERE, "Failed converting file {0}", name);
                        //                        System.exit(0);
                        status = false;
                    }
                }
                if (count == 0) {
                    fileAndFolder.getLogger().log(Level.WARNING, "No Record {0}", name);
                    status = false;
                    outputStream.close();
                    File oldfile = new File(fileOut);
                    File newfile = new File(fileOutNoRecord);
                    System.out.println(oldfile.getName());
                    oldfile.renameTo(newfile);

                    if (newfile.exists() && newfile.isFile()) {
                        oldfile.delete();

                    }

                }
                if (status) {

                    List<String[]> data = new ArrayList<String[]>();
                    data.add(new String[]{"\u0001{1:F01BANK DANAMON}{2:" + fileName[0] + "}{4:" + "\r\n"});
                    data.add(new String[]{":20:" + date[0].substring(2, 8) + "\r\n"});
                    data.add(new String[]{":25:" + fileName[1] + "\r\n"});
                    data.add(new String[]{":28C:" + String.format("%05d", Integer.parseInt(String.valueOf(days))) + "\r\n"});

                    //                    csvWriter.writeNext(new String[]{"\u0001{1:F01BANK DANAMON}{2:" + fileName[0] + "}{4:"+"\r\n"});
                    //                    csvWriter.writeNext(new String[]{":20:" + date[0].substring(2,8)+"\r\n"});
                    //                    csvWriter.writeNext(new String[]{":25:" + fileName[1]+"\r\n"});
                    //                    csvWriter.writeNext(new String[]{":28C:" + String.format("%05d", Integer.parseInt(String.valueOf(days)))+"\r\n"});
                    String[] firstElement = records.get(0);
                    //            System.out.println(firstElement[1]);
                    String currency = getCurrency(firstElement[7]);
                    System.out.println(currency);
                    if (currency == null) {
                        fileAndFolder.getLogger().log(Level.WARNING, "Currency mot found {0}", name);
                    }

                    decimalFormat = new DecimalFormat("000000000000.00");
                    Double tempAmount = Double.parseDouble(firstElement[12]);
                    String amount = decimalFormat.format(tempAmount).replace(".", ",");
                    if (currency.equals("IDR") || currency.equals("JPY")) {
                        amount = getAmount(firstElement[12]);
                        System.out.println("TES AMOUNT " + amount);
                    }
                    System.out.println("TES AMOUNT " + amount);
                    data.add(new String[]{":60F:" + firstElement[9] + date[0].substring(2, 8) + currency + amount + "\r\n"});

                    //                    csvWriter.writeNext(new String[]{":60F:" + firstElement[9] + date[0].substring(2,8) + currency + String.format("%015.2f",
                    //                            Float.parseFloat(firstElement[12])).replace(".", ",")+"\r\n"});
                    for (String[] record : records) {
                        String[] code = record[4].split(":");
                        int indexCodeName = codeName1.indexOf(code[0]);
                        List<String> dataMnemonic = dataFileMnemonic.get(indexCodeName);
                        String codeName = dataMnemonic.get(1);

                        tempAmount = Double.parseDouble(record[8]);
                        amount = decimalFormat.format(tempAmount).replace(".", ",");
                        if (currency.equals("IDR") || currency.equals("JPY")) {
                            amount = getAmount(record[8]);
                        }

                        String[] dateTrx = record[0].split(" ");

                        data.add(new String[]{":61:" + dateTrx[0].substring(2, 8) + record[9]
                            + amount + codeName + record[11] + "\r\n"});
                        data.add(new String[]{":86:" + record[10] + "\r\n"});
                        //                        csvWriter.writeNext(new String[]{":61:" + dateTrx[0].substring(2,8) + record[9] +
                        //                                ammount + codeName + record[11]+"\r\n"});
                        //                        csvWriter.writeNext(new String[]{":86:" + record[10]+"\r\n"});
                    }

                    String[] lastElement = records.get(records.size() - 1);
                    currency = getCurrency(lastElement[7]);

                    tempAmount = Double.parseDouble(lastElement[13]);
                    amount = decimalFormat.format(tempAmount).replace(".", ",");
                    if (currency.equals("IDR") || currency.equals("JPY")) {
                        amount = getAmount(lastElement[13]);
                    }

                    data.add(new String[]{":62F:" + lastElement[9] + date[0].substring(2, 8) + currency + amount + "\r\n"});
                    data.add(new String[]{"-}" + "\u0003\r\n"});
                    //                    csvWriter.writeNext(new String[]{":62F:" + lastElement[9] + date[0].substring(2,8) + currency + String.format("%015.2f",
                    //                            Float.parseFloat(lastElement[13])).replace(".", ",")+"\r\n"});
                    //                    csvWriter.writeNext(new String[]{"-}"+"\u0003\r\n"});

                    for (int i = 0; i < 2; i++) {
                        data.add(new String[]{"\r\n"});
                        //                        csvWriter.writeNext(new String[]{"\r\n"});
                    }

                    //            System.out.println("File converted");
                    csvWriter.writeAll(data, true);
                    csvWriter.flush();
                    fileAndFolder.getLogger().log(Level.INFO, "File {0} converted", name);
                    status = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
                fileAndFolder.getLogger().severe(e.toString());
                fileAndFolder.getLogger().log(Level.SEVERE, "Failed converting file {0}", name);
                status = false;
                System.exit(0);
            }

            //            if (status == true) {
            //                moveFile(name, in, backup, "IN", "BACKUP");
            //                moveFileSplit(name, decryptResult, backup, "DECRYPT", "BACKUP");
            //            }
        }
    }

}
