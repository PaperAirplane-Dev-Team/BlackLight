#!/usr/bin/env bash
if [ ! -d "$HOME/gradle-2.2.1" ]; then
  pushd $HOME;
  wget http://services.gradle.org/distributions/gradle-2.2.1-all.zip;
  unzip gradle-2.2.1-all.zip >> /dev/null;
  popd;
else
  echo 'Using cached directory.';
fi

export GRADLE_HOME=$HOME/gradle-2.2.1
export PATH=$GRADLE_HOME/bin:$PATH