SCRIPT_DIR=`dirname $0`
java -Xmx1024M -Dsbt.override.build.repos=true -Dfile.encoding=UTF-8 -XX:+CMSClassUnloadingEnabled -jar "$SCRIPT_DIR/project/sbt-launch.jar" $@
