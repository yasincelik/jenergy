import os
lines = [line.strip() for line in open('conf/hosts')]
outstart = "\""
outend = "\""
seeds = outstart
myString = ",".join(lines)
seeds = myString
print seeds
#print "sed \'s/seedsTemplate/"+seeds+"/g\' conf/cassandra.yaml | grep seeds"
os.system("sed -i \'s/seedsTemplate/"+seeds+"/g\' conf/cassandra.yaml | grep seeds")
#os.system("sed \'s/seedsTemplate/"+seeds+"/g\' ../conf/cassandra.yaml.template | grep seeds")


#adding the listening ip of zookeeper
#lines = [line.strip() for line in open('conf/hosts')]

#myString = "clientPortAddress="+lines[0]
#print myString

#with open("conf/zoo.cfg", "a") as myfile:
#    myfile.write("\n"+myString)
#    myfile.close()
    







