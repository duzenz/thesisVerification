package model;

public class UserTrack {

    private int userId;
    private int trackId;
    private int listenCount;
    
    @Override
    public String toString() {
        return "UserTrack [userId=" + userId + ", trackId=" + trackId + ", listenCount=" + listenCount + "]";
    }

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
    
}
