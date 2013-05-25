import sys
import os
import argparse
import fileinput

desc = "Deployment script for pre-release code changes and package building.  " \
       "To deploy, we first PREP for release, then commit to our repository, then BUILD releases."

parser = argparse.ArgumentParser(description=desc)
group = parser.add_mutually_exclusive_group()
group.add_argument("-p", "--prep", help="Run this FIRST before creating a build.", action='store_true')
group.add_argument("-b", "--build", dest='market', help="Build for which market (AMAZON or GOOGLE)")
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


def printGitHelp():
    print "\nYou now must\033[91m COMMIT, PUSH, AND TAG\033[0m these changes!\n"
    print "LUCAS LOOK THIS PROCESS UP AND DOCUMENT HERE"
    print "git commit"
    print "git push"
    print "git tag"
    print "git push --tags"
    print "\033[91m\nAfter you tag this release, rerun deploy.py and create your builds.\033[0m"


def main():
    print "Available Deploy Tasks:"
    print "1. Increment versions before committing to repository"
    print "2. Create a build for Amazon or Google"
    inputchar = ''
    while inputchar != '1' and inputchar != '2':
        if inputchar != '\n':
            print "\nEnter 1 or 2:"
        inputchar = sys.stdin.read(1)

    if inputchar == '1':
        print "Incrementing versions..."
        # Tick up our versions.
        updateAndroidManifest()
        # Update our database version if necessary.
        updateDatabaseVersion()

        exit()

    if inputchar == '2':
        print "Have you already committed, pushed, and tagged the release version? (y/n)"

#    print "4. Set our pack URLs to secure URL"
  # Calculate the secure URL
  # Set the secure URL
#    print "5. Enter AMAZON or GOOGLE to set the marketplace."
  # Read in value
  # Validate for AMAZON or GOOGLE
  # Repeat if necessary
  # If AMAZON set market to AMAZON
  # If GOOGLE set market to GOOGLE

'''
Before Marketplace Upload
* Set Version Code one higher than whatever is published on the store
* Did we change the database? If so, update the database version.
* Tag the commit i.e. git tag v1.33 && git push --tags
* Remove Debug flag (DO NOT COMMIT)
* Remove TEST_URL flag
* Change the URL in PackClient to the live packs URL (for in-App-purchasing)
* Change Market flag in BuzzwordsApplication to the relevant store
* If FULL VSN (release-amazon as of 3-11-13): Follow the TODO inside DeckOpenHelper to install full packs.
** Clone our private repository of words
  git clone http://siramix.com/git/buzzwordspacks.git wordpacks/
** Copy packs/buzzwords_i.json and packs/buzzwords_ii.json to the buzzwords codebase
* Sign apk -- Right click project, Export - key is in dropbox
* Verify Lite version and Full version URLs in app
* Download onto a test device the soon to be out of date app

During Marketplace Upload
* Update screenshots
* Review market app info

After Marketplace Upload
* Update siramix.github site
* Update app on phone through marketplaceS
 
All new features are outlined in release notes.  Generally just UI changes plus the ability to name teams.

We also addressed the patch issue with upsale links. All Upsale dialogs should now also link directly to the Amazon store when purchased from the Amazon store. This is also true for the "Rate-us" dialogs.
'''

'''
Can we automate package creation from eclipse? signing, etc.
'''
if args.prep:
    print "Prepping codebase for final commit..."
    updateAndroidManifest()
    updateDatabaseVersion()
    printGitHelp()
elif args.market == "AMAZON":
    print "AMAZON!!!"
elif args.market == "GOOGLE":
    print "GOOGLE!!!"
else:
    parser.print_help()
