mvn clean -f ../pom.xml;
mvn package assembly:single -DskipTests -f ../pom.xml

#copy the jar and the conf files to have them in the same folder as the jar

#cp ../target/Jenergy-*-jar-with-dependencies.jar ./Jenergy.jar
#cp -r ../conf ./conf
#python setupSeeds.py

