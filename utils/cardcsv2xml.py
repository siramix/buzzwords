from xml.dom.minidom import getDOMImplementation
import sys
import re
import codecs

if len(sys.argv) != 3:
  print "Usage: %s <csv input> <xml output>" % sys.argv[0]
  sys.exit()

f = codecs.open(sys.argv[1],'r','utf-8')

impl = getDOMImplementation()

newdoc = impl.createDocument(None, 'pack', None)
top = newdoc.documentElement

for line in f.readlines():
  row = line.split(',')

  cardEl = newdoc.createElement('card')

  titleEl = newdoc.createElement('title')
  titleTxt = newdoc.createTextNode(row[0])
  titleEl.appendChild(titleTxt)

  badWordsEl = newdoc.createElement('bad-words')

  packNameEl = newdoc.createElement('pack-name')
  packNameTxt = newdoc.createTextNode(row[7])
  packNameEl.appendChild(packNameTxt)

  catEl = newdoc.createElement('categories')
  catTxt = newdoc.createTextNode(row[6])
  catEl.appendChild(catTxt)

  for i in range(1,6):
    wordEl = newdoc.createElement('word')
    wordTxt = newdoc.createTextNode(row[i].upper())
    wordEl.appendChild(wordTxt)
    badWordsEl.appendChild(wordEl)
  cardEl.appendChild(packNameEl)
  cardEl.appendChild(titleEl)
  cardEl.appendChild(catEl)
  cardEl.appendChild(badWordsEl)
  top.appendChild(cardEl)

outfile = codecs.open(sys.argv[2], 'w','utf-8')
uglyXml = newdoc.toprettyxml(indent='  ')
text_re = re.compile('>\n\s+([^<>\s].*?)\n\s+</', re.DOTALL)    
prettyXml = text_re.sub('>\g<1></', uglyXml)
outfile.write(prettyXml)
outfile.close()
