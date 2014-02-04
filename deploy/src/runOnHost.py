import os
import paramiko
import pprint
import time
import sys

def run(ssh, command):
    stdin, stdout, stderr = ssh.exec_command(command)
    pp.pprint(stdout.readlines())
    pp.pprint(stderr.readlines())
    return

def run_no_out(ssh, command):
    ssh.exec_command(command)
    return


curDir = os.path.dirname(os.path.realpath(__file__))
print curDir
os.chdir(curDir)

pp = pprint.PrettyPrinter(indent=1)


ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

confDir = '../conf/'

hostnames = [line.strip() for line in open(confDir + 'hosts')]

print 'Number of arguments:', len(sys.argv), 'arguments.'

if len(sys.argv) < 4: 
    print "usage: distTool run hostnumber command"
    sys.exit()

host = sys.argv[2]

commandline = sys.argv

del commandline[0]
del commandline[0]
del commandline[0]

print commandline
cmd = ' '.join(commandline)

if host == 'all':
    for hostname in hostnames:  
        print "running command on : "+ hostname+":"+ str(hostnames.index(hostname))
              
        ssh.connect(hostname)
        run(ssh, cmd)
        ssh.close()

elif len(hostnames) < int(host): 
    print len(hostnames)
    print "host does not exist:" + host
else :
    print "Running command: " + cmd
    print "hostnames total: " + str(len(hostnames))
    print "host number: "+ host
    
    hostname = hostnames[int(host) - 1]
    print "running command on : "+ hostname
    
    ssh.connect(hostname)
    run(ssh, cmd)
    














































