package model;

public class Track {

    private int trackId;
    private String trackMbid;
    private String trackName;
    private String duration;
    private String listener;
    private String playCount;
    private String tags;
    private String artistName;
    private String artistMbid;

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public String getTrackMbid() {
        return trackMbid;
    }

    public void setTrackMbid(String trackMbid) {
        this.trackMbid = trackMbid;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getListener() {
        return listener;
    }

    public void setListener(String listener) {
        this.listener = listener;
    }

    public String getPlayCount() {
        return playCount;
    }

    public void setPlayCount(String playCount) {
        this.playCount = playCount;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtistMbid() {
        return artistMbid;
    }

    public void setArtistMbid(String artistMbid) {
        this.artistMbid = artistMbid;
    }

    @Override
    public String toString() {
        return "Track [trackId=" + trackId + ", trackMbid=" + trackMbid + ", trackName=" + trackName + ", duration=" + duration + ", listener=" + listener + ", playCount=" + playCount + ", tags="
                + tags + ", artistName=" + artistName + ", artistMbid=" + artistMbid + "]";
    }

}
