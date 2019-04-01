FROM centos:7
MAINTAINER Vasek Pavlin <vasek@redhat.com>

VOLUME ["/target"]
CMD ["/usr/bin/bash"]

ENV LANG=en_US.utf8
ENV USER_NAME forge

RUN yum -y -q install git java java-devel which &&\
    yum clean all

ENV JAVA_HOME /usr/lib/jvm/java-openjdk

RUN useradd --user-group --create-home --shell /bin/false ${USER_NAME}

ENV HOME /home/${USER_NAME}

WORKDIR ${HOME}

COPY . ./

RUN chown -R ${USER_NAME}:${USER_NAME} ./*

USER ${USER_NAME}

