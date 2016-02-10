import struct
from datetime import datetime
from collections import namedtuple

format = struct.Struct('<IHHcccxIq')
Record = namedtuple('Record', 'id lang length tailSpace isUnused isNumeric mapFilePos timestamp text')

def loader(fp):
	fp.seek(256)

	while fp.readable():
		header = fp.read(format.size)
		try:
			record = list(format.unpack(header))
		except struct.error:
			raise StopIteration

		record[3] = bool(record[3][0] & 0x1)
		record[4] = bool(record[4][0] & 0x1)
		record[5] = bool(record[5][0] & 0x1)
		record[7] = datetime.utcfromtimestamp(record[7])

		if record[3]:
			data = fp.read(1023).decode('tis620')
		else:
			buffer = bytearray()
			while True:
				i = fp.read(1)[0]
				if i == 0:
					break
				buffer.append(i)
			data = buffer.decode('tis620')

		record.append(data)

		yield Record._make(record)

if __name__ == '__main__':
	import sys

	filename = sys.argv[1] if len(sys.argv) > 1 else 'SyllableDB-V1.dat'

	i = 0
	for item in loader(open(filename, 'rb')):
		assert item.id == i + 1, 'id {} is not sequential'.format(item.id)
		print(item)
		i += 1
