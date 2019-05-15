#!/bin/bash

CONFIG_TEMPLATE="/usr/bin/config-tmpl.json"
INDEX="/usr/share/nginx/html/index.html"

export WELCOME_APP_CONFIG_ENCODED=$(echo ${WELCOME_APP_CONFIG} | sed 's/\\/\\\\/g' | sed 's/\"/\\\"/g' | sed s/\'/\\\'/g)

# Encode quotes and remove line breaks
ENCODED_CONFIG=$(envsubst < ${CONFIG_TEMPLATE} | sed -e 's/[]\/$*.^[]/\\&/g' | sed ':a;N;$!ba;s/\n/ /g')
# create injected index.html with json settings
sed -i -e "s/<script id=\"script-injection\"><\/script>/<script id=\"injected-script\">$(echo ${ENCODED_CONFIG})<\/script>/g" ${INDEX}
echo -------------------------------------
cat ${INDEX}

envsubst < /tmp/nginx.conf > /etc/nginx/nginx.conf
exec /run.sh
