#!/bin/bash

#
# This script can be used to generate the unified template for the entire Launch application
#
# Just run it and pipe its output to "openshift/launch-template.yaml".
#

main() {
    DIR=$(cd "$(dirname "$0")" ; pwd -P)
    cat $DIR/../openshift/split/launcher-header.yaml
    echo "parameters:"
    printTemplateParams $DIR/../openshift/split/launcher-configmaps.yaml
    printTemplateParams $DIR/../openshift/split/launcher-deployment.yaml
    printTemplateParams $DIR/../openshift/split/launcher-routes.yaml
    printTemplateParams $DIR/../openshift/split/launcher-secrets.yaml
    echo "objects:"
    printTemplateObjects $DIR/../openshift/split/launcher-configmaps.yaml
    printTemplateObjects $DIR/../openshift/split/launcher-deployment.yaml
    printTemplateObjects $DIR/../openshift/split/launcher-routes.yaml
    printTemplateObjects $DIR/../openshift/split/launcher-secrets.yaml
}

printTemplateParams() {
    perl < $1 -e '
    use strict;
    use warnings;
    my $start=0;
    while (my $line = <>) {
        if ($start == 1) {
            if ($line =~ /^[\s-]/) {
                print $line;
            } else {
                exit;
            }
        }
        if ($line =~ /parameters:/) {
            $start=1;
        }
    }'
}

printTemplateObjects() {
    perl < $1 -e '
    use strict;
    use warnings;
    my $start=0;
    while (my $line = <>) {
        if ($start == 1) {
            if ($line =~ /^[\s-]/) {
                print $line;
            } else {
                exit;
            }
        }
        if ($line =~ /objects:/) {
            $start=1;
        }
    }'
}

main"$@"

