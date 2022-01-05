package main.h2h.mt;



import java.io.File;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class AppMT{

    private static File fileName;
    private static BCPGPFileAndFolder fileAndFolder = new BCPGPFileAndFolder();

    public static void main(String[] args) throws Exception {

        Security.addProvider(new BouncyCastleProvider());
        checkFileAndFolder();
        convertMT940();
    }

    public static void checkFileAndFolder() throws Exception {
        fileAndFolder.createLog();
        fileAndFolder.checkAppConfigMt();
    }

    public static void convertMT940() throws Exception {
        ConvertMT940 convert = new ConvertMT940();
        convert.convertMt(
                fileAndFolder.getFileMnemonic(),
                fileAndFolder.getFolderIn(),
                fileAndFolder.getFolderMt(),
                fileAndFolder.getFolderBackup());
    }
}

