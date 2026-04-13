FROM jenkins/jenkins:lts

USER root

RUN apt-get update && apt-get install -y \
    make \
    build-essential \
    cmake \
    pkg-config \
    libcriterion-dev \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

USER jenkins

ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

COPY bonus/myMarvinBonus.yml /var/jenkins_home/my_marvin.yml
COPY bonus/privateJob_dsl.groovy /var/jenkins_home/privateJob_dsl.groovy

ENV CASC_JENKINS_CONFIG=/var/jenkins_home/my_marvin.yml