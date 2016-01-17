package dataProcess;

import verifyCf.VerifyCf;

public class CreateUserTrackTablesAndFiles {

    public static void main(String[] args) {
        BuildTrainingCfFiles cf = new BuildTrainingCfFiles();
        // BuildTrainingCbrFiles cbr = new BuildTrainingCbrFiles();
        
        VerifyCf verifyCf = new VerifyCf();
        // CfUser cfUser = new CfUser();
        // CbrUser cbrUser = new CbrUser();

        //cf.createBaseTable();
        for (int i = 2; i <= 2; i++) {
            //cf.runOperations(i);
        }
        for (int i = 2; i <= 2; i++) {
            //cf.createCsvDatas(i);
            verifyCf.runOperations(i);
            // cbr.runOperations(i);
            // cfUser.runOperations(i);
            // cbrUser.runOperations(i);
        }
    }
}