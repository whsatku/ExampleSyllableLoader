import struct
from io import SEEK_CUR

def loader(fp):
	fp.seek(256)

	is64 = None

	while fp.readable():
		try:
			id = struct.unpack('<I', fp.read(4))[0]
		except struct.error:
			return
		fp.seek(1, SEEK_CUR)
		hasTailSpace = (fp.read(1)[0] & 1<<3) >> 3 == 1
		fp.seek(2+4, SEEK_CUR)

		if is64 == None:
			fp.seek(8, SEEK_CUR)
			if fp.read(4) == b'\0\0\0\0':
				is64 = True
			else:
				is64 = False
				fp.seek(-4, SEEK_CUR)
		elif is64 == True:
			fp.seek(8+4, SEEK_CUR)
		elif is64 == False:
			fp.seek(4, SEEK_CUR)

		if hasTailSpace:
			data = fp.read(1023).decode('tis620')
		else:
			buffer = bytearray()
			while True:
				i = fp.read(1)[0]
				if i == 0:
					break
				buffer.append(i)
			data = buffer.decode('tis620')

		yield [id, data]

if __name__ == '__main__':
	import sys

	filename = sys.argv[1] if len(sys.argv) > 1 else 'SyllableDB-V1.dat'

	i = 0
	for item in loader(open(filename, 'rb')):
		assert item[0] == i + 1, 'id {} is not sequential'.format(item[0])
		print(item)
		i += 1
