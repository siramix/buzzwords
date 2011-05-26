from xml.dom.minidom import parse
import sys
import codecs

if len(sys.argv) != 3:
  print "Usage: %s <xml input> <csv output>" % sys.argv[0]
  sys.exit()
dom = parse(sys.argv[1])
f = codecs.open(sys.argv[2],'w','utf-8')
cards = dom.getElementsByTagName('card')

for card in cards:
  f.write(card.getElementsByTagName('title')[0].childNodes[0].data + ',')
  words = card.getElementsByTagName('bad-words')[0].getElementsByTagName('word')
  for word in words:
    f.write(word.childNodes[0].data.upper() + ',')
  f.write(card.getElementsByTagName('categories')[0].childNodes[0].data + ',')
  f.write(card.getElementsByTagName('pack-name')[0].childNodes[0].data + ',')
  f.write('\n')

f.close()
  
