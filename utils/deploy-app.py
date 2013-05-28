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
    print "Exiting deployment."
    exit()


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
        print "What should the version name be updated to? (format: #.#)"
        newVsnName = sys.stdin.read(4).strip("\n")
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
    print "This is where we will update Config.java to point to the super secret pack location."


def printGitHelp():
    print "\nYou now must\033[94m COMMIT, PUSH, AND TAG\033[0m these changes!\n"
    print "LUCAS LOOK THIS PROCESS UP AND DOCUMENT HERE"
    print "git commit"
    print "git push"
    print "git tag"
    print "git push --tags"
    print "\033[94m\nAfter you tag this release, rerun deploy-app.py to create your builds.\033[0m"


def buildApk(market):
    '''
    Echo and run the ant commands necessary to create a build for the provided
    market. Accepts "google" or "amazon" as parameter values.
    '''
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
        print "\033[94m" + "mkdir %s" % marketApkDir + "\033[0m"
        subprocess.check_call(["mkdir", marketApkDir])
        print "\033[94m" + "copy %s to %s" % (RELEASE_APK, marketApkDir) + "\033[0m"
        shutil.copy2(RELEASE_APK, marketApkDir + "buzzwords.apk")
    except:
        print "\nError copying %s APK to dropbox." % market
        print "If the folder for your apk version already exists, make sure you've updated versions or delete the existing folder."


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

'''
Primary script functionality. Two paths depending passed arguments, PREP or BUILD.
'''
if args.prep:
    print "Prepping codebase for final commit..."
    updateAndroidManifest()
    updateDatabaseVersion()
    printGitHelp()

elif args.build:
    print "Have you already committed, pushed, and tagged the release version? (y/n)"
    inputchar = sys.stdin.read(1)
    if inputchar != 'y':
        print "Before you can build releases, you must PREP (-p) the sourcecode for release."
        exitDeploy()

    pointToSecretPacks()

    # Get our keystore from dropbox if possible
    try:
        print "\033[94m" + "copy %s to ./keystore" % (DROPBOX + 'keystore') + "\033[0m"
        shutil.copy2(DROPBOX + 'keystore', 'keystore')
    except:
        print "Couldn't find keystore in Dropbox"
        print "Is this your dropbox directory %s ?" % DROPBOX
        print "If it isn't, open up this util and modify this code to point to your Dropbox (or make it modifiable)."

    # Create Dropbox directory for current version of our apk
    try:
        print "\033[94m" + "mkdir %s" % APK_VSN_DIR + "\033[0m"
        subprocess.check_call(["mkdir", APK_VSN_DIR])
    except:
        print "Error creating dropbox apk folder for current version."
        print "If the folder for your apk version already exists, make sure you've updated versions or delete the existing folder."

    # Parse configs and build new Amazon APK
    buildApk("amazon")

    # Parse configs and build new Google APK
    buildApk("google")

    # Clean up our temporary keystore
    try:
        print "\033[94m" + "rm keystore" + "\033[0m"
        subprocess.check_call(["rm", "keystore"])
    except:
        print "Error deleting temporary keystore."

else:
    parser.print_help()
