
package cinema;

public class MovieReserved extends AbstractEvent {

    private Long MovieId;
    private String status;
    private String desc;
    private Long reviewCnt;
    private String lastAction;

    public Long getMovieId() {
        return MovieId;
    }

    public void setMovieId(Long MovieId) {
        this.MovieId = MovieId;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
    public Long getReviewCnt() {
        return reviewCnt;
    }

    public void setReviewCnt(Long reviewCnt) {
        this.reviewCnt = reviewCnt;
    }
    public String getLastAction() {
        return lastAction;
    }

    public void setLastAction(String lastAction) {
        this.lastAction = lastAction;
    }
}

