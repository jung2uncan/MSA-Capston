package cinema.external;

public class Payment {

    private Long payId;
    private Long rsvId;
    private Long movieId;
    private String status;

    public Long getPayId() {
        return payId;
    }
    public void setPayId(Long payId) {
        this.payId = payId;
    }
    public Long getRsvId() {
        return rsvId;
    }
    public void setRsvId(Long rsvId) {
        this.rsvId = rsvId;
    }
    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

}
