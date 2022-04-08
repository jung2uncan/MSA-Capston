
package cinema;

public class ReservationCancelled extends AbstractEvent {

    private Long rsvId;
    private Long movieId;
    private String status;

    public ReservationCancelled(){
        super();
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

