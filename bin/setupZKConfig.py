import os
import sys

first='false'
if sys.argv[2]=='true':
    first='true'
    
    
currenthost=sys.argv[1]; 

hosts = [line.strip() for line in open('conf/hosts')]
       


if first=='true':    
    print 'setzk config: setting up global state for ensemble'

     #adding the ensemble ips and ids to the main conf file
    os.system("cp ../conf/zoo.cfg conf/zoo.cfg")
    f = open ('conf/zoo.cfg','a')
    portquorum = 2888
    portleader = 3888
    id = 1
    for host in hosts:
        line = "\nserver."+str(id)+"="+host+":"+str(portquorum)+":"+str(portleader)
        id = id+1
        portquorum = portquorum+1
        portleader=portleader+1
 #       print line
        f.write(line)
    f.close()
    
    

    #setting up the individual config files
    portincrement = 0
    oldlines = [line.strip() for line in open('conf/zoo.cfg')]
    #print oldlines
    
    
    for host in hosts:
  #      print "\n\n"
   #     print "creating conf for "+ host
        parsedlines=[]
        line=""
        for line in oldlines:
            parsedlines.append(line.split('='))
            
        #print parsedlines
        
        f = open('conf/zoo.'+host+'.cfg','w')
        for line in parsedlines:
            #print line
            if line[0]=='dataDir':
                line[1]=line[1]+'.'+host
            
            if line[0]=='clientPort':
                line[1]=int(line[1])+portincrement
    #        print line
            
            if len(line)>1:
     #           print len(line)
                newline = str(line[0])+'='+str(line[1])
      #          print newline
                f.write("\n"+newline)
        f.close()
        
        portincrement= portincrement+1
    

#os.system("sed -i \'s/seedsTemplate/"+seeds+"/g\' conf/cassandra.yaml | grep seeds")

    
#id = 1
#creating the id files
#os.system('rm -r data/zk*')

#for host in hosts:
#    os.system('mkdir data/zk.'+host)
#    os.system('echo '+str(id)+' >> data/zk.'+host+'/myid')
#    id = id +1

if first=='false':
    print 'setzk config: Not setting up the global state, only affecting my local conf files'




#every node does this
#find out my id from the hostfile and create my folder.
#print hosts
#print currenthost
id = hosts.index(currenthost)+1
#id = hosts.index("login2")+1
#print 'my id: '+str(id)
os.system('mkdir data/zk.'+currenthost+' 2>/dev/null')
#print 'echo '+str(id)+' > tee data/zk.'+currenthost+'/myid'
os.system('echo '+str(id)+' > data/zk.'+currenthost+'/myid')

