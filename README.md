# ants-job

To start a master node:
java -jar -Dspring.profiles.active=master -Dserver.port=8080  ants-job-0.0.1-SNAPSHOT.jar

To start two worker nodes:
java -jar -Dspring.profiles.active=worker -Dserver.port=8081 ants-job-0.0.1-SNAPSHOT.jar
java -jar -Dspring.profiles.active=worker -Dserver.port=8082 ants-job-0.0.1-SNAPSHOT.jar