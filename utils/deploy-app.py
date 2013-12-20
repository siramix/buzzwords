'''
Deployment script for pre-release code changes and package building.

TODOS:
* The argument for --lite and --full is a silly way to do it. We should check the
   current branch and if it's not release-lite or release exit, otherwise just
   do the right thing automatically.
* Tagging process can be added to --prep
'''
import os
import sys
import shutil
import subprocess
import argparse
import fileinput

desc = "Deployment script for pre-release code changes and package building.  " \
       "To deploy, we first PREP for release, then commit to our repository, then BUILD releases."

parser = argparse.ArgumentParser(description=desc)
group = parser.add_mutually_exclusive_group()
group.add_argument("-p", "--prep", help="Run this FIRST before creating a build.", action='store_true')
group.add_argument("-b", "--build", help="Run this AFTER tagging and commiting the PREP'ed code", action='store_true')
litefull = parser.add_mutually_exclusive_group()
litefull.add_argument("-l", "--lite", help="For deploying release-lite", action='store_true')
litefull.add_argument("-f", "--full", help="For deploying release", action='store_true')
args = parser.parse_args()


def replaceAll(file, searchExp, replaceExp):
    '''
    Replace a given expression in a provided file.
    '''
    for line in fileinput.input(file, inplace=1):
        if searchExp in line:
            line = line.replace(searchExp, replaceExp)
        sys.stdout.write(line)


def exitDeploy():
    print "\033[31mExiting deployment.\033[0m"
    sys.exit(1)


def updateAndroidManifest():
    '''
    Check the AndroidManifest file versions and increment them if necessary.
    Basically this will do a find replace of the two versions that exist in
    the manifest that need updating each release.
    '''
    manifestFile = "AndroidManifest.xml"
    print "\n1. Tick up our AndroidManifest versions"
    try:
        for line in fileinput.input(manifestFile):
            if "android:versionCode" in line:
                curVsnCode = int(line.split("\"")[1])
                newVsnCode = curVsnCode + 1
            if "android:versionName" in line:
                curVsnName = line.split("\"")[1]

        # Prompt user to update Version Code
        inputchar = ''
        while inputchar != 'y' and inputchar != 'n':
            if inputchar != '\n':
                print "Increment android:versionCode from %d to %d? (y/n)" % (curVsnCode, newVsnCode)
            inputchar = sys.stdin.read(1)
        if (inputchar == 'n'):
            print "What should the version be set to?"
            newVsnCode = int(sys.stdin.read(3).strip("\n"))
        replaceAll(manifestFile, "android:versionCode=\"%d\"" % curVsnCode, "android:versionCode=\"%d\"" % newVsnCode)
        print "versionCode updated."

        # Prompt user to update Verision Name
        print "\nCurrent versionName is %s" % curVsnName
        print "What should the version name be updated to? (format: #.##)"
        newVsnName = float(sys.stdin.read(5).strip("\n"))
        replaceAll(manifestFile, "android:versionName=\"%s\"" % curVsnName, "android:versionName=\"%s\"" % newVsnName)
        print "versionName updated."

        # Confirm replacements
        print "\nUpdated %s with the following lines.\n" % manifestFile
        for line in fileinput.input(manifestFile):
            if "android:versionCode" in line:
                print line
            if "android:versionName" in line:
                print line
        fileinput.close()
    except IOError:
        print "\nError finding %s. Are you running script from the buzzwords root directory?" % manifestFile
        exitDeploy()


def updateDatabaseVersion():
    '''
    Ask the user if any database changes were made. If so, we must increment
    the database version in Consts.java.
    '''
    dbVersionFile = "src/com/buzzwords/Consts.java"
    print "\n2. Check database schema"
    inputchar = ''
    while inputchar != 'y' and inputchar != 'n':
        if (inputchar != '\n'):
            print "Were any database schema changes made? (y/n)"
        inputchar = sys.stdin.read(1)
    if inputchar == 'n':
        return
    try:
        for line in fileinput.input(dbVersionFile):
            if "DATABASE_VERSION" in line:
                curVsn = int(line.split("=")[1].strip().strip(';'))
                newVsn = curVsn + 1

        # Prompt user to update Database Version
        inputchar = ''
        while inputchar != 'y' and inputchar != 'n':
            if inputchar != '\n':
                print "Increment DATABASE_VERSION from %d to %d? (y/n)" % (curVsn, newVsn)
            inputchar = sys.stdin.read(1)
        if (inputchar == 'y'):
            replaceAll(dbVersionFile, "DATABASE_VERSION = %d" % curVsn, "DATABASE_VERSION = %d" % newVsn)
            print "DATABASE_VERSION updated."
        else:
            fileinput.close()
            print "You told me there were schema changes. You have to update the version if there were!"
            exitDeploy()

        # Confirm replacements
        print "\nUpdated %s with the following lines.\n" % dbVersionFile
        for line in fileinput.input(dbVersionFile):
            if "DATABASE_VERSION" in line:
                print line
    except IOError:
        print "\nError finding %s. Are you running from the buzzwords root directory?" % dbVersionFile
        exitDeploy()


