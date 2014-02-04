import os
import paramiko
import pprint
import sys
import time
import ConfigParser

def run(ssh, command):
    stdin, stdout, stderr = ssh.exec_command(command)
    #pp.pprint(stdout.readlines())
    pp.pprint(stderr.readlines())
    return

def run_no_out(ssh, command):
    stdin, stdout, stderr = ssh.exec_command(command)
    return

def runUgly(host,command):
    print 'ssh '+host+' \''+ command+' &\''+'&'
    #os.system('ssh '+host+' \''+ command+'&\''+'&')

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



curDir = os.path.dirname(os.path.realpath(__file__))
print curDir
os.chdir(curDir)

pp = pprint.PrettyPrinter(indent=4)

args = sys.argv

confDir = '../conf/'

hostnames = [line.strip() for line in open(confDir + 'hosts')]

numhosts = len(hostnames)

numHrequested= numhosts

if len(args) > 2:
    numHrequested = args[2] 

hostnames = hostnames[:int(numHrequested)]

print hostnames
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
jenergyFolder = ConfigSectionMap("SectionJenergy")['jenergyfolder']
print 'Using the folder: '+ jenergyFolder
sleepTime = 3
for hostname in hostnames:
    ssh.connect(hostname)
    print "Starting the ComputeSpace on: "+ hostname
    toformat = 'false'
    if hostnames.index(hostname)==0:
        toformat = 'true'
        
    command =jenergyFolder+'/bin/startLocalCS.sh '+hostname+' '+toformat
    print command
    run_no_out(ssh, command)
    #runUgly(hostname,command)
    
    print 'startCS: waiting '+str(sleepTime)+'s to start the next CS node'   
    time.sleep(sleepTime)
    
    if hostnames.index(hostname)>=5:
        sleepTime = 3
        
time.sleep(5)
ssh.close()


