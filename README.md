
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
시나리오는 예약(reservation)--> 결재(payment) 시의 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 예약 요청이 과도할 경우 CB 를 통하여 장애격리.

```yaml
# DestinationRule 를 생성하여 circuit break 가 발생할 수 있도록 설정 최소 connection pool 설정 
# cmd 터미널 창에 입력
kubectl apply -f - << EOF
  apiVersion: networking.istio.io/v1alpha3
  kind: DestinationRule
  metadata:
    name: dr-payment
    namespace: cinema
  spec:
    host: payment
    trafficPolicy:
      outlierDetection:
        consecutive5xxErrors: 1
        interval: 1s
        baseEjectionTime: 3m
        maxEjectionPercent: 100
EOF
```

-  Circuit Breaker 테스트 환경 설정
```
kubectl scale deploy payment --replicas=3
```
```
root@labs--2065963007:/home/project# kubectl get pod -o wide -n cinema
NAME                           READY   STATUS    RESTARTS   AGE     IP               NODE                                                NOMINATED NODE   READINESS GATES
payment-5bcbc89f89-fs2ph   2/2     Running   0          147m    192.168.65.180   ip-192-168-64-163.ap-northeast-2.compute.internal   <none>           <none>
payment-5bcbc89f89-hrsm9   2/2     Running   0          4s      192.168.27.166   ip-192-168-0-247.ap-northeast-2.compute.internal    <none>           <none>
payment-5bcbc89f89-pzqpx   2/2     Running   0          4s      192.168.50.150   ip-192-168-39-51.ap-northeast-2.compute.internal    <none>           <none>
siege-75d5587bf6-mpg9f         2/2     Running   0          4h28m   192.168.39.181   ip-192-168-39-51.ap-northeast-2.compute.internal    <none> 
```

- Siege 접속
```
#Siege 접속
kubectl create deploy siege --image=ghcr.io/acmexii/siege-nginx:latest
kubectl exec -it pod/siege-75d5587bf6-mpg9f -n cinema -- /bin/bash
```

- Circuit Breaker 동작 확인
```
+root@siege-75d5587bf6-fns4p:/# http http://payment:8080/actuator/echo
- HTTP/1.1 200 OK
content-length: 39
content-type: application/hal+json;charset=UTF-8
date: Fri, 08 Apr 2022 04:21:39 GMT
server: envoy
x-envoy-upstream-service-time: 215

payment-5bcbc89f89-fs2ph/192.168.65.180 

root@siege-75d5587bf6-fns4p:/# http http://payment:8080/actuator/echo
HTTP/1.1 200 OK
content-length: 40
content-type: application/hal+json;charset=UTF-8
date: Fri, 08 Apr 2022 04:21:40 GMT
server: envoy
x-envoy-upstream-service-time: 16

payment-5bcbc89f89-hrsm9/192.168.27.166

root@siege-75d5587bf6-fns4p:/# http http://payment:8080/actuator/echo
HTTP/1.1 200 OK
content-length: 40
content-type: application/hal+json;charset=UTF-8
date: Fri, 08 Apr 2022 04:21:41 GMT
server: envoy
x-envoy-upstream-service-time: 25

payment-5bcbc89f89-pzqpx/192.168.50.150
```

- Siege 실행 
 3개중 1개의 컨테이너로 접속하여 명시적으로 5xx 오류를 발생

```
# 새로운 터미널 Open
# 3개 중 하나의 컨테이너에 접속
kubectl exec -it pod/payment-5bcbc89f89-pzqpx -c payment -- /bin/sh
#
# httpie 설치 및 서비스 명시적 다운
apk update
apk add httpie
- http PUT http://localhost:8080/actuator/down
```

-   Siege로 접속한 이전 터미널에서 payment 서비스로 접속해 3회 이상 호출

```
http GET http://payment:8080/actuator/health
```
-   아래 URL을 통해 3개 중  `2개`의 컨테이너만 서비스 됨을 확인
```
+root@siege-75d5587bf6-fns4p:/# http http://payment:8080/actuator/echo
- HTTP/1.1 200 OK
content-length: 39
content-type: application/hal+json;charset=UTF-8
date: Fri, 08 Apr 2022 06:15:21 GMT
server: envoy
x-envoy-upstream-service-time: 215

payment-5bcbc89f89-fs2ph/192.168.65.180

root@siege-75d5587bf6-fns4p:/# http http://payment:8080/actuator/echo
HTTP/1.1 200 OK
content-length: 40
content-type: application/hal+json;charset=UTF-8
date: Fri, 08 Apr 2022 06:15:22 GMT
server: envoy
x-envoy-upstream-service-time: 16

payment-5bcbc89f89-hrsm9/192.168.27.166

root@siege-75d5587bf6-fns4p:/# http http://payment:8080/actuator/echo
HTTP/1.1 200 OK
content-length: 40
content-type: application/hal+json;charset=UTF-8
date: Fri, 08 Apr 2022 06:15:23 GMT
server: envoy
x-envoy-upstream-service-time: 25

payment-5bcbc89f89-fs2ph/192.168.65.180

root@siege-75d5587bf6-fns4p:/# http http://payment:8080/actuator/echo
HTTP/1.1 200 OK
content-length: 40
content-type: application/hal+json;charset=UTF-8
date: Fri, 08 Apr 2022 06:15:24 GMT
server: envoy
x-envoy-upstream-service-time: 16

payment-5bcbc89f89-hrsm9/192.168.27.166
```

