from optparse import OptionParser
import argparse
import datetime
import os
import paramiko
import pprint
import select
import sys
import threading
import time



def run(ssh, command):
    print command
    stdin, stdout, stderr = ssh.exec_command(command)
    pp = pprint.PrettyPrinter(indent=1)
    pp.pprint(stderr.readlines())
    pp.pprint(stdout.readlines())
   
    return

def run_no_out(ssh, command):
    ssh.exec_command(command)
    
    return
def runUgly(host,command):
    #print 'ssh '+host+' '+ command
    os.system('ssh '+host+' \''+ command+'&\''+'&')
    
def run_master(host, options,x):
        print "Starting a master on : " + host    
        cmd = options.masterscript
        ssh = paramiko.SSHClient()
        ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        ssh.connect(host)
        print "\n\n\n\n\n\n\n RUNNING a master\n\n\n\n\n\n\n\n\n"
        cmd= cmd+" > /tmp/masteroutput 2>&1" 
        print cmd
        run_no_out(ssh, cmd)
        #os.system('ssh '+host+' '+ cmd+'&')
        ssh.close()
        if options.output and x==0:
            os.system("tail -f /tmp/masteroutput")
        
def setRecover(host, recovery, options, ssh):
        # set the recovery status
        pathbin = options.workerscript
        pathdir = pathbin.split("/")
        pathdir.pop()
        pathdir = '/'.join(pathdir)
        cmd = pathdir + "/setRecoveryStatus.sh " + recovery
        ssh.connect(host)
        run_no_out(ssh, cmd)
        ssh.close()



parser = argparse.ArgumentParser()

parser.add_argument("-m", "--master", dest="masterscript",
                  help="set the master script path with options", nargs='+', metavar="FILE ")


parser.add_argument("-w", "--worker", dest="workerscript",
                  help="set the worker script path", nargs='+', metavar="FILE")

parser.add_argument("-p", "--numworkers", dest="numWorkers",
                  help="set the number of workers", metavar="num")

parser.add_argument("-q", "--nummasters", dest="numMasters",
                  help="set the number of masters for the applications(experimental)", metavar="num")

parser.add_argument("--output", help="should the master return output on the current shell", action="store_true")


#args1 = ["-m", "jenergy/bin/runMaster.sh 1000 10", "-w", "jenergy/bin/runWorker.sh", "-p", "10", "-q", "2"]

options = parser.parse_args(sys.argv[2:])



options.masterscript = ' '.join(options.masterscript)
print "Master script: " + options.masterscript;

options.workerscript = ' '.join(options.workerscript)
print "Worker script: " + options.workerscript;

print "Number of Workers: " + options.numWorkers;
print "Number of Masters: " + options.numMasters;

if (options.masterscript == None) or (options.workerscript == None) or (options.numWorkers == None) or (options.numMasters == None):
    parser.print_help()
    sys.exit()


curDir = os.path.dirname(os.path.realpath(__file__))
print curDir
os.chdir(curDir)

pp = pprint.PrettyPrinter(indent=1)


ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

confDir = '../conf/'

hostnames = [line.strip() for line in open(confDir + 'hosts')]


# requires to have startcs already run
# running the master(s) in round robin fashion
for x in range(0, int(options.numWorkers)):
    
    host = hostnames[x % len(hostnames)]
    
    print "\n\n\n\n\n\n\n\n\n\n\n\***************************\nstarting a worker on : " + host    
    cmd = options.workerscript
    print cmd
    
    ssh.connect(host)
    run_no_out(ssh, cmd)
    ssh.close()
    
    
masters = []
for x in range(0, int(options.numMasters)):
        host = hostnames[x % len(hostnames)]
        if x == 0:
            # starting the first master 
            setRecover(host, "false", options, ssh)
        elif x == 1:
            # after the first master just setting the recovery flag once after waiting a couple seconds
            time.sleep(20); 
            setRecover(host, "true", options, ssh)
        else:
            time.sleep(20); 
            
        thread = threading.Thread(target=run_master, args=(host, options,x))
        thread.start()
        masters.append(thread)
        
         
for thread in masters:
    thread.join()     
    
#ssh.close()


