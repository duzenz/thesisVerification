package model;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.Constants;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import connection.DBOperation;

public class CbrUtil {

    public DBOperation dbUtil;
    public ObjectProperty age;
    public ObjectProperty register;
    public ObjectProperty gender;
    public ObjectProperty country;

    public CbrUtil() {
        dbUtil = new DBOperation();
    }

    public OntModel loadCbrModel(String filePath) {
        OntModel model = null;
        try {
            model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            InputStream in = new FileInputStream(filePath);
            model.read(in, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        age = model.getObjectProperty(Constants.namespace + "HAS-AGE");
        country = model.getObjectProperty(Constants.namespace + "HAS-COUNTRY");
        gender = model.getObjectProperty(Constants.namespace + "HAS-GENDER");
        register = model.getObjectProperty(Constants.namespace + "HAS-REGISTER");
        return model;
    }

    public void saveIntances(OntModel model, String filepath) {
        FileWriter out;
        try {
            out = new FileWriter(filepath);
            model.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public OntModel removeIndividiuals(OntModel model, List<Integer> ids) {
        OntClass individualClass = model.getOntClass(Constants.namespace + "RECOMMEND_CASE");
        ExtendedIterator instances = individualClass.listInstances();

        List<Integer> allIds = new ArrayList<Integer>();
        while (instances.hasNext()) {
            Individual instance = (Individual) instances.next();
            String instanceId = instance.getLocalName();
            allIds.add(Integer.parseInt(instanceId.substring(1, instanceId.length())));
        }

        System.out.println(allIds.size());
        System.out.println(ids.size());

        for (int indId : allIds) {
            if (!ids.contains(indId)) {
                Individual ind = model.getIndividual(Constants.namespace + "I" + indId);
                if (ind != null) {
                    ind.remove();
                }
            }
        }
        return model;
    }

    public CustomIndividual createIndividualForUser(OntModel model, User user, List<UserTrack> userTracks) {

        CustomIndividual ci = new CustomIndividual();
        ci.setAge(user.getAgeCol());
        ci.setCountry(user.getCountry());
        ci.setGender(user.getGender());
        ci.setRegister(user.getRegisterCol());

        Set<String> tagSet = new HashSet<String>();

        for (UserTrack userTrack : userTracks) {

            Track track = dbUtil.getTrackInfo("track", userTrack.getTrackId());

            String[] tagArray = track.getTags().split(",");
            for (int i = 0; i < tagArray.length; i++) {
                String tag = tagArray[i].trim();
                if (tag != null && tag.length() != 0) {
                    tagSet.add(tag.replace("\"", "").replaceAll("\\s+", "_").replaceAll("#", ""));
                }
            }
        }
        ci.setTags(tagSet);
        return ci;
    }

    public Map<String, Integer> compareInstanceWithModelInstances(OntModel model, List<Individual> modelInstances, CustomIndividual newIndividual) {
        Map<String, Integer> distanceMap = new HashMap<String, Integer>();
        for (Individual selectedIndividual : modelInstances) {
            int value = compareProperties(selectedIndividual, newIndividual);
            distanceMap.put(selectedIndividual.getLocalName(), value);
        }
        return distanceMap;
    }

    public Set<Property> setComparedPropertyList(CustomIndividual individual, OntModel model) {
        Set<Property> propertySet = new HashSet<Property>();
        for (String tag : individual.getTags()) {
            Property p = model.getProperty(tag);
            if (p != null) {
                propertySet.add(p);
            }
        }
        return propertySet;
    }

    public int compareProperties(Individual selectedIndividual, CustomIndividual newIndividual) {
        int value = 0;

        StmtIterator comparedProps = selectedIndividual.listProperties(age);
        while (comparedProps.hasNext()) {
            Statement comparedStatement = (Statement) comparedProps.next();
            Object comparedObj = comparedStatement.getLiteral().getValue();
            if (newIndividual.getAge().length() > 0 && newIndividual.getAge().equals(comparedObj.toString())) {
                value += 2;
            }
        }
        
        comparedProps = selectedIndividual.listProperties(gender);
        while (comparedProps.hasNext()) {
            Statement comparedStatement = (Statement) comparedProps.next();
            Object comparedObj = comparedStatement.getLiteral().getValue();
            if (newIndividual.getGender().length() > 0 && newIndividual.getGender().equals(comparedObj.toString())) {
                value += 2;
            }
        }
        
        comparedProps = selectedIndividual.listProperties(country);
        while (comparedProps.hasNext()) {
            Statement comparedStatement = (Statement) comparedProps.next();
            Object comparedObj = comparedStatement.getLiteral().getValue();
            if (newIndividual.getCountry().length() > 0 && newIndividual.getCountry().equals(comparedObj.toString())) {
                value += 2;
            }
        }
        
        comparedProps = selectedIndividual.listProperties(register);
        while (comparedProps.hasNext()) {
            Statement comparedStatement = (Statement) comparedProps.next();
            Object comparedObj = comparedStatement.getLiteral().getValue();
            if (newIndividual.getRegister().length() > 0 && newIndividual.getRegister().equals(comparedObj.toString())) {
                value += 2;
            }
        }
        
        StmtIterator testedProps = selectedIndividual.listProperties();
        while (testedProps.hasNext()) {
            Statement testedStatement = (Statement) testedProps.next();
            String localName = testedStatement.getPredicate().getLocalName();
            if (!localName.equals("HAS-AGE") && 
                !localName.equals("HAS-COUNTRY") && 
                !localName.equals("HAS-REGISTER") &&
                !localName.equals("HAS-ARTIST") &&
                !localName.equals("HAS-SELF_VIEW") &&
                !localName.equals("HAS-PLAY_COUNT") &&
                !localName.equals("HAS-LISTENER") &&
                !localName.equals("HAS-DURATION") &&
                !localName.equals("type") &&
                !localName.equals("HAS-GENDER")) {
                for (String tag : newIndividual.getTags()) {
                    if (localName.equals(tag)) {
                        value += 1;
                        break;
                    }
                }
            }
        }
        return value;
    }

    public List<Individual> getInstanceList(OntModel model, OntClass caseObj) {
        ExtendedIterator instances = caseObj.listInstances();
        List<Individual> individualList = new ArrayList<Individual>();
        while (instances.hasNext()) {
            individualList.add(model.getIndividual(instances.next().toString()));
        }
        System.out.println("instance list size: " + individualList.size());
        return individualList;
    }
}
