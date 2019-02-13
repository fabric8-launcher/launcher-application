FROM registry.centos.org/jboss/base-jdk:8
MAINTAINER Vasek Pavlin <vasek@redhat.com>

EXPOSE 8080
EXPOSE 8443

ENV LANG=en_US.UTF-8

USER root

RUN chgrp -R 0 /opt/jboss &&\
    chmod -R g+rw /opt/jboss &&\
    find /opt/jboss -type d -exec chmod g+x {} + &&\
    yum -y -q install git &&\
    yum clean all &&\
    rm -rf /var/cache/yum &&\
    git config --system user.name redhat-developers-launcher &&\
    git config --system user.email 45641108+redhat-developers-launcher@users.noreply.github.com

USER jboss

COPY target/launcher-backend-runner.jar ./
COPY target/lib/ ./lib/

CMD ["sh", "-c", "java -Djava.net.preferIPv4Stack=true \
                        -XX:+UnlockExperimentalVMOptions \
                        -XX:+UseCGroupMemoryLimitForHeap \
                        -XX:MaxRAMFraction=1 \
                        -XX:MetaspaceSize=96M \
                        -XX:MaxMetaspaceSize=512m \
                        -XX:AdaptiveSizePolicyWeight=90 \
                        -XX:+ExitOnOutOfMemoryError \
                        -XX:+HeapDumpOnOutOfMemoryError \
                        -XX:+UseParallelGC \
                        -XX:MinHeapFreeRatio=20 \
                        -XX:MaxHeapFreeRatio=40 \
                        -XX:CICompilerCount=2 \
                        -XX:ParallelGCThreads=1 \
                        -XX:ConcGCThreads=1 \
                        -XX:GCTimeRatio=4 \
                        -XshowSettings:vm \
                        $JAVA_OPTS \
                        -jar launcher-backend-runner.jar"]