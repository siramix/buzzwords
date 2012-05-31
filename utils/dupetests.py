import sys
import os
from decimal import *

def help():
    print "Usage: %s <logcat file> <# of phrases to test>" % sys.argv[0]
    print "**Make sure you stream the logcat output to this script (adb logcat | python dupetests.py 1000)"

def main():
    os.system("adb logcat -c")
    list = []
    numlines = 0
    print "\nLogcat cleared ***PROBABLY NOT*** run 'adb logcat -c' to be sure"
    print "Reading in %s lines." % sys.argv[1]
    print "^C to stop input"    
    
    while numlines < int(sys.argv[1]):    
        try: 
            line = sys.stdin.readline()
        except KeyboardInterrupt:
            break

        if not line:
            break

        if "Dealing" in line:
            # Add our word to the list of words encountered
            rhs = line.partition('::')[2]
            word = rhs.partition('::')[0]
            list.append(word)
            numlines += 1

    print "\nDone reading logcat. Running stats."

    # Calc the number of occurances of all words and packs
    wordsAndCounts = {}
    packs = {}
    wordsum = 0
    for word in list:
        # Count each occurrance of each distinct word
        count = list.count(word)
        wordsAndCounts[word] = count
        wordsum += 1
        # Count each occurance of each distinct pack
        packId = word.partition('PACK ')[2]
        if packId in packs:
            packs[packId] += 1
        else:
            packs[packId] = 1

    # Count the distribution of words, i.e. how many
    # words showed up once, twice, three times, a lady
    count_distribution = {}
    for word in wordsAndCounts:
        countkey = wordsAndCounts[word]
#        print "%s : %s" % (word, countkey)
        if countkey in count_distribution:
            count_distribution[countkey] += 1
        else:
            count_distribution[countkey] = 1

    print "TOTAL WORDS: %d" % wordsum
    print "PACK PERCENTAGES: %s" % packs
    for pack, count in packs.iteritems():
        dcount = Decimal(count)
        dtotal = Decimal(wordsum)
        percentage = round((100*(dcount/dtotal)),2)
        print "PackID %s had %s cards shown which was %s%% of total" % (pack, count, percentage)
    print "DISTRIBUTION OF WORD_COUNTS: %s" % count_distribution
    for countnum, countofcountnum in count_distribution.iteritems():
        print "Number of words seen %s time(s): %s" % (countnum, countofcountnum)

if __name__ == '__main__':
    if len(sys.argv) != 2:
        help()
        exit()
    help()
    main()
