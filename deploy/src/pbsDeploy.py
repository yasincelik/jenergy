'''
Created on Jun 5, 2013

@author: moutai
'''
import os
import paramiko
import pprint
import threading
import sys
import ConfigParser


def run(command):
    os.system(command)
    return

def ConfigSectionMap(section):
    dict1 = {}
    Config = ConfigParser.ConfigParser()
    Config.read("../conf/config")
    options = Config.options(section)
    for option in options:
        try:
            dict1[option] = Config.get(section, option)
            #print dict1[option]
            if dict1[option] == -1:
                DebugPrint("skip: %s" % option)
        except:
            print("exception on %s!" % option)
            dict1[option] = None
    return dict1

    

def deploy(hostnames):
    hostname = hostnames[0]
    print 'host:'+str(hostname)+': deploying to '+hostname
    jenergyFolder = ConfigSectionMap("SectionJenergy")['jenergyfolder']
    print 'Using the folder: '+ jenergyFolder
    
    #os.system(jenergyFolder+"/bin/setup.sh")
    
    confDir="../conf/"
    
    os.system("rsync -avz " + confDir + "/hosts "+jenergyFolder+"/conf/hosts")
    #print 'host: installing software dependencies'
    #command = " java -version"
    #print command
    #run(command)

curDir = os.path.dirname(os.path.realpath(__file__))
#print curDir
os.chdir(curDir)

pp = pprint.PrettyPrinter(indent=4)

confDir = '../conf/'
#print os.listdir('./')

#os.system("setnodes.sh")

hostnames = [line.strip() for line in open(confDir + 'hosts')]

print hostnames
#os.system("tar cvzf jenergy.tar.gz --exclude='data' --exclude='.git' /home/moutai/jenergy")


deploy(hostnames)
sys.exit()