-   Pool Ejection 타임(3’) 경과후엔 컨테이너 3개가 모두 동작됨이 확인
```
+root@siege-75d5587bf6-fns4p:/# http http://payment:8080/actuator/echo
- HTTP/1.1 200 OK
content-length: 39
content-type: application/hal+json;charset=UTF-8
date: Fri, 08 Apr 2022 06:15:21 GMT
server: envoy
x-envoy-upstream-service-time: 215

payment-5bcbc89f89-fs2ph/192.168.65.180

root@siege-75d5587bf6-fns4p:/# http http://payment:8080/actuator/echo
HTTP/1.1 200 OK
content-length: 40
content-type: application/hal+json;charset=UTF-8
date: Fri, 08 Apr 2022 06:15:22 GMT
server: envoy
x-envoy-upstream-service-time: 16

payment-5bcbc89f89-hrsm9/192.168.27.166

root@siege-75d5587bf6-fns4p:/# http http://payment:8080/actuator/echo
HTTP/1.1 200 OK
content-length: 40
content-type: application/hal+json;charset=UTF-8
date: Fri, 08 Apr 2022 06:15:25 GMT
server: envoy
x-envoy-upstream-service-time: 16

payment-5bcbc89f89-pzqpx/192.168.50.150
```

-------------
#### Autoscale (HPA)


--------------------
#### - Zero-downtime deploy (readiness probe)

##### **ReadinessProbe**
Pod의 생명주기중 **Pending 상태**에서의 동작, 서비스 요청에 응답가능한지 확인
Service와 연결된 Pod를 확인하여 Readiness Probe에 대해 응답이 없거나 실패 응답을 보낸다면 해당 Pod를 사용불가능한 상태라고 판단하여 ***서비스 목록에서 제외***(App구동 순간에 트래픽이 흐르지 않게)

![readiness](https://user-images.githubusercontent.com/74287598/162379634-7c2f4995-806a-4651-823e-50ec7cfeafeb.JPG)

서비스 생성

```yaml
# service.yml
apiVersion: v1
kind: Service
metadata:
	name: svc-readiness
spec:
	selector:
		app: readiness
	ports:
	- port: 8080
		targetPort: 8080
```

Service에 연결할 Pod생성

```yml
spec:
      containers:
        - name: payment
          image: 979050235289.dkr.ecr.ap-northeast-2.amazonaws.com/user03-payment:v1
          ports:
            - containerPort: 8080
      volumes:
        - name: volume
          persistentVolumeClaim:
            claimName: aws-efs
          readinessProbe:  # killing -> restart
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 10
```
-----------
#### - Self-healing (liveness probe)
- Pod의 생명주기중 **Running 상태**에서의 동작함.
컨테이너가 정상 실행중인지 확인(LivenessProbe를 설정하지 않으면 기본 상태값은 Success) -> 계속 HealthCheck
컨테이너의 상태를 주기적으로 체크하여, 응답이 없다면 ***컨테이너를 자동으로 재시작***.

![liveness](https://user-images.githubusercontent.com/74287598/162379258-61b3a4d0-6ab2-4a76-8c28-dace52320580.JPG)

*Liveness Probe*는 컨테이너 상태가 비정상이라고 판단하면, **해당 Pod를 재시작**하는 반면 **ReadinessProbe는 해당 Pod를 사용할 수 없음으로 체크하고 서비스 등에서 제외함**

Service생성

```yml
# service.yml
apiVersion: v1
kind: Service
metadata:
	name: svc-liveness
spec:
	selector:
		app: liveness
	ports:
	- port: 8080
		targetPort: 8080

```

Service에 연동할 Pod 생성

```yml
spec:
      containers:
        - name: payment
          image: 979050235289.dkr.ecr.ap-northeast-2.amazonaws.com/user03-payment:v1
          ports:
            - containerPort: 8080
      volumes:
        - name: volume
          persistentVolumeClaim:
            claimName: aws-efs
          livenessProbe:   # health Check 
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 120
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5
```

----------------------
#### ConfigMap

*1) configmap.yml 파일 생성*
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
	name: cinema-config
	namespace: cinema
data:
	# 단일 key-value
	max_reservation_per_person: "100"
	ui_properties_file_name: "user-interface.properties"

# kubectl apply -f cofingmap.yml
```

*2) deployment.yml 에 적용하기*
```yaml
    spec:
      containers:
        - name: payment
          image: 979050235289.dkr.ecr.ap-northeast-2.amazonaws.com/user03-payment:v1
          ports:
            - containerPort: 8080
          env:
			      # cofingmap에 있는 단일 key-value
            - name: MAX_RESERVATION_PER_PERSION
              valueFrom:
                configMapKeyRef:
                  name: cinema-config
                  key: max_reservation_per_person
            - name: UI_PROPERTIES_FILE_NAME
              valueFrom:
                configMapKeyRef:
                  name: cinema-config
                  key: ui_properties_file_name

# kubectl apply -f deployment.yml

```


