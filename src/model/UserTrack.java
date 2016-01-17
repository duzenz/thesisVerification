package model;

public class UserTrack {

    private int userId;
    private int trackId;
    private int listenCount;
    private float rating;
    private String time;

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

    public int getListenCount() {
        return listenCount;
    }

    public void setListenCount(int listenCount) {
        this.listenCount = listenCount;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float f) {
        this.rating = f;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "UserTrack [userId=" + userId + ", trackId=" + trackId + ", listenCount=" + listenCount + ", rating=" + rating + ", time=" + time + "]";
    }

}
