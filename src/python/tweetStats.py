#!/usr/bin/env python3

# I refer to tweets as messages, msg for short.
# The general aproach here is to try and use multiple cores by distributing the
# msgs to workers. It won't outperform for a small number of msgs < ~200.

# The block diagram would look like:
# Ingest msg -> Sequence msg -> Distribute msg to worker
# Workers will supply results for the two tasks to two different blocks:
# - A dict of words and their counts is passed to the word accumulator for ft1.
# - A sequence and count of unique words in the msg will be passed to:
#     A buffered resequencer -> then a Running median calculator for ft2.

import argparse
from collections import Counter
import fileinput
from multiprocessing import Process, SimpleQueue, cpu_count, freeze_support
import os
import sys

cores = 0
# Check the input as conforming to the challenge specifications.
def check(msg):
    if msg.isprintable() and msg.islower():
        return msg
    else:
        return ''

# Just a wrapper for splitting the string on whitespace.
def split(msg):
    return msg.split()

# Accepts messages and sends their word count and sequenced unique words out.
def worker(in_queue, unique_words_queue, word_count_queue):
    for seq, msg in iter(in_queue.get, None):
        c = Counter(split(check(msg)))
        word_count_queue.put(c)
        unique_words_queue.put((seq, len(c)))
    # Pass on doneness
    unique_words_queue.put(None)
    word_count_queue.put(None)

# Accumulates word counts from a queue of counts from each message.
# Then writes the sorted counts to a file.
def accumulator(in_queue, outdir):
    word_counts = Counter()
    for workers in range(cores):
        for c in iter(in_queue.get, None):
            word_counts.update(c)
    with open(os.path.join(outdir, "ft1.txt"),"w") as file:
        for item in sorted(word_counts.items()):
            file.write("{:<28} {}\n".format(*item))

# Dequeues the counts of unique words per message in sequenced order and sends
# the ordered sequence out.
def buffered_resequener(in_queue, out_queue):
    buffer = {}
    expect = 0
    for workers in range(cores):
        for seq, i in iter(in_queue.get, None):
            buffer[seq] = i
            while (expect in buffer):
                out_queue.put(buffer[expect])
                del buffer[expect]
                expect += 1
    out_queue.put(None)

# Keeps a set of values seen from the queue and writes a running median to a
# file as each value is recieved.
def running_median(in_queue, outdir):
    seen = [0]*70
    total = 0
    with open(os.path.join(outdir, "ft2.txt"), "w") as file:
        for i in iter(in_queue.get, None):
            seen[i] += 1
            total += 1
            # Finding the median in the seen histogram
            m = 0
            s = seen[m]
            while (s * 2 < total and m < 69):
                m += 1
                s += seen[m]
            if (s * 2 == total):
                n = m + 1
                while (n < 70 and seen[n] == 0 ):
                    n += 1
                m = (m + n) / 2
            file.write("{}\n".format(m))

def sequencer(out_queue, seq=0):
    seq = [seq]
    def s(msg):
        cur = seq[0]
        seq[0] += 1
        out_queue.put((cur, msg))
    return s

def ingest(files, out_queue):
    seq = sequencer(out_queue)
    with fileinput.input(files) as f:
        for line in f:
            seq(line.strip());

def parse_args():
    parser = argparse.ArgumentParser(
        description='''Count words in tweets.
        Word counts are written to ft1.txt.
        A running median of unique words per tweet is written to ft2.txt.''')
    parser.add_argument('--outdir', '-o', default='tweet_output',
        help='directory in which to write ft1.txt and ft2.txt.')
    parser.add_argument('file', nargs='*',
        help='files to read tweets from, or none for stdin')
    args = parser.parse_args()

    if os.path.exists(args.outdir):
        if os.path.isdir(args.outdir):
            return args
        else:
            print("The output directory {!r} isn't a directory.".format(
                args.outdir))
            sys.exit(-1)
    else:
        print("The output directory {!r} doesn't exist.".format(args.outdir))
        sys.exit(-1)

def start(parsed_args):
    processes          = []
    msg_queue          = SimpleQueue()
    word_count_queue   = SimpleQueue()
    unique_words_queue = SimpleQueue()
    median_queue       = SimpleQueue()

    # Prep workers to read from msg queue and write to other queues
    for i in range(cores):
        p = Process(target=worker,
                      args=(msg_queue, unique_words_queue, word_count_queue))
        processes.append(p)
        p.start()

    # Prep a process to accumulate word_count_queue for ft1.txt
    p = Process(target=accumulator,
                  args=(word_count_queue, parsed_args.outdir))
    processes.append(p)
    p.start()

    # Prep a process to re-sequence unique words counted
    p = Process(target=buffered_resequener,
                  args=(unique_words_queue, median_queue))
    processes.append(p)
    p.start()

    # Prep a process to keep a running median of unique words for ft2.txt
    p = Process(target=running_median,
                  args=(median_queue, parsed_args.outdir))
    processes.append(p)
    p.start()

    # Start reading msgs for the msg_queue
    ingest(parsed_args.file, msg_queue)

    # Sending an indication to stop, one for each process
    for i in range(cores):
        msg_queue.put(None)

    # This step gathers the child processes, but may be unneccessary
    for p in processes:
        p.join()


if __name__ == '__main__':
    freeze_support()
    cores = cpu_count()
    start(parse_args())
