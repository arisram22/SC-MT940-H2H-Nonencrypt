package main.h2h.mt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class BCPGPFileAndFolder {

    private Logger logger;
    private File folderLog;
    private String filePrivateKeyEncrypt, filePublicKeyEncrypt, filePrivateKeyDecrypt,
            filePublicKeyDecrypt, filePassphraseDecrypt, filePassphraseEncrypt, fileMnemonic;
    private File folderBackup, folderDecrypt, folderDecryptResult,
            folderEncrypt, folderEncryptResult, folderKeyDecrypt,
            folderKeyEncrypt, folderMnemonic, folderMt, folderIn;

    public Logger getLogger() {
        return logger;
    }

    public File getFolderBackup() {
        return folderBackup;
    }

    public File getFolderDecrypt() {
        return folderDecrypt;
    }

    public File getFolderDecryptResult() {
        return folderDecryptResult;
    }

    public File getFolderEncrypt() {
        return folderEncrypt;
    }

    public File getFolderEncryptResult() {
        return folderEncryptResult;
    }

    public File getFolderKeyDecrypt() {
        return folderKeyDecrypt;
    }

    public File getFolderKeyEncrypt() {
        return folderKeyEncrypt;
    }

    public File getFolderMnemonic() {
        return folderMnemonic;
    }

    public File getFolderMt() {
        return folderMt;
    }

    public String getFilePrivateKeyEncrypt() {
        return filePrivateKeyEncrypt;
    }

    public String getFilePublicKeyEncrypt() {
        return filePublicKeyEncrypt;
    }

    public String getFilePrivateKeyDecrypt() {
        return filePrivateKeyDecrypt;
    }

    public String getFilePublicKeyDecrypt() {
        return filePublicKeyDecrypt;
    }

    public String getFilePassphraseDecrypt() {
        return filePassphraseDecrypt;
    }

    public String getFilePassphraseEncrypt() {
        return filePassphraseEncrypt;
    }

    public String getFileMnemonic() {
        return fileMnemonic;
    }

    public File getFolderLog() {
        return folderLog;
    }

    public void setFolderLog(File folderLog) {
        this.folderLog = folderLog;
    }

    public File getFolderIn() {
        return folderIn;
    }

    public void setFolderIn(File folderIn) {
        this.folderIn = folderIn;
    }

    public void createLog() throws IOException {
        folderLog = new File("Log");
        if (!folderLog.exists()) {
            folderLog.mkdir();
        }

        FileHandler fileHandler = new FileHandler("Log/Converter.log", true);
        logger = Logger.getLogger("Log");
        logger.addHandler(fileHandler);
        logger.setLevel(Level.ALL);
        SimpleFormatter simpleformatter = new SimpleFormatter();
        fileHandler.setFormatter(simpleformatter);

    }

    public void checkAppConfigDecrypt() {
        Properties prop = new Properties();
        String configFile = "app.config";
        try {
            InputStream inputStream = new FileInputStream(configFile);
            prop.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        folderBackup = new File(prop.getProperty("app.pathBackup"));
        folderDecrypt = new File(prop.getProperty("app.pathDecrypt"));
        folderDecryptResult = new File(prop.getProperty("app.pathDecryptResult"));
        folderKeyDecrypt = new File(prop.getProperty("app.pathKeyDecrypt"));
        folderMnemonic = new File(prop.getProperty("app.pathMnemonic"));
        folderMt = new File(prop.getProperty("app.pathMt"));

        if (!checkFolderDecrypt(
                folderBackup,
                folderDecrypt,
                folderDecryptResult,
                folderKeyDecrypt,
                folderMnemonic,
                folderMt
        )) {
            System.exit(0);
        }

        filePrivateKeyEncrypt = prop.getProperty("app.privateKeyEncrypt");
        filePublicKeyEncrypt = prop.getProperty("app.publicKeyEncrypt");
        filePassphraseEncrypt = prop.getProperty("app.passphraseEncrypt");
        filePrivateKeyDecrypt = prop.getProperty("app.privateKeyDecrypt");
        filePublicKeyDecrypt = prop.getProperty("app.publicKeyDecrypt");
        filePassphraseDecrypt = prop.getProperty("app.passphraseDecrypt");
        fileMnemonic = prop.getProperty("app.mnemonic");

        if (!checkConfigDecrypt(
                filePrivateKeyDecrypt,
                filePublicKeyDecrypt,
                filePassphraseDecrypt
        )) {
            return;
        }
    }

    public void checkAppConfigEncrypt() {
        Properties prop = new Properties();
        String configFile = "app.config";
        try {
            InputStream inputStream = new FileInputStream(configFile);
            prop.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        folderEncrypt = new File(prop.getProperty("app.pathEncrypt"));
        folderEncryptResult = new File(prop.getProperty("app.pathEncryptResult"));
        folderKeyEncrypt = new File(prop.getProperty("app.pathKeyEncrypt"));

        if (!checkFolderEncrypt(
                folderEncrypt,
                folderEncryptResult,
                folderKeyEncrypt
        )) {
            System.exit(0);
        }

        filePrivateKeyEncrypt = prop.getProperty("app.privateKeyEncrypt");
        filePublicKeyEncrypt = prop.getProperty("app.publicKeyEncrypt");
        filePassphraseEncrypt = prop.getProperty("app.passphraseEncrypt");
        filePrivateKeyDecrypt = prop.getProperty("app.privateKeyDecrypt");
        filePublicKeyDecrypt = prop.getProperty("app.publicKeyDecrypt");
        filePassphraseDecrypt = prop.getProperty("app.passphraseDecrypt");
        fileMnemonic = prop.getProperty("app.mnemonic");

        if (!checkConfigEncrypt(
                filePrivateKeyEncrypt,
                filePublicKeyEncrypt,
                filePassphraseEncrypt
        )) {
            return;
        }
    }

    public void checkAppConfigMt() {
        Properties prop = new Properties();
        String configFile = "app.config";
        try {
            InputStream inputStream = new FileInputStream(configFile);
            prop.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        folderBackup = new File(prop.getProperty("app.pathBackup"));
//        folderMnemonic = new File(prop.getProperty("app.pathMnemonic"));
        folderMt = new File(prop.getProperty("app.pathMt"));
        folderIn = new File(prop.getProperty("app.pathIn"));

        if (!checkFolderMt(
                folderBackup,
//                folderMnemonic,
                folderMt,
                folderIn
        )) {
            System.exit(0);
        }

        fileMnemonic = prop.getProperty("app.mnemonic");

        if (!checkConfigMt()) {
            return;
        }
    }

    public boolean checkFolderDecrypt(
            File folderBackup,
            File folderDecrypt,
            File folderDecryptResult,
            File folderKeyDecrypt,
            File folderMnemonic,
            File folderMt
    ) {
        boolean status = true;

        if (!folderBackup.exists()) {
            logger.severe("Folder BACKUP not found.");
            status = false;
        }
        if (!folderDecrypt.exists()) {
            logger.severe("Folder IN not found.");
            status = false;
        }
        if (!folderDecryptResult.exists()) {
            logger.severe("Folder DECRYPT not found.");
            status = false;
        }
        if (!folderKeyDecrypt.exists()) {
            logger.severe("Folder KEY not found.");
            status = false;
        }
        if (!folderMnemonic.exists()) {
            logger.severe("Folder MNEMONIC not found.");
            status = false;
        }
        if (!folderMt.exists()) {
            logger.severe("Folder MT not found.");
            status = false;
        }
        System.out.println("Start");
        return status;
    }

    public boolean checkFolderEncrypt(
            File folderEncrypt,
            File folderEncryptResult,
            File folderKeyEncrypt
    ) {
        boolean status = true;

        if (!folderEncrypt.exists()) {
            logger.severe("Folder ENCRYPT not found.");
            status = false;
        }
        if (!folderEncryptResult.exists()) {
            logger.severe("Folder ENCRYPT RESULT not found.");
            status = false;
        }

        if (!folderKeyEncrypt.exists()) {
            logger.severe("Folder KEY ENCRYPT not found.");
            status = false;
        }

        return status;
    }

    public boolean checkFolderMt(
            File folderBackup,
//            File folderMnemonic,
            File folderMt,
            File folderIn
    ) {
        boolean status = true;

        if (!folderBackup.exists()) {
            logger.severe("Folder BACKUP not found.");
            status = false;
        }
//        if (!folderMnemonic.exists()) {
//            logger.severe("Folder MNEMONIC not found.");
//            status = false;
//        }
        if (!folderMt.exists()) {
            logger.severe("Folder MT not found.");
            status = false;
        }
        if (!folderIn.exists()) {
            logger.severe("Folder IN not found.");
            status = false;
        }
        System.out.println("Start");
        return status;
    }

    public boolean checkConfigDecrypt(
            String filePrivateKeyDecrypt,
            String filePublicKeyDecrypt,
            String filePassphraseDecrypt
    ) {
        logger.info("Reading configuration file.");
        boolean status = true;

        if (filePrivateKeyDecrypt == null) {
            status = false;
            logger.warning("Private key for Decrypt not found");
        }

        if (filePublicKeyDecrypt == null) {
            status = false;
            logger.warning("Public key for Decrypt not found");
        }

        if (filePassphraseDecrypt == null) {
            status = false;
            logger.warning("Passphrase for Decrypt not found");
        }

        if (fileMnemonic == null) {
            status = false;
            logger.warning("Mnemonic not found");
        }
        return status;
    }

    public boolean checkConfigEncrypt(
            String filePrivateKeyEncrypt,
            String filePublicKeyEncrypt,
            String filePassphraseEncrypt
    ) {
        logger.info("Reading configuration file.");
        boolean status = true;

        if (filePrivateKeyEncrypt == null) {
            status = false;
            logger.warning("Private key for Encrypt not found");
        }

        if (filePublicKeyEncrypt == null) {
            status = false;
            logger.warning("Public key for Encrypt not found");
        }

        if (filePassphraseEncrypt == null) {
            status = false;
            logger.warning("Passphrase for Encrypt not found");
        }

        return status;
    }

    public boolean checkConfigMt() {
        logger.info("Reading configuration file.");
        boolean status = true;
        return status;
    }
}
