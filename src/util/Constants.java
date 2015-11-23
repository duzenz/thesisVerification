package util;

public class Constants {

    public static final String baseOwlFileLocation = "original_data//recommend.owl";
    public static final String dataLocations = "data//";
    public static final String namespace = "http://www.example.com/ontologies/recommend.owl#";
    public static final String absoluteDataLocation = "D://thesisWorkspace//thesisVerification//data//";
    
    
    //table names
    public static final String rawTable = "lastfm";
    public static final String dataTable = "yearly_table";
    public static final String trainingTable = "training_";
    public static final String trainingUserTrackTable = "training_user_track_";
    public static final String testTable = "test_";
    public static final String testUserTrackTable = "test_user_track_";
    public static final String startDate = "2008-01-01";
    public static final String endDate = "2008-03-31";
    
    
    //cf constants
    public static final int threshold = 15;
    public static final int recommendationCount = 20;
    public static final String cfOutputFileName = "cf_test_results_";
    public static final String cfPrecisionResults = "cf_precision_results_";
    public static final int listenThreshold = 1;
    public static final int historyLimit = 15;
    public static final int historyRecommendCount = 10;
    
    //cbr constants
    public static final String trainingOwlFileName = "training_";
    public static boolean ASC = true;
    public static boolean DESC = false;
    public static int cbrRecommendationCount = 5;
    public static String cbrPrecisionResults = "cbr_precision_results_";
}
