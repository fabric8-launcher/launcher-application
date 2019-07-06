#!/bin/bash

#
# This script should be run in the root of a Booster project.
# It will then create the necessary template files for the
# launch application.
# Read the script's output carefully!
#

main() {
    # Step 1 - Run mvn to create the fabric8 OpenSHift temapltes
    echo "Running 'mvn fabric8:resource -Dfabric8.openshift.trimImageInContainerSpec=false  -Popenshift' to create application templates..."
    mvn > /dev/null fabric8:resource -Dfabric8.openshift.trimImageInContainerSpec=false -Popenshift || { echo "Maven command failed. Try running 'mvn fabric8:resource -Dfabric8.openshift.trimImageInContainerSpec=false -Popenshift' yourself to see what went wrong." ; exit 1; }

    # Step 2 - Find the templates
    TEMPLATES=$(find . -wholename '*/fabric8/openshift.yml')

    for tpl in $TEMPLATES
    do
        # Step 3 - Determine project root from template path
        PRJDIR=$(dirname $(dirname $(dirname $(dirname $(dirname $tpl)))))

        # Step 4 - Create an ".openshiftio" folder in each project root
        OSIODIR=$PRJDIR/.openshiftio
        mkdir -p $OSIODIR

        # Step 5 - Find the project's name in the fabric8 template
        APPPRJNAME=$(findProjectName $tpl)
        IMAGENAME=$(findImageName $tpl)
        echo "IMAGE $IMAGENAME"

        # Step 6 - Create the first part of the template
        APPTPL=$OSIODIR/application.yaml
        createTemplate $APPTPL $APPPRJNAME $IMAGENAME
        
        # Step 7 - Append the fabric8 template
        appendTemplate $tpl $APPTPL

        echo "Created template '$APPTPL' for project '$APPPRJNAME'"

        # If Jenkinsfile exists we'll check it for mroe things to convert
        JENKINSFILE=$PRJDIR/Jenkinsfile
        if [ -e $JENKINSFILE ]
        then
            # Step 8 - Check if Jenkinsfile has an "oc new-app" command
            OCCMD=$(findOcNewAppCommand $JENKINSFILE $APPPRJNAME)
            if [[ ! -z $OCCMD ]]
            then
                SRVTPL=$OSIODIR/service.yaml
                echo "A command to create a support service has been found,"
                echo "will now run the following to try to create a template for it:"
                echo "    $OCCMD > $SRVTPL"
                $OCCMD > $SRVTPL
            fi

            # Step 9 - Check if Jenkinsfile has an "oc create configmap" command
            OCCMD=$(findOcCreateConfigmapCommand $JENKINSFILE)
            if [[ ! -z $OCCMD ]]
            then
                CFGMAPTPL=$OSIODIR/resource.configmap.yaml
                echo "A command to create a config map has been found,"
                echo "will now run the following to try to create a template for it:"
                echo "    $OCCMD > $CFGMAPTPL"
                $OCCMD > $CFGMAPTPL
                echo ""
                echo "The ConfigMap has been created in the file: '$CFGMAPTPL'."
                echo "Edit it and add the following lines to any toplevel 'metadata:' sections:"
                echo "(the lines should be indented with respect to each 'metadata:' section)"
                echo ""
                echo "    labels:"
                echo "      booster: $APPPRJNAME"
            fi

            # Step 10 - Check if Jenkinsfile has an "oc policy" command
            POLICY=$(findOcPolicy $JENKINSFILE)
            if [[ ! -z $POLICY ]]
            then
                CFGMAPTPL=$OSIODIR/resource.roles.yaml
                echo "A command to create user roles has been found,"
                echo "will create a template for it."
                echo "$POLICY" > $CFGMAPTPL
            fi
        fi
    done

    echo ""
    echo "The following changes have been made to the project:"

    git status

    echo ""
    echo "You should only commit files that are related to the application,"
    echo "discard any changes that might have been made to tests for example."
}

