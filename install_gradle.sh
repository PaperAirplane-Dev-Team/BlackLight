#!/usr/bin/env bash
if [ ! -d "$HOME/gradle-2.2.1" ]; then
  wget http://services.gradle.org/distributions/gradle-2.2.1-all.zip;
  unzip gradle-2.2.1-all.zip >> /dev/null;
else
  echo 'Using cached directory.';
fi

export GRADLE_HOME=$HOME/gradle-2.2.1
export PATH=$GRADLE_HOME/bin:$PATH