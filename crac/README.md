# README

 * build the `.jar`: `./mvnw -DskipTests clean package`
 * build the `Dockerfile`: `docker build . -t crac:latest`
 * run the new image: `docker run -it --privileged --rm -p 8080:8080 --name crac crac`. This should dump you  into the Docker image with a shell. 
 * run the JVM application ready to capture a checkpoint. `java -XX:CRaCCheckpointTo=/opt/crac-files -jar /opt/app/crac-0.0.1-SNAPSHOT.jar  `. Note the PID of the process.
 * run another shell: `docker exec -it -u root crac /bin/bash`. 
 * in that second shell, capture the checkpoint itself with: `jcmd $PID JDK.checkpoint`. Your Java process in the first shell should have exited. 
 * exit the second shell.
 * check for the checkpointed data: `ls -la /opt/crac-files`
 * run the program without the checkpoint: `java -jar /opt/app/crac-0.0.1-SNAPSHOT.jar`. hit the endpoint 
 * run the program with the checkpoint: `java -XX:CRaCRestoreFrom=/opt/crac-files `