def pointToSecretPacks():
    '''
    Ask the user what the secret pack directory is and insert it into the
    build properties file.
    '''
    secret_dir = raw_input("Enter the name of the production S3 bucket (ex. bw-packdata-test).\n")
    propfile = "build.properties"
    packUriTag = "config.packBaseUri"
    try:
        for line in fileinput.input(propfile):
            if packUriTag in line:
                cur_url = line.split('=')[1]
                new_url = "\"https://s3.amazonaws.com/siramix.buzzwords/%s/\"\n" % secret_dir
        replaceAll(propfile, "%s=%s" % (packUriTag, cur_url), "%s=%s" % (packUriTag, new_url))
        print "Pack URI updated."

    except IOError:
        print "\nError reading %s. Are you running from the buzzwords root directory?" % propfile
        exitDeploy()
    # Confirm replacements
    print "\nUpdated %s with the following lines.\n" % propfile
    for line in fileinput.input(propfile):
        if packUriTag in line:
            print line


def printGitHelp():
    print "\nBefore building, you must\033[94m COMMIT, PUSH, AND TAG\033[0m changes by --prep!\n"
    print "LUCAS LOOK THIS PROCESS UP AND DOCUMENT HERE"
    print "git commit"
    print "git push"
    print "git tag"
    print "git push --tags"
    print "\033[94m\nAfter you tag this release, rerun deploy-app.py to create your builds.\033[0m"

def printDirExistsWarning():
    print "\033[94mCareful, you are overriding the contents of this folder."
    print "The only reason for doing this is deploying both Lite and Full versions or testing builds.\033[0m\n"


def buildApk(market):
    '''
    Echo and run the ant commands necessary to create a build for the provided
    market. Accepts "google" or "amazon" as parameter values.
    '''
    print "\n\nBuilding apk for %s..." % market
    print "\033[94m" + "ant config-%s" % market + "\033[0m"
    subprocess.call(["ant", "config-%s" % market])
    print "\033[94m" + "ant release" + "\033[0m"
    subprocess.call(["ant", "release"])
    copyApk(market)


def copyApk(market):
    '''
    Copies the existing buzzords.apk file to the appropriate folder in dropbox
    depending on the provided market. Accept "google" or "amazon" as parameter values.
    '''
    marketApkDir = APK_VSN_DIR + market + "/"
    try:
        print "\033[94m" + "mkdir -p %s" % marketApkDir + "\033[0m"
        subprocess.check_call(["mkdir", "-p", marketApkDir])
    except:
        printDirExistsWarning()

    try:
        apkFind = subprocess.check_call(["find", marketApkDir + NEW_APK_NAME])
    except:
        apkFind = 1
        print "APK did not already exist. Beginning copy."
    if apkFind == 0:
        print "The APK already exists. You should have updated versions using --prep."
        print "Continue deploying to this location (BE SURE)? (y/n)"
        inputchar = sys.stdin.read(2)
        if inputchar != 'y\n':
            print "Make sure you've updated versions or delete the existing folder or apk."
            exitDeploy()
        else:
            sys.stdin.flush();

    try:
        print "\033[94m" + "copy %s to %s%s" % (RELEASE_APK, marketApkDir, NEW_APK_NAME) + "\033[0m"
        shutil.copy2(RELEASE_APK, marketApkDir + NEW_APK_NAME)
    except:
        print "\nError copying %s APK to dropbox." % market

'''
Global variables
'''
manifestFile = "AndroidManifest.xml"
for line in fileinput.input(manifestFile):
    if "android:versionName" in line:
        curVsnName = line.split("\"")[1]

DROPBOX = os.environ['HOME'] + "/Dropbox/Siramix/"
APK_VSN_DIR = DROPBOX + "apps/buzzwords_apks/Buzzwords-%s/" % curVsnName
RELEASE_APK = "bin/Buzzwords-release.apk"
NEW_APK_NAME = "buzzwords.apk"

'''
Primary script functionality. Two paths depending passed arguments, PREP or BUILD.
'''
if (not args.lite and not args.full) or (not args.build and not args.prep):
    print "\033[94mYou must supply --full or --lite AND --prep or --build.\033[0m"
    print "\033[94mSee launch_process.txt\033[0m"
    parser.print_help()
    exitDeploy()

if args.lite:
    NEW_APK_NAME = "buzzwordslite.apk"
elif args.full:
    NEW_APK_NAME = "buzzwords.apk"

if args.prep:
    print "Prepping codebase for final commit..."
    updateAndroidManifest()
    updateDatabaseVersion()
    printGitHelp()

elif args.build:
    inputchar = raw_input("Have you already committed, pushed, and tagged the release (prepped) version? (y/n)\n")
    if inputchar != 'y':
        printGitHelp()
        exitDeploy()
    else:
        sys.stdin.flush();
    pointToSecretPacks()

    # Get our keystore from dropbox if possible
    try:
        print "\033[94m" + "copy %s to ./keystore" % (DROPBOX + 'keystore') + "\033[0m"
        shutil.copy2(DROPBOX + 'keystore', 'keystore')
    except:
        print "Couldn't find keystore in Dropbox"
        print "Is this your dropbox directory %s ?" % DROPBOX
        print "If it isn't, open up this util and modify this code to point to your Dropbox (or make it modifiable)."

    # Parse configs and build new Amazon APK
    buildApk("amazon")

    # Parse configs and build new Google APK
    buildApk("google")

    # Parse configs and build new Samsung APK
    buildApk("samsung")

    # Clean up our temporary keystore
    try:
        print "\033[94m" + "rm keystore" + "\033[0m"
        subprocess.check_call(["rm", "keystore"])
    except:
        print "Error deleting temporary keystore."
        exitDeploy()
    print "\033[32m\nDeployment Complete!\033[0m"
    sys.exit(0)
else:
    parser.print_help()
