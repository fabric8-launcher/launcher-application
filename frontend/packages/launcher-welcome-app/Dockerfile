# This image is used to deploy
FROM registry.centos.org/kbsingh/openshift-nginx:latest
MAINTAINER Vasek Pavlin <vasek@redhat.com>

ENV LANG=en_US.utf8
ENTRYPOINT /usr/bin/entrypoint.sh

USER root

RUN yum install -y gettext
ADD nginx.conf /tmp/nginx.conf

COPY scripts/entrypoint.sh /usr/bin/entrypoint.sh
COPY scripts/config-tmpl.json /usr/bin/config-tmpl.json
RUN chmod +x /usr/bin/entrypoint.sh

RUN rm -f /usr/share/nginx/html/*

COPY build/ /usr/share/nginx/html/
RUN chmod 777 -R /var/lib/nginx /usr/share/nginx/html/
RUN chmod 777 /etc/nginx/nginx.conf

USER 997
