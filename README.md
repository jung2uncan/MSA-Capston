
*MSA-Capston*
--------------------------------------------------------------------------------------------------
![영화를보고  무료 아이콘](https://cdn-icons-png.flaticon.com/512/4221/4221419.png)

# Table of contents
 - **예제 - 🎥영화예약하기 (Cinema)**
		
		- 서비스 시나리오
		
		- Check Potint
		
		- 분석/설계
		
		- 구현 
		
		- 운영


## #서비스 시나리오
#### 🎥영화 예약 시스템 

#### 💻**기능적 요구사항**
1. 매니저가 상영할 영화를 등록/수정/삭제한다.
2.  고객이 영화를 선택하여 예약한다.
3.  예약과 동시에 결제가 진행된다.
4.  예약이 되면 예약 내역(Message)이 전달된다.
5.  고객이 예약을 취소할 수 있다.
6.  예약 사항이 취소될 경우 취소 내역(Message)이 전달된다.
7.  영화관람 후에 후기(review)를 남길 수 있다.
8.  전체적인 영화에 대한 정보 및 예약 상태 등을 한 화면에서 확인 할 수 있다.(viewpage)


#### 💬**비기능적 요구사항**
1.  트랜잭션
    1)  결제가 되지 않은 예약 건은 성립되지 않아야 한다. (Sync 호출)
2.  장애격리
    1)  영화 등록 및 메시지 전송 기능이 수행되지 않더라도 예약은 365일 24시간 받을 수 있어야 한다 Async (event-driven), Eventual Consistency
    2)  예약 시스템이 과중되면 사용자를 잠시동안 받지 않고 잠시 후에 하도록 유도한다 Circuit breaker, fallback
3.  성능
    1)  모든 방에 대한 정보 및 예약 상태 등을 한번에 확인할 수 있어야 한다 (CQRS)
    2)  예약의 상태가 바뀔 때마다 메시지로 알림을 줄 수 있어야 한다 (Event driven)


## #:heavy_check_mark:CheckPoints
  1. Saga
  2. CQRS
  3. Correlation
  4. Req / Resp
  5. Gateway
  6. Deploy / Pipeline
  7. Circuit Breaker
  8. Autoscale (HPA)
  9. Zero-downtime deploy (readiness probe)
  10. ConfigMap/Persistence Volume
  11. Polyglot
  12. Self-healing (liveness probe)
 *https://workflowy.com/s/assessment/qJn45fBdVZn4atl3*


