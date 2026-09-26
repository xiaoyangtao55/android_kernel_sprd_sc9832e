# tools/dtbtool/Makefile
# 独立编译 dtbtool，不参与内核主构建

CC      := gcc
CFLAGS  := -O2 -Wall -D_GNU_SOURCE

TARGET  := dtbtool
SRC     := dtbtool.c

.PHONY: all clean

all: $(TARGET)

$(TARGET): $(SRC)
	$(CC) $(CFLAGS) -o $@ $<

clean:
	rm -f $(TARGET)