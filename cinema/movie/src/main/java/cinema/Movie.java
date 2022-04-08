package cinema;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
//import java.util.List;
//import java.util.Date;

@Entity
@Table(name="Movie_table")
public class Movie {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long movieId;
    private String status;
    private String desc;
    private Long reviewCnt;
    private String lastAction;

    @PostPersist
    public void onPostPersist(){

        //////////////////////////////
        // Movie 테이블 Insert 후 수행
        //////////////////////////////

        // 기본값 셋팅
        lastAction = "register";    // Insert는 항상 register
        reviewCnt = 0L;             // 리뷰 건수는 따로 입력이 들어오지 않아도 기본값 0
        status = "available";       // 최초 등록시 항상 이용가능

        // MovieRegistered Event 발생
        MovieRegistered MovieRegistered = new MovieRegistered();
        BeanUtils.copyProperties(this, MovieRegistered);
        MovieRegistered.publishAfterCommit();

    }

    @PostUpdate
    public void onPostUpdate(){

        /////////////////////////////
        // Movie 테이블 Update 후 수행
        /////////////////////////////

        System.out.println("lastAction : " + lastAction);

        // MovieModified Event 발생 혹은 리뷰 이벤트 발생시
        if(lastAction.equals("modify") || lastAction.equals("review")) {
            MovieModified MovieModified = new MovieModified();
            BeanUtils.copyProperties(this, MovieModified);
            MovieModified.publishAfterCommit();
        }

        // MovieReserved Event 발생
        if(lastAction.equals("reserved")) {
            MovieReserved MovieReserved = new MovieReserved();
            BeanUtils.copyProperties(this, MovieReserved);
            MovieReserved.publishAfterCommit();
        }

        // MovieCancelled Event 발생
        if(lastAction.equals("cancelled")) {
            MovieCancelled MovieCancelled = new MovieCancelled();
            BeanUtils.copyProperties(this, MovieCancelled);
            MovieCancelled.publishAfterCommit();
        }

        // review 작성/삭제 시 -> Do Nothing
        //if(lastAction.equals("review")) {
            // Do Nothing
        //}
        
    }

    @PreRemove
    public void onPreRemove(){

        ////////////////////////////
        // Movie 테이블 Delete 전 수행
        ////////////////////////////

        // MovieDeleted Event 발생
        MovieDeleted MovieDeleted = new MovieDeleted();
        BeanUtils.copyProperties(this, MovieDeleted);
        MovieDeleted.publishAfterCommit();

    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long MovieId) {
        this.movieId = MovieId;
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
