package main.h2h.mt;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;

public abstract class BCPGPUtils {

    private static Logger logger;

    public static PGPPublicKey readPublicKey(String publicKeyFilePath) throws IOException, PGPException {

        InputStream in = new FileInputStream(new File(publicKeyFilePath));

        in = PGPUtil.getDecoderStream(in);
        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(in);
        PGPPublicKey key = null;

        Iterator rIt = pgpPub.getKeyRings();
        while (key == null && rIt.hasNext()) {
            PGPPublicKeyRing kRing = (PGPPublicKeyRing) rIt.next();
            Iterator kIt = kRing.getPublicKeys();
            boolean encryptionKeyFound = false;

            while (key == null && kIt.hasNext()) {
                PGPPublicKey k = (PGPPublicKey) kIt.next();
                if (k.isEncryptionKey()) {
                    key = k;
                }
            }
        }

        if (key == null) {
            throw new IllegalArgumentException(
                    "Can't find encryption key in key ring.");
        }

        return key;
    }

    public static PGPPublicKey readPublicKey(String publicKeyFilePath, long keyId) throws IOException, PGPException {

        InputStream in = new FileInputStream(new File(publicKeyFilePath));

        in = PGPUtil.getDecoderStream(in);
        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(in);
        PGPPublicKey key = null;

        Iterator rIt = pgpPub.getKeyRings();
        while (rIt.hasNext()) {
            PGPPublicKeyRing kRing = (PGPPublicKeyRing) rIt.next();
            Iterator kIt = kRing.getPublicKeys();
            boolean encryptionKeyFound = false;

            while (kIt.hasNext()) {
                PGPPublicKey k = (PGPPublicKey) kIt.next();
                long keyid = k.getKeyID();
                if (keyid == keyId) {
                    key = k;
                }
                //if (k.isEncryptionKey()) {
                //	key = k;
                //}
            }
        }

        if (key == null) {
            throw new IllegalArgumentException(
                    "Can't find encryption key in key ring.");
        }

        return key;
    }

    public static PGPPrivateKey findPrivateKey(InputStream keyIn, long keyID,
            char[] pass) throws IOException, PGPException,
            NoSuchProviderException {
        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(
                PGPUtil.getDecoderStream(keyIn));

        PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);

        if (pgpSecKey == null) {
            return null;
        }
        return pgpSecKey.extractPrivateKey(pass, "BC");
    }

    public static PGPSecretKey findSecretKey(InputStream in) throws IOException, PGPException {
        in = PGPUtil.getDecoderStream(in);
        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(in);

        //
        // we just loop through the collection till we find a key suitable for encryption, in the real
        // world you would probably want to be a bit smarter about this.
        //
        PGPSecretKey key = null;

        //
        // iterate through the key rings.
        //
        Iterator rIt = pgpSec.getKeyRings();

        while (key == null && rIt.hasNext()) {
            PGPSecretKeyRing kRing = (PGPSecretKeyRing) rIt.next();
            Iterator kIt = kRing.getSecretKeys();

            while (key == null && kIt.hasNext()) {
                PGPSecretKey k = (PGPSecretKey) kIt.next();

                if (k.isSigningKey()) {
                    key = k;
                }
            }
        }

        if (key == null) {
            throw new IllegalArgumentException(
                    "Can't find signing key in key ring.");
        }
        return key;
    }

    public static void createLog() throws IOException {
        File folderLog = new File("Log");
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

    public static void checkAppConfigDecrypt() {
        Properties prop = new Properties();
        String configFile = "app.config";
        try {
            InputStream inputStream = new FileInputStream(configFile);
            prop.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File folderBackup = new File(prop.getProperty("app.pathBackup"));
        File folderDecrypt = new File(prop.getProperty("app.pathDecrypt"));
        File folderDecryptResult = new File(prop.getProperty("app.pathDecryptResult"));
        File folderKeyDecrypt = new File(prop.getProperty("app.pathKeyDecrypt"));
        File folderMnemonic = new File(prop.getProperty("app.pathMnemonic"));
        File folderMt = new File(prop.getProperty("app.pathMt"));

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
        String filePrivateKeyDecrypt = prop.getProperty("app.privateKeyDecrypt");
        String filePublicKeyDecrypt = prop.getProperty("app.publicKeyDecrypt");
        String filePassphraseDecrypt = prop.getProperty("app.passphraseDecrypt");

        if (!checkConfigDecrypt(
                filePrivateKeyDecrypt,
                filePublicKeyDecrypt,
                filePassphraseDecrypt
        )) {
            return;
        }
    }

    private static boolean checkFolderDecrypt(
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

        return status;
    }

    private static boolean checkConfigDecrypt(
            String filePrivateKeyDecrypt,
            String filePublicKeyDecrypt,
            String filePassphraseDecrypt
    ) {
        logger.info("Reading configuration file.");
        boolean status = true;

        if (filePrivateKeyDecrypt == null) {
            status = false;
            logger.warning("Private key not found");
        }

        if (filePublicKeyDecrypt == null) {
            status = false;
            logger.warning("Public key not found");
        }

        if (filePassphraseDecrypt == null) {
            status = false;
            logger.warning("Passphrase not found");
        }

        return status;
    }

    public static void checkAppConfigEncrypt() {
        Properties prop = new Properties();
        String configFile = "app.config";
        try {
            InputStream inputStream = new FileInputStream(configFile);
            prop.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File folderEncrypt = new File(prop.getProperty("app.pathEncrypt"));
        File folderEncryptResult = new File(prop.getProperty("app.pathEncryptResult"));
        File folderKeyEncrypt = new File(prop.getProperty("app.pathKeyEncrypt"));

        if (!checkFolderEncrypt(
                folderEncrypt,
                folderEncryptResult,
                folderKeyEncrypt
        )) {
            System.exit(0);
        }
        String filePrivateKeyEncrypt = prop.getProperty("app.privateKeyEncrypt");
        String filePublicKeyEncrypt = prop.getProperty("app.publicKeyEncrypt");
        String filePassphraseEncrypt = prop.getProperty("app.passphraseEncrypt");

        if (!checkConfigEncrypt(
                filePrivateKeyEncrypt,
                filePublicKeyEncrypt,
                filePassphraseEncrypt
        )) {
            return;
        }
    }

    private static boolean checkFolderEncrypt(
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

    private static boolean checkConfigEncrypt(
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

    public static void checkAppConfigMt() {
        Properties prop = new Properties();
        String configFile = "app.config";
        try {
            InputStream inputStream = new FileInputStream(configFile);
            prop.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File folderBackup = new File(prop.getProperty("app.pathBackup"));
        File folderDecrypt = new File(prop.getProperty("app.pathDecrypt"));
        File folderMnemonic = new File(prop.getProperty("app.pathMnemonic"));
        File folderMt = new File(prop.getProperty("app.pathMt"));

        if (!checkFolderMt(
                folderBackup,
                folderDecrypt,
                folderMnemonic,
                folderMt
        )) {
            System.exit(0);
        }
    }

    private static boolean checkFolderMt(
            File folderBackup,
            File folderDecrypt,
            File folderMnemonic,
            File folderMt
    ) {
        boolean status = true;

        if (!folderBackup.exists()) {
            logger.severe("Folder BACKUP not found.");
            status = false;
        }
        if (!folderDecrypt.exists()) {
            logger.severe("Folder DECRYPT not found.");
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

        return status;
    }

}
