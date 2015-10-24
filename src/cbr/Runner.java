package cbr;

public class Runner {

    public static void main(String[] args) {
        for (int i = 1; i <= 3; i++) {
            BuildTrainingOwlFile bt = new BuildTrainingOwlFile("training_" + i + ".owl", "training_user_track_" + i);
            bt.runOperations();
        }
    }
}