findOcNewAppCommand() {
    export PRJNAME=$2
    perl - $1 <<-'__EOF__'
    use strict;
    use warnings;
    my $prjname=$ENV{"PRJNAME"};
    while (my $line = <>) {
        chomp $line;
        if ($line =~ /(oc\s+new-app\s+.+?)[;"]/) {
            print "$1 --dry-run -o yaml -l booster=$prjname"
        }
    }
__EOF__
}

findOcCreateConfigmapCommand() {
    perl - $1 <<-'__EOF__'
    use strict;
    use warnings;
    while (my $line = <>) {
        chomp $line;
        if ($line =~ /(oc\s+create\s+configmap\s+.+?)[;"]/) {
            print "$1 --dry-run -o yaml"
        }
    }
__EOF__
}

findOcPolicy() {
    export PRJNAME=$2
    perl - $1 <<-'__EOF__'
    use strict;
    use warnings;
    my $prjname=$ENV{"PRJNAME"};
    while (my $line = <>) {
        chomp $line;
        if ($line =~ /oc\s+policy\s+add-role-to-user\s+(.+?)\s+-z\s+(.+?)"/) {
            print "apiVersion: v1
kind: List
items:
- apiVersion: v1
  kind: RoleBinding
  metadata:
    name: role-view-default
    labels:
      booster: ${prjname}
  subjects:
  - kind: ServiceAccount
    name: $2
  roleRef:
    name: $1
"
        }
    }
__EOF__
}

appendTemplate() {
    perl < $1 >> $2 -e '
    use strict;
    use warnings;
    my $start=0;
    while (my $line = <>) {
        if ($start == 1) {
            if ($line !~/fabric8\.io\/git-commit/) {
                print $line;
            }
        }
        if ($line =~ /items:/) {
            $start=1;
        }
    }'
}

findProjectName() {
    perl - $1 <<-'__EOF__'
    use strict;
    use warnings;
    my $prjname='';
    my $selindent=-1;
    while (my $line = <>) {
        chomp $line;
        if ($selindent >= 0) {
            if ($line =~ /^(\s*)\w/) {
                my $indent=length($1);
                if ($indent <= $selindent) {
                    $selindent=-1;
                }
            }
            if ($line =~ /\s*(app:|project:)\s*(\S+)/) {
                $prjname=$2;
            }
        }
        if ($line =~ /^(\s*)selector:/) {
            $selindent=length($1);
        }
    }
    print $prjname;
__EOF__
}

findImageName() {
    perl - $1 <<-'__EOF__'
    use strict;
    use warnings;
    my $imgname='';
    my $selindent=-1;
    while (my $line = <>) {
        chomp $line;
        if ($selindent >= 0) {
            if ($line =~ /^([-\s]*)\w/) {
                my $indent=length($1);
                if ($indent <= $selindent) {
                    $selindent=-1;
                }
            }
            if ($line =~ /[-\s]*image:\s*(\S+)/) {
                $imgname=$1;
            }
        }
        if ($line =~ /^([-\s]*)containers:/) {
            $selindent=length($1);
        }
    }
    print $imgname;
__EOF__
}

createTemplate() {
    cat > $1 <<'__EOF__'
apiVersion: v1
kind: Template
metadata:
  name: launcher-builder
  annotations:
    description: This template creates a Build Configuration using an S2I builder.
    tags: instant-app
parameters:
- name: SOURCE_REPOSITORY_URL
  description: The source URL for the application
  displayName: Source URL
  required: true
- name: SOURCE_REPOSITORY_REF
  description: The branch name for the application
  displayName: Source Branch
  value: master
  required: true
- name: SOURCE_REPOSITORY_DIR
  description: The location within the source repo of the application
  displayName: Source Directory
  value: .
  required: true
- name: GITHUB_WEBHOOK_SECRET
  description: A secret string used to configure the GitHub webhook.
  displayName: GitHub Webhook Secret
  required: true
  from: '[a-zA-Z0-9]{40}'
  generate: expression
objects:
- apiVersion: v1
  kind: ImageStream
  metadata:
    name: ${SRVNAME}
    labels:
      booster: ${SRVNAME}
  spec: {}
- apiVersion: v1
  kind: ImageStream
  metadata:
    name: runtime-${SRVNAME}
    labels:
      booster: ${SRVNAME}
  spec:
    tags:
    - name: latest
      from:
        kind: DockerImage
        name: registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift
- apiVersion: v1
  kind: BuildConfig
  metadata:
    name: ${SRVNAME}
    labels:
      booster: ${SRVNAME}
  spec:
    output:
      to:
        kind: ImageStreamTag
        name: ${IMAGENAME}
    postCommit: {}
    resources: {}
    source:
      git:
        uri: ${SOURCE_REPOSITORY_URL}
        ref: ${SOURCE_REPOSITORY_REF}
      #contextDir: ${SOURCE_REPOSITORY_DIR}
      type: Git
    strategy:
      sourceStrategy:
        from:
          kind: ImageStreamTag
          name: runtime-${SRVNAME}:latest
        incremental: true
        env:
        - name: MAVEN_ARGS_APPEND
          value: "-pl ${SOURCE_REPOSITORY_DIR}"
        - name: ARTIFACT_DIR
          value: "${SOURCE_REPOSITORY_DIR}/target"
      type: Source
    triggers:
    - github:
        secret: ${GITHUB_WEBHOOK_SECRET}
      type: GitHub
    - type: ConfigChange
    - imageChange: {}
      type: ImageChange
  status:
    lastVersion: 0
__EOF__
    if [[ ! -z $2 ]]
    then
        perl -i -pe "s/\\\$\{SRVNAME}/$2/g" $1
    fi
    if [[ ! -z $3 ]]
    then
        IMG=${3//\//\\\/}
        perl -i -pe "s/\\\$\{IMAGENAME}/$IMG/g" $1
    fi
}

main"$@"

