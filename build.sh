#!/usr/bin/env bash

if [ -z $WORK_DIR ]; then
    WORK_DIR="app/"
fi

if [ "$SKIP_TESTS" = true ]; then
    tests="-DskipTests"
fi

if [ -f "/usr/share/maven/ref/settings-docker.xml" ]; then
    docker_settings="-s /usr/share/maven/ref/settings-docker.xml"
fi

mvn -P dockerbuild \
    $docker_settings \
    -pl "!orienteer-archetype-war, !orienteer-archetype-jar, !orienteer-standalone,
    !orienteer-birt, !orienteer-bpm, !orienteer-camel, !orienteer-etl, !orienteer-taucharts,
    !orienteer-architect, !orienteer-mail, !orienteer-users, !orienteer-object" \
    clean install $tests

mkdir -p $WORK_DIR

cp target/jetty-runner.jar                         $WORK_DIR/
cp orienteer-war/target/orienteer.war              $WORK_DIR/
cp orienteer.properties                            $WORK_DIR/orienteer.properties
cp -r orienteer-core/config                        $WORK_DIR/config