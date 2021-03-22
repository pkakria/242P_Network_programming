import re, sys, collections
import logging
import threading
import time, os, concurrent.futures

def read_stopwords():
    stopwords = set(open('stop_words').read().split(','))
    return stopwords

#in python we can just add counters with + to merge them
class Counter:
    def __init__(self, stopwords):
        self.counts = collections.Counter()
        self.stopwords = stopwords
        
    def process(self, f):
        words = re.findall('\w{3,}', open(f, encoding='utf-8').read().lower())
        self.counts.update(w for w in words if w not in self.stopwords)

    def __str__(self):
        s = ""
        for (w, c) in self.counts.most_common(40):
            s = s + str(w) + '-' + str(c) + '\n'
        return s

    def merge(self, other_counter):
        self.counts = self.counts + other_counter.counts
    
def countwords(c, global_counter, f, lock):
    c.process(f) # update local counter
    with lock:
        print("This is thread ", threading.current_thread().ident, " updating global counter")
        global_counter.merge(c)
   
    
def main():
    # do something
    print("Main thread is ", threading.get_ident())
    stopwords = read_stopwords()
    lock = threading.Lock() # this is to allow only one thread to take the lock
    global_counter = Counter(stopwords)
    start_time = time.time_ns()
    with concurrent.futures.ThreadPoolExecutor(max_workers=10) as executor:
        for root, dirs, files in os.walk("."):
            for file in files:
                if file.endswith(".txt"):
                    executor.submit(countwords, Counter(stopwords), global_counter, os.path.join(root, file), lock)
    end_time = time.time_ns()
    print(global_counter)
    print("Elapsed time = ", (end_time-start_time)/1e6, " ms")


if __name__ == "__main__":
    main()
