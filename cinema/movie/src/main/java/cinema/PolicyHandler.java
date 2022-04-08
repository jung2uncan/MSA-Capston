package cinema;

import java.util.Optional;

//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import cinema.config.kafka.KafkaProcessor;

@Service
public class PolicyHandler{


    @Autowired
    private MovieRepository movieRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReviewCreated_UpdateReviewCnt(@Payload ReviewCreated reviewCreated){

        if(reviewCreated.isMe()){

            ////////////////////////////////////////
            // 리뷰 등록 시 -> Movie의 리뷰 카운트 증가
            ////////////////////////////////////////
            System.out.println("##### listener UpdateReviewCnt : " + reviewCreated.toJson());

            long movieIdOfReview = reviewCreated.getMovieId(); // 등록된 리뷰의 MovieID

            updateReviewCnt(movieIdOfReview, +1); // 리뷰건수 증가
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReviewDeleted_UpdateReviewCnt(@Payload ReviewDeleted reviewDeleted){
        if(reviewDeleted.isMe()){
            ////////////////////////////////////////
            // 리뷰 삭제 시 -> Movie의 리뷰 카운트 감소
            ////////////////////////////////////////
            System.out.println("##### listener UpdateReviewCnt : " + reviewDeleted.toJson());

            long movieIdOfReview = reviewDeleted.getMovieId(); // 삭제된 리뷰의 MovieID

            updateReviewCnt(movieIdOfReview, -1); // 리뷰건수 감소
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationConfirmed_ConfirmReserve(@Payload ReservationConfirmed reservationConfirmed){

        if(reservationConfirmed.isMe()){

            ////////////////////////////////////////////////////////////////////
            // 예약 확정 시 -> Movie의 status => reserved, lastAction => reserved
            ////////////////////////////////////////////////////////////////////

            System.out.println("##### listener ConfirmReserve : " + reservationConfirmed.toJson());

            long movieId = reservationConfirmed.getMovieId(); 

            updateMovieStatus(movieId, "reserved", "reserved"); // Status Update

        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCancelled_Cancel(@Payload ReservationCancelled reservationCancelled){

        if(reservationCancelled.isMe()){
            //////////////////////////////////////////////////////////////////////
            // 예약 취소 시 -> Movie의 status => available, lastAction => cancelled
            //////////////////////////////////////////////////////////////////////
            System.out.println("##### listener Cancel : " + reservationCancelled.toJson());

            long movieId = reservationCancelled.getMovieId(); // 삭제된 리뷰의 MovieID

            updateMovieStatus(movieId, "available", "cancelled"); // Status Update
        }
    }

    
    private void updateReviewCnt(long movieId, long num)     {

        //////////////////////////////////////////////
        // movieId 영화 데이터의 ReviewCnt를 num 만큼 가감
        //////////////////////////////////////////////

        // Movie 테이블에서 movieId의 Data 조회 -> movie
        Optional<Movie> res = movieRepository.findById(movieId);
        Movie movie = res.get();

        System.out.println("movieId    : " + movie.getMovieId());
        System.out.println("reviewCnt : " + movie.getReviewCnt());

        // movie 값 수정
        movie.setReviewCnt(movie.getReviewCnt() + num); // 리뷰건수 증가/감소
        movie.setLastAction("review");  // lastAction 값 셋팅

        System.out.println("Edited reviewCnt : " + movie.getReviewCnt());
        System.out.println("Edited lastAction : " + movie.getLastAction());

        /////////////
        // DB Update
        /////////////
        movieRepository.save(movie);

    }

        
    private void updateMovieStatus(long movieId, String status, String lastAction)     {

        //////////////////////////////////////////////
        // movieId 영화 데이터의 status, lastAction 수정
        //////////////////////////////////////////////

        // Movie 테이블에서 movieId의 Data 조회 -> movie
        Optional<Movie> res = movieRepository.findById(movieId);
        Movie movie = res.get();

        System.out.println("movieId      : " + movie.getMovieId());
        System.out.println("status      : " + movie.getStatus());
        System.out.println("lastAction  : " + movie.getLastAction());

        // movie 값 수정
        movie.setStatus(status); // status 수정 
        movie.setLastAction(lastAction);  // lastAction 값 셋팅

        System.out.println("Edited status     : " + movie.getStatus());
        System.out.println("Edited lastAction : " + movie.getLastAction());

        /////////////
        // DB Update
        /////////////
        movieRepository.save(movie);

    }

}
