package dataProcess;

import verifyCbr.CbrUser;
import verifyCf.CfUser;

public class CreateUserTrackTablesAndFiles {

    public static void main(String[] args) {
        BuildTrainingCfFiles cf = new BuildTrainingCfFiles();
        BuildTrainingCbrFiles cbr = new BuildTrainingCbrFiles();
        //VerifyCf verify = new VerifyCf();
        CfUser cfUser = new CfUser();
        CbrUser cbrUser = new CbrUser();
        
        cf.createBaseTable();
        for (int i = 1; i <= 3; i++) {
            cf.runOperations(i);
            cbr.runOperations(i);
            cfUser.runOperations(i);
            cbrUser.runOperations(i);
            //verify.runOperations(i);
        }
    }
}