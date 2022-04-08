package cinema;

public class ReservationCreated extends AbstractEvent {

    private Long rsvId;
    private Long roomId;
    private String status;
    private Long payId;

    public ReservationCreated(){
        super();
    }

    public Long getRsvId() {
        return rsvId;
    }

    public void setRsvId(Long rsvId) {
        this.rsvId = rsvId;
    }
    public Long getMovieId() {
        return roomId;
    }

    public void setMovieId(Long roomId) {
        this.roomId = roomId;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getPayId() {
        return payId;
    }

    public void setPayId(Long payId) {
        this.payId = payId;
    }
}
