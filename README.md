# ants-job

Write your own distributed job scheduling framework using ETCD and Spring Boot
https://medium.com/nerd-for-tech/write-your-own-distributed-job-scheduling-framework-using-etcd-and-spring-boot-83dbdb1a056b

To start a master node:
java -jar -Dspring.profiles.active=master -Dserver.port=8080  ants-job-0.0.1-SNAPSHOT.jar

To start two worker nodes:
java -jar -Dspring.profiles.active=worker -Dserver.port=8081 ants-job-0.0.1-SNAPSHOT.jar
java -jar -Dspring.profiles.active=worker -Dserver.port=8082 ants-job-0.0.1-SNAPSHOT.jar