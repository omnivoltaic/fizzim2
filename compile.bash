#! /usr/bin/bash
#rm -f .*.sw* && \
ls *.java > /dev/null && \
version=`perl -n -e 'if (m/currVer\s*=\s*"([\d\.]+)"/) {print "$1";}' *.java` && \
echo "Creating ${version}_jar directory" && \
if [ -d ${version}_jar ]; then
  rm -fr ${version}_jar
fi && \
mkdir ${version}_jar && \
cd ${version}_jar && \
echo "Copying java files to ${version}_jar directory" && \
cp -pr ../*.java ../org ../*.png ../compile.bash ./ && \
echo "Running javac " && \
javac -target 1.5 *.java && \
echo "Main-Class: FizzimGui" > manifest.txt && \
echo "Creating jar file " && \
mkdir src && \
mv *.java src/ && \
jar cvfm fizzim_v${version}.jar manifest.txt *.class splash.png icon.png org/ src/ compile.bash > jar.log && \
echo "Copying jar file back to main directory" && \
cp fizzim*.jar ../ && \
echo done


