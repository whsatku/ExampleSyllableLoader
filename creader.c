#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>

#define c_time int64_t

#define TIME_DETECT 0
#define TIME_64 1
#define TIME_32 2

typedef struct DATA_RECORD{
	struct header{
		uint32_t id;
		// 00    000000,0000 0       0      0        0
		// lang  length     tailSpc unused numeric filler
		uint16_t lang:2;     // 2
		uint16_t length:10;  // 12
		uint16_t tailSpace:1; // 13*
		uint16_t isUnused:1;  // 14
		uint16_t numeric:1;   // 15
		uint16_t _filler:1;   // 16 -> 2 byte
		uint32_t mapFilePos;
	} header;
	int timeSize;
	c_time timestamp;
	char szText[1024];
} DATA_RECORD;

void syllable_skip_to_data(FILE* fp){
	fseek(fp, 256, SEEK_SET);
}

int syllable_read_record(FILE* fp, DATA_RECORD* record, int timeSize){
	if(fread(&record->header, sizeof(record->header), 1, fp) != 1){
		return 0;
	}

	// detect
	if(timeSize == TIME_DETECT){
		fseek(fp, 8, SEEK_CUR);
		int success = 1;
		for(int i = 0; i < 4; i++){
			if(fgetc(fp) != 0){
				success = 0;
				fseek(fp, 4-i, SEEK_CUR);
				break;
			}
		}
		if(success == 1){
			timeSize = TIME_64;
		}else{
			timeSize = TIME_32;
		}
		fseek(fp, -8-4, SEEK_CUR);
	}

	record->timeSize = timeSize;

	if(timeSize == TIME_64){
		fread(&record->timestamp, sizeof(int64_t), 1, fp);
		fseek(fp, 4, SEEK_CUR);
	}else if(timeSize == TIME_32){
		int32_t timestamp;
		fread(&timestamp, sizeof(timestamp), 1, fp);
		record->timestamp = (int64_t) timestamp;
	}

	int i;
	for(i = 0; i < sizeof(record->szText); i++){
		record->szText[i] = fgetc(fp);

		if(record->szText[i] == 0){
			i++;
			break;
		}
	}
	record->szText[i] = 0;

	return 1;
}

int main(int argc, char *argv[]){
	if(argc < 2){
		printf("usage: %s filename.dat\n", argv[0]);
		return 0;
	}
	FILE* fp = fopen(argv[1], "rb");
	syllable_skip_to_data(fp);

	DATA_RECORD* record = (DATA_RECORD*) malloc(sizeof(DATA_RECORD));
	int lastId = 0;
	int timeSize = TIME_DETECT;
	while(!feof(fp)){
		if(syllable_read_record(fp, record, timeSize) != 1){
			break;
		}

		if(record->header.id != lastId + 1){
			printf("E: Record not continuous. Expecting ID %d, found %d\n", lastId+1, record->header.id);
			return 1;
		}

		lastId = record->header.id;
		timeSize = record->timeSize;

		printf(
			"record %d lang %d length %d tailSpace %d unused %d numeric %d mapfilepos %d timestamp %lld\n",
			record->header.id, record->header.lang, record->header.length, record->header.tailSpace,
			record->header.isUnused, record->header.numeric, record->header.mapFilePos,
			record->timestamp
		);
		printf("%s\n=========\n", record->szText);
	}

	return 0;
}
