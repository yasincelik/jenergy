import os
import paramiko
import pprint
import threading
import ConfigParser

def run(ssh, command):
    stdin, stdout, stderr = ssh.exec_command(command)
    pp.pprint(stdout.readlines())
    pp.pprint(stderr.readlines())
    return

def stopall(hostname,jenergyFolder):
    print hostname
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    print "Stopping the ComputeSpace on: "+ hostname
    ssh.connect(hostname)

    
    command = jenergyFolder+'/bin/stopAll.sh'
    run(ssh, command)
    print 'Stopping CS Done : '+ hostname
    ssh.close()


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

confDir = '../conf/'

hostnames = [line.strip() for line in open(confDir + 'hosts')]

jenergyFolder = ConfigSectionMap("SectionJenergy")['jenergyfolder']
print 'Using the folder: '+ jenergyFolder

hostsThreads=[]
for hostname in hostnames:
    print hostname
    thread = threading.Thread(target=stopall, args=(hostname,jenergyFolder))
    thread.start()
    hostsThreads.append(thread)
    
        
        
for thread in hostsThreads:
    thread.join() 


