package cinema;

import javax.persistence.*;
//import java.util.List;

@Entity
@Table(name="Movieview_table")
public class Movieview {

        @Id
        private Long movieId;
        private String desc;
        private Long reviewCnt;
        private String movieStatus;
        private Long rsvId;
        private String rsvStatus;
        private Long payId;
        private String payStatus;


        public Long getMovieId() {
            return movieId;
        }

        public void setId(Long movieId) {
            this.movieId = movieId;
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

        public String getMovieStatus() {
            return movieStatus;
        }

        public void setMovieStatus(String movieStatus) {
            this.movieStatus = movieStatus;
        }

        public Long getRsvId() {
            return rsvId;
        }

        public void setRsvId(Long rsvId) {
            this.rsvId = rsvId;
        }

        public String getRsvStatus() {
            return rsvStatus;
        }

        public void setRsvStatus(String rsvStatus) {
            this.rsvStatus = rsvStatus;
        }

        public Long getPayId() {
            return payId;
        }

        public void setPayId(Long payId) {
            this.payId = payId;
        }

        public String getPayStatus() {
            return payStatus;
        }

        public void setPayStatus(String payStatus) {
            this.payStatus = payStatus;
        }

}
