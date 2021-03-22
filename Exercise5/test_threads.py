import concurrent.futures
import threading, time

def execute_task(i):
    lock.acquire()
    print("Thread = ", threading.get_ident())
    print(i)
    i+=1
    time.sleep(1)
    lock.release()

i=1
lock = threading.Lock()
with concurrent.futures.ThreadPoolExecutor(max_workers=10) as executor:
    for num in range(10):
        executor.submit(execute_task, i)
