package cbr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.List;

import util.Constants;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import connection.DBOperation;

public class BuildTrainingOwlFile {
    
    private String filename;
    private String trainingTableName;
    private String dataFilePath;
    
    public BuildTrainingOwlFile(String filename, String trainingTableName) {
        this.filename = filename;
        this.trainingTableName = trainingTableName;
        this.dataFilePath = Constants.dataLocations + this.filename;
    }
    
    //move file io class
    public void copyBaseFileToNewLocation(String baseFile, String targetFile) {
        File source = new File(baseFile);
        File destination = new File(targetFile);
        try {
            Files.copy(source.toPath(), destination.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //move file io class
    public void deleteFile(String path) {
        File file = new File(path);
        try {
            Files.delete(file.toPath());
        } catch (NoSuchFileException x) {
            System.err.format("%s: no such" + " file or directory%n", path);
        } catch (DirectoryNotEmptyException x) {
            System.err.format("%s not empty%n", path);
        } catch (IOException x) {
            System.err.println(x);
        }
    }
    
    //move cbr class
    public OntModel loadCbrModel(String filePath) {
        OntModel model = null;
        try {
            model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            InputStream in = new FileInputStream(filePath);
            model.read(in, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }
    
    //TODO move cbr class
    public void saveIntances(OntModel model, String filepath) {
        FileWriter out;
        try {
            out = new FileWriter(filepath);
            model.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //TODO move cbr class
    public OntModel removeIndividiuals(OntModel model) {
        DBOperation db = new DBOperation();
        List<Integer> uniqueTrackIds = db.getDistinctTrackIdsOfTable(this.trainingTableName);
        
        for (int trackId : uniqueTrackIds) {
            Individual ind = model.getIndividual(Constants.namespace + "I" + trackId);
            if (ind != null) {
                System.out.println("removed track from model with id : " + trackId);
                ind.remove();
            }
        }
        return model;
    }
    
    public void runOperations() {
        deleteFile(Constants.dataLocations + this.filename);
        System.out.println("removed old file if it is available");
        
        copyBaseFileToNewLocation(Constants.baseOwlFileLocation, this.dataFilePath);
        System.out.println("base owl file copied");
        
        OntModel model = loadCbrModel(dataFilePath);
        System.out.println("base owl file loaded");
        
        model = removeIndividiuals(model);
        
        System.out.println("saving model..");
        saveIntances(model, dataFilePath);
    }
}
