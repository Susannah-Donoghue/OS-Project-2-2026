import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;

public class DomainThread  extends Thread{
    private final CapabilityLists system;
    private final int threadId;
    private String currentDomain;

    public DomainThread (CapabilityLists system, int threadId, String startingDomain) {
        this.system = system;
        this.threadId = threadId;
        this.currentDomain = startingDomain;
    }

    private void log(String message) {
        System.out.println("[Thread: " + threadId + "(" + currentDomain + ")]" + message);
    }

    private void yieldRandomly() {
        int count = ThreadLocalRandom.current().nextInt(3, 8);

        log("Yielding " + count + " times");

        for (int i = 0; i < count; i++) {
            Thread.yield();
        }
    }

    private void accessFile(int fileIndex, boolean isWrite) {
        FileObject file = system.getFile(fileIndex);
        String fileName = file.getName();

        CapabilityLists.Permission operation = isWrite ? CapabilityLists.Permission.WRITE : CapabilityLists.Permission.READ;

        log ("Attempting to " + (isWrite ? "write" : "read") + " resource: " + fileName);

        if (!system.arbitrator(currentDomain, fileName, operation)) {
            log ("Operation failed, permission denied");
            yieldRandomly();
            return;
        }

        Lock lock = isWrite ? file.getLock().writeLock() : file.getLock().readLock();

        lock.lock();

        try {
            if (isWrite) {
                String content = system.randomColor();
                file.write(content);

                log("Writing '" + content + "' to resource " + fileName);
            }
            else {
                log("Resource " + fileName + " contains " + file.read());
            }

            yieldRandomly();
        }
        finally {
            lock.unlock();
        }

        log("Operation Complet.");
    }

    private void switchDomain(int targetIndex) {
        String targetDomain = "D" + (targetIndex + 1);

        if (targetDomain.equals(currentDomain)) {
            return;
        }

        log("Attempting to switch from " + currentDomain + " to " + targetDomain + ".");

        if (!system.arbitrator(currentDomain, targetDomain, CapabilityLists.Permission.SWITCH)) {
            log("Operation failes, permission denied");
            yieldRandomly();
            return;
        }

        currentDomain = targetDomain;

        log("Switched to " + currentDomain);

        yieldRandomly();

        log("Operation Complete.");
    }

    @Override
    public void run() {
        for (int request = 0; request < 5; request++) {
            int choice;

            do {
                choice = ThreadLocalRandom.current().nextInt(system.getFileCount() + system.getDomainCount());
            }
            while (choice >= system.getFileCount() && ("D" + (choice - system.getFileCount() + 1)).equals(currentDomain));

            if (choice < system.getFileCount()) {
                boolean isWrite = ThreadLocalRandom.current().nextBoolean();

                accessFile(choice, isWrite);
            }

            else {
                switchDomain(choice - system.getFileCount());
            }
        }
    }
}
