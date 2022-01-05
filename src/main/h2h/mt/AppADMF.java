/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.h2h.mt;

import java.io.File;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/** 
 *
 * @author Aris Ramadhan
 */
public class AppADMF {

 
    private static File fileName;
    private static BCPGPFileAndFolder fileAndFolder = new BCPGPFileAndFolder();

    public static void main(String[] args) throws Exception {

        Security.addProvider(new BouncyCastleProvider());
        checkFileAndFolder();
        convertMT940Admf();
    }

    public static void checkFileAndFolder() throws Exception {
        fileAndFolder.createLog();
        fileAndFolder.checkAppConfigMt();
    }

    public static void convertMT940Admf() throws Exception {
        ConvertMT940 convert = new ConvertMT940();
        convert.convertMtAdmf(
                fileAndFolder.getFileMnemonic(),
                fileAndFolder.getFolderIn(),
                fileAndFolder.getFolderMt(),
                fileAndFolder.getFolderBackup());
//        convert.moveFile(fileName.toString(), fileAndFolder.getFolderIn(), fileAndFolder.getFolderBackup(), "DECRYPT", "BACKUP");
//        convert.moveFileSplit(fileName.toString(), fileAndFolder.getFolderDecryptResult(), fileAndFolder.getFolderBackup(), "DECRYPT RESULT", "BACKUP");
    }
}
