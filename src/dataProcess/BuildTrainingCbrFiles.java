package dataProcess;

import java.util.List;

import model.CbrUtil;
import util.Constants;
import util.Util;

import com.hp.hpl.jena.ontology.OntModel;

import connection.DBOperation;

public class BuildTrainingCbrFiles {
    
    private Util util;
    private CbrUtil cbrUtil;
    private DBOperation dbUtil;
    
    public BuildTrainingCbrFiles() {
        util = new Util();
        cbrUtil = new CbrUtil();
        dbUtil = new DBOperation();
    }
    
    public void runOperations(int index) {
        String filename = Constants.trainingTable + index + ".owl";
        String dataFilePath = Constants.dataLocations + filename;
        String trainingTableName = Constants.trainingUserTrackTable + index;
        
        util.deleteFile(dataFilePath);
        System.out.println("removed old file if it is available");
        
        util.copyFileToNewLocation(Constants.baseOwlFileLocation, dataFilePath);
        System.out.println("base owl file copied");
        
        OntModel model = cbrUtil.loadCbrModel(dataFilePath);
        System.out.println("base owl file loaded");
        
        List<Integer> uniqueTrackIds = dbUtil.getDistinctColumnValuesOfTable(trainingTableName, "track_id");
        model = cbrUtil.removeIndividiuals(model, uniqueTrackIds);
        
        System.out.println("saving model..");
        cbrUtil.saveIntances(model, dataFilePath);
    }
}
