package cinema;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

 @RestController
 public class MovieController {

        @Autowired
        MovieRepository MovieRepository;

        @RequestMapping(value = "/check/chkAndReqReserve",
                        method = RequestMethod.GET,
                        produces = "application/json;charset=UTF-8")

        public boolean chkAndReqReserve(HttpServletRequest request, HttpServletResponse response) throws Exception {
                System.out.println("##### /check/chkAndReqReserve  called #####");

                // Parameter로 받은 MovieID 추출
                long MovieId = Long.valueOf(request.getParameter("MovieId"));
                System.out.println("######################## chkAndReqReserve MovieId : " + MovieId);

                // MovieID 데이터 조회
                Optional<Movie> res = MovieRepository.findById(MovieId);
                Movie Movie = res.get(); // 조회한 Movie 데이터
                System.out.println("######################## chkAndReqReserve Movie.getStatus() : " + Movie.getStatus());

                // Movie의 상태가 available이면 true
                boolean result = false;
                if(Movie.getStatus().equals("available")) {
                        result = true;
                } 

                System.out.println("######################## chkAndReqReserve Return : " + result);
                return result;
        }
 }
