import sys

if len(sys.argv) != 5:
  print "Usage: %s <# words to generate> <pack Id> <starting ID> <name of outfile>" % sys.argv[0]
  print "   ex: generateWordFile.py 1000 2 1000 res/raw/pack2.json"
  sys.exit()

outfile = open(sys.argv[4], 'w')

for i in range(int(sys.argv[1])):
  word = i+1
  packid = int(sys.argv[2])
  index = int(sys.argv[3])+word
  diff = i%3
  wordStr = '{"title": "WORD %d - PACK %d", "_id": %d, "badwords": "Word 1, Word 2, Word 3, Word 4, Word 5"}\n' % (word, packid, index)
  outfile.write(wordStr)

outfile.close()