## #분석/설계
#### #1 Evnent Storming 결과
```
주요 도메인(Core): Cinema, Reservation, Pay
서브 도메인(Supporting) : Message
```
![EventStorming](https://user-images.githubusercontent.com/74287598/162121076-4d53ae8f-b0f1-48b0-a11b-468500201a9b.JPG)



#### #2 요구사항 만족 확인하기👌

![EventStorming_Line](https://user-images.githubusercontent.com/74287598/162121828-687a4b72-c7ec-4056-a016-d20fd7d64007.JPG)

``````
 - 매니저가 상영할 영화를 등록/수정/삭제한다. //Green Line
 - 고객이 영화를 선택하여 예약한다. //Red Line 
 - 예약과 동시에 결제가 진행된다. 	//Red Line
 - 예약이 되면 예약 내역(Message)이 전달된다. //Pnik Line
 - 고객이 예약을 취소할 수 있다. //Blue Line
 - 예약 사항이 취소될 경우 취소 내역(Message)이 전달된다. //Pnik Line
 - 영화관람 후에 후기(review)를 남길 수 있다. //Pulple Line
 - 전체적인 영화에 대한 정보 및 예약 상태 등을 한 화면에서 확인 할 수 있다.(viewpage) //오른쪽 아래
``````


#### #3 헥사고날 아키텍처 다이어그램 도출
![헥사고날아키텍쳐](https://user-images.githubusercontent.com/74287598/162122331-a13dc4e1-37f6-4c07-aee1-96f3e8d5a99d.JPG)


## #구현

### #1 각 마이크로 서비스 실행
```
 cd /message
 mvn spring-boot:run
 ```
```
 cd /movie
 mvn spring-boot:run
 ```
```
 cd /payment
 mvn spring-boot:run
 ```
```
 cd /reservation
 mvn spring-boot:run
 ```
```
 cd /viewpage
 mvn spring-boot:run
 ```
 ```
 cd /gateway
 mvn spring-boot:run
 ```
 
##
 ### #2 CheckPoint

#### - CQRS
영화(Movie) 의 예약가능 여부, 리뷰 및 예약/결재 등 총 Status 에 대하여 고객(Customer)이 조회 할 수 있도록 CQRS 로 구현하였다. 

  *MovieView*

![MovieView_Table](https://user-images.githubusercontent.com/74287598/162131283-aa24351e-c6f0-4855-a901-4b40c861356b.JPG)

- 영화가 새로 등록되었을 때,  viewpage의 ViewHandler를 통해 새로운 영화 객체 생성
("MovieRegisterd" 이벤트 발생 시, Pub/Sub 기반으로 MovieView 테이블에 정보 저장)

![whenMovieRegistered_then_CREATE_1](https://user-images.githubusercontent.com/74287598/162131739-a624033b-4813-4095-9911-6b659607db55.JPG)

- 결재가 승인되었을 때,  viewpage의 ViewHandler를 통해 해당 영화예약 정보 업데이트
("PaymentApproved" 이벤트 발생 시, Pub/Sub 기반으로 MovieView 테이블에 정보 업데이트)

![whenPaymentApproved_then_UPDATE_3](https://user-images.githubusercontent.com/74287598/162135714-78ceb114-775e-4efd-ae1d-8f746b4ea0a2.JPG)

-    Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터 개발 및 자동 생성

![Repository Pattern_JPA](https://user-images.githubusercontent.com/74287598/162201496-2d97d08b-6b3c-4787-9126-0c806f0a1c50.JPG)


#### 등록과 조회의 분리
##### ***영화 등록하기***
```
http POST localhost:8088/movies desc="Beautiful Movie~"  
http POST localhost:8088/movies desc="Happy Story!" 
```
![영화등록](https://user-images.githubusercontent.com/74287598/162136536-4e413311-5ab5-4db9-88c7-8b1c183397e9.JPG)

##### ***등록된 영화 조회하기***
![등록된 영화 조회](https://user-images.githubusercontent.com/74287598/162136983-584f1aba-391b-4a52-8c85-9652b9853b9a.JPG)

-----------------

#### - Correlation
영화 예약 / 취소에 따른 Payment의 상태 변화
1) 영화 예약하기
```
update reservation_table set movie_id=?, pay_id=?, status=?  where rsv_id=?
```
![1번 영화예약](https://user-images.githubusercontent.com/74287598/162140484-c911b840-50c6-43c1-bb08-e62e5d84dcc0.JPG)

2) 영화 예약에 따른 결재 상태 Update
```
update  payment_table set movie_id=?,rsv_id=?,status=? where pay_id=?
```
![예약 후 결재 상태 변경](https://user-images.githubusercontent.com/74287598/162140487-237168b8-9124-4acd-88df-6e6546191853.jpg)

3) 영화 예약 취소하기
```
http PATCH localhost:8088/reservations/1 status=reqCancel
```
![예약취소](https://user-images.githubusercontent.com/74287598/162140502-82922cf7-46d4-483b-a51a-3f187f3a12d0.JPG)

4) 예약 취소 후, 결재 상태 변경
5) 
![예약 취소 후 결재 상태 변경](https://user-images.githubusercontent.com/74287598/162140510-32ff2847-a5ea-4fae-a593-76120a4a2853.JPG)

![예약취소2](https://user-images.githubusercontent.com/74287598/162141479-72171540-73e7-4a37-9ce8-ce946bc2d5d0.JPG)

#### *Kafka consumer 이벤트 모니터링*
- 카프카를 이용하여 Pub/Sub 으로 하나 이상의 서비스가 연동
```
root@labs--2065963007:/home/project# /usr/local/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic cinema --from-beginning
```
```
{"eventType":"MovieRegistered","timestamp":"20220407055544","movieId":1,"status":"available","desc":"Beautiful Movie~","reviewCnt":0,"lastAction":"register","me":true}
{"eventType":"MovieRegistered","timestamp":"20220407063955","movieId":1,"status":"available","desc":"Beautiful Movie~","reviewCnt":0,"lastAction":"register","me":true}
{"eventType":"MovieRegistered","timestamp":"20220407064009","movieId":2,"status":"available","desc":"Happy Story!","reviewCnt":0,"lastAction":"register","me":true}
{"eventType":"PaymentApproved","timestamp":"20220407070249","payId":1,"rsvId":1,"movieId":1,"status":"paid","me":true}
{"eventType":"ReservationCreated","timestamp":"20220407070249","rsvId":1,"status":null,"payId":null,"movieId":1,"me":true}
{"eventType":"ReservationConfirmed","timestamp":"20220407070250","rsvId":1,"movieId":1,"status":"reserved","payId":1,"me":true}
{"eventType":"MovieReserved","timestamp":"20220407070250","status":"reserved","desc":"Beautiful Movie~","reviewCnt":0,"lastAction":"reserved","movieId":1,"me":true}
{"eventType":"PaymentApproved","timestamp":"20220407070320","payId":2,"rsvId":2,"movieId":1,"status":"paid","me":true}
{"eventType":"ReservationCreated","timestamp":"20220407070320","rsvId":2,"status":"reqReserve","payId":null,"movieId":1,"me":true}
{"eventType":"ReservationConfirmed","timestamp":"20220407070320","rsvId":2,"movieId":1,"status":"reserved","payId":2,"me":true}
{"eventType":"ReservationCancelRequested","timestamp":"20220407070636","rsvId":1,"movieId":1,"status":"reqCancel","payId":1,"me":true}
{"eventType":"PaymentCancelled","timestamp":"20220407070636","payId":1,"rsvId":1,"movieId":1,"status":"cancelled","me":true}
{"eventType":"ReservationCancelled","timestamp":"20220407070636","rsvId":1,"movieId":1,"status":"cancelled","payId":1,"me":true}
{"eventType":"MovieCancelled","timestamp":"20220407070636","movieId":1,"status":"available","desc":"Beautiful Movie~","reviewCnt":0,"lastAction":"cancelled","me":true}
```
----------
#### - Req / Resp
 *FeignClient 적용*

![FeignClient](https://user-images.githubusercontent.com/74287598/162142655-39b644a0-77e4-4847-9557-a0976a711a48.JPG)

-----------
#### - 장애 격리 (*비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트*)
영화 예매(결제)가 이루어진 후에 영화관 관리 시스템의 상태가 업데이트 되고, 예약 시스템의 상태가 업데이트 되며, 예약 및 취소 메시지가 전송되는 시스템과의 통신 행위는 비동기식으로 처리한다.

 - 이를 위하여 결제가 승인되면 결제가 승인 되었다는 이벤트를 카프카로 송출한다. (Publish)
```
# Payment.java

package cinema;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Payment_table")
public  class  Payment {
@Id
@GeneratedValue(strategy=GenerationType.AUTO)
private  Long  payId;
private  Long  rsvId;
private  Long  movieId;
private  String  status;

@PostPersist
public  void  onPostPersist(){
	////////////////////////////
	// 결제 승인 된 경우
	////////////////////////////

	// 이벤트 발행 -> PaymentApproved
	PaymentApproved  paymentApproved = new  PaymentApproved();
	BeanUtils.copyProperties(this, paymentApproved);
	paymentApproved.publishAfterCommit();
}
```
- 예약 시스템에서는 결제 승인 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다.

```
# Reservation.java

...
@PostUpdate
public  void  onPostUpdate(){
////////////////////////////////
// RESERVATION에 UPDATE 된 경우
////////////////////////////////

if(this.getStatus().equals("reqCancel")) {
	///////////////////////
	// 취소 요청 들어온 경우
	///////////////////////

	// 이벤트 발생 --> reservationCancelRequested
	ReservationCancelRequested  reservationCancelRequested = new  ReservationCancelRequested();
	BeanUtils.copyProperties(this, reservationCancelRequested);
	reservationCancelRequested.publishAfterCommit();
}

...
```
- 메시지 서비스는 영화 등록 및  예약/결제와 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 메시지 서비스가 장애 상태 라도 영화 등록 및  예약을 받는데 문제가 없다.


***#메시지 서비스 (message) 기동 안 한 상태***

```
# 영화 등록
http POST localhost:8088/movies desc="Message MicroService id down!!!!!!"   #Success
```
![장애격리1](https://user-images.githubusercontent.com/74287598/162206755-fa13b2fb-3fea-404f-a0a9-3c7007ff7a11.JPG)

```
# 영화 예약
http POST localhost:8088/reservations movieId=1 status=reqReserve    #Success
```
![장애격리2](https://user-images.githubusercontent.com/74287598/162206760-9c6920c7-721c-4191-a0ea-78eef29c603a.JPG)

```
# 예약 상태 확인
http GET localhost:8088/reservations    #메시지 서비스와 상관없이 예약 상태는 정상 확인
```
![장애격리3_정상예약](https://user-images.githubusercontent.com/74287598/162206752-ec83262c-715e-4ccf-9cf9-4e82e9671f05.JPG)


----------------
#### - Gateway
*gateway application.yaml*
![gateway_application_yaml파일](https://user-images.githubusercontent.com/74287598/162144121-93987952-74d1-4df6-b1d2-e7a00813fe36.JPG)

-------------------------
####  - Polyglot
*payment MicroService의 경우 Database 'HSQL'사용*
![payment DB HSQL 사용](https://user-images.githubusercontent.com/74287598/162151005-58aacce3-6f33-44be-a08c-5eab7019871c.JPG)

```
[INFO] >>> spring-boot-maven-plugin:2.1.9.RELEASE:run (default-cli) > test-compile @ payment >>>
Downloading from central: https://repo.maven.apache.org/maven2/org/hsqldb/hsqldb/2.4.1/hsqldb-2.4.1.pom
Downloaded from central: https://repo.maven.apache.org/maven2/org/hsqldb/hsqldb/2.4.1/hsqldb-2.4.1.pom (1.3 kB at 1.7 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/hsqldb/hsqldb/2.4.1/hsqldb-2.4.1.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/hsqldb/hsqldb/2.4.1/hsqldb-2.4.1.jar (1.6 MB at 2.1 MB/s)


... 생략
2022-04-07 08:01:20.281  INFO 24092 --- [           main] org.hibernate.dialect.Dialect            : HHH000400: Using dialect: org.hibernate.dialect.HSQLDialect
2022-04-07 08:01:20.318 DEBUG 24092 --- [           main] o.h.type.spi.TypeConfiguration$Scope     : Scoping TypeConfiguration [org.hibernate.type.spi.TypeConfiguration@5c2b6403] to MetadataBuildingContext [org.hibernate.boot.internal.MetadataBuildingContextRootImpl@619ac06c]
2022-04-07 08:01:20.523 DEBUG 24092 --- [           main] o.h.type.spi.TypeConfiguration$Scope     : Scoping TypeConfiguration [org.hibernate.type.spi.TypeConfiguration@5c2b6403] to SessionFactoryImpl [org.hibernate.internal.SessionFactoryImpl@1c51754c]
Hibernate: 
    
    drop table payment_table if exists Hibernate: 
    drop sequence hibernate_sequence if exists Hibernate: create sequence hibernate_sequence start with 1 increment by 1
Hibernate:     
    create table payment_table (
       pay_id bigint not null,
        movie_id bigint,
        rsv_id bigint,
        status varchar(255),
        primary key (pay_id)
    )
```


## #운영
#### - Deploy 
- Kubernetes용 Deployment.yaml 을 작성하고 Kubernetes에 Deploy를 생성

*deployment.yml*
![deployment yaml](https://user-images.githubusercontent.com/74287598/162158476-65cdf8d0-96b9-421f-9dd8-5b1eca75424e.JPG)

```
===================네임스페이스 생성========================
kubectl create namespace cinema

===================도커 이미지 생성========================
mvn package
docker build -t jung2uncan/gateway:v1 .
docker run --name gateway -d -p 8080:80 openjdk:8u212-jdk-alpine
docker push jung2uncan/gateway:v1

===================도커 이미지 기반 컨테이너 생성========================
kubectl create deployment gateway --image=jung2uncan/gateway:v1 -n cinema

===================컨테이너 접속 채널 생성========================
kubectl expose deploy gateway --type=LoadBalancer --port=80 -n cinema

===================AWS 컨테이너 레지스트리(ECR) 생성========================
aws ecr create-repository --repository-name user03-gateway --image-scanning-configuration scanOnPush=true --region ap-northeast-2

========================도커 로그인========================

=========Dockerizing & Container Registry에 Push=========
docker build -t 979050235289.dkr.ecr.ap-northeast-2.amazonaws.com/user03-gateway:v1 .
docker push 979050235289.dkr.ecr.ap-northeast-2.amazonaws.com/user03-gateway:v1 

===================YAML 기반 서비스 배포하기========================
cd kubernetes/
kubectl apply -f deployment.yml

===================YAML 기반 서비스 생성하기========================
kubectl apply -f service.yaml 

========================API Gateay 엔드포인트 확인========================
Service  및 엔드포인트 확인 
kubectl get svc -n cinema           
           
```

```kubectl get deploy -o wide -n cinema```

![kubectl get deploy](https://user-images.githubusercontent.com/74287598/162159574-591a7d9e-4e61-4fff-bb90-607e6511fd9e.JPG)

```kubectl get service -n cinema```
![kubectl get service](https://user-images.githubusercontent.com/74287598/162163724-a5ff03fe-95c0-4851-9baa-f1f4ca3e592d.JPG)

---------------

#### - PipeLine 
각 구현체들은 각자의 source repository 에 구성되었고, 사용한 CI/CD는 buildspec.yml을 이용한 AWS codebuild를 사용함.

- -CodeBuild 프로젝트를 생성하고 AWS_ACCOUNT_ID, KUBE_URL, KUBE_TOKEN 환경 변수 세팅을 한다.
#### CodeBuild 에서 EKS 연결

-   Service Account 생성

```yaml
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ServiceAccount
metadata:
  name: eks-admin
  namespace: kube-system
EOF

```

-   ClusterRoleBinding 생성

```yaml
cat <<EOF | kubectl apply -f -
apiVersion: rbac.authorization.k8s.io/v1beta1
kind: ClusterRoleBinding
metadata:
  name: eks-admin
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- kind: ServiceAccount
  name: eks-admin
  namespace: kube-system
EOF
```
-   SA로 EKS 접속 토큰 가져오기

```yaml
kubectl -n kube-system describe secret eks-admin
```
![KUBE_TOKEN 확인](https://user-images.githubusercontent.com/74287598/162240322-9a0798ef-1166-4e55-83ae-a6a75d22c2dc.JPG)

```
buildspec.yml 파일 
마이크로 서비스 각 yml 파일 생성
```
![buildspec](https://user-images.githubusercontent.com/74287598/162241470-19de3940-6006-4bea-9fa1-fa8b41b52bc9.JPG)


***codebuild 실행***
- codebuild 프로젝트 및 빌드 이력
![codebuild 성공이력1](https://user-images.githubusercontent.com/74287598/162242687-7c2c6fd3-cb9f-44cc-8dc1-06c9ef391afc.JPG)

-   codebuild 빌드 내역 (Movie 서비스 세부)
![codebuild 성공이력2](https://user-images.githubusercontent.com/74287598/162242704-f3887c73-a0de-414d-bf59-5157a064c154.JPG)

-  codebuild 빌드 내역 (전체 이력 조회)
![codebuild 성공이력3](https://user-images.githubusercontent.com/74287598/162242713-6caeef91-48c2-48c4-95e8-9794c201e18f.JPG)

----------------------

#### Circuit Breaker



-------------
#### Autoscale (HPA)


--------------------
#### Zero-downtime deploy (readiness probe)


----------------------
#### ConfigMap/Persistence Volume


--------------
#### Self-healing (liveness probe)
