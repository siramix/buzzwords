outfile = open('ids.csv','w')
x = open('foo.txt')
j = x.readlines()
for i in j:
  outfile.write(i.split('=')[1].strip())
  outfile.write(',')
outfile.close()
