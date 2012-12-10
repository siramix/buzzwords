from xml.dom.minidom import parse
import sys
import codecs

if len(sys.argv) != 4:
    print "Usage: %s <xml input> <json output> <starting id>" % sys.argv[0]
    print "xml input: xml file to parse"
    print "json output: json output file"
    print "starting id: the starting id of the cards you are creating" 
    sys.exit()
dom = parse(sys.argv[1])
f = codecs.open(sys.argv[2],'w','utf-8')
cardindex = int(sys.argv[3])
cards = dom.getElementsByTagName('card')

"""
This is what we're up against. This is the enemy we face!
  <card>
    <pack-name>starter</pack-name>
    <title>Fly Swatter</title>
    <categories>starter</categories>
    <bad-words>
      <word>KILL</word>
      <word>SLAP</word>
      <word>BUG</word>
      <word>BUZZ</word>
      <word>TOOL</word>
    </bad-words>
  </card>
"""
for card in cards:
    title = card.getElementsByTagName('title')[0].childNodes[0].data
    badwords = card.getElementsByTagName('bad-words')[0].getElementsByTagName('word')
    badwordList = []
    for badword in badwords:
        badwordList.append(badword.childNodes[0].data)
    badwordString = ','.join(badwordList)
    f.write("{\"title\" : \"%s\", \"badwords\" : \"%s\", \"_id\" : %s}\n" % (title, badwordString, cardindex))
    cardindex += 1
f.close()
