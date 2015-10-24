package model;

public class Recommendation {

    private int userId;
    private int trackId;
    private int recommended;
    private int order;
    private String value;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public int getRecommended() {
        return recommended;
    }

    public void setRecommended(int recommended) {
        this.recommended = recommended;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Recommendation(int userId, int trackId, int recommended, String value, int order) {
        super();
        this.userId = userId;
        this.trackId = trackId;
        this.recommended = recommended;
        this.value = value;
        this.order = order;
    }
}
