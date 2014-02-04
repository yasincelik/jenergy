import os
import paramiko
import pprint
import time
import commands
import sys

def run(ssh, command):
    stdin, stdout, stderr = ssh.exec_command(command)
    pp.pprint(stdout.readlines())
    pp.pprint(stderr.readlines())
    return

def run_no_out(ssh, command):
    ssh.exec_command(command)
    return

print sys.argv
debug= False
if len(sys.argv)>2 and sys.argv[2] == 'debug':
    debug =True
    print 'Using the Debug version'

curDir = os.path.dirname(os.path.realpath(__file__))
print curDir
os.chdir(curDir)

pp = pprint.PrettyPrinter(indent=1)

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

confDir = '../conf/'

hostnames = [line.strip() for line in open(confDir + 'hosts')]

for hostname in hostnames:
    
    ssh.connect(hostname)

    print "Status for host: "+ hostname
    
    #status, output = commands.getstatusoutput("jps")
    #print "jps is in: "+output 
    #command = 'jps'
    #run(ssh, command)\
    if debug == True:
        command = 'ps x | grep java'
    else:
        command = 'jps'
    run(ssh, command)
    
ssh.close()