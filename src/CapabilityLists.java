import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CapabilityLists {

    public enum Permission {
        READ,
        WRITE,
        READ_WRITE,
        SWITCH
    }

    public static class CapabilityEntry {
        private final String objectId;
        private final Permission permission;

        public CapabilityEntry(String objectId, Permission permission) {
            this.objectId = objectId;
            this.permission = permission;
        }

        public String getObjectId() {
            return objectId;
        }

        public Permission getPermission() {
            return permission;
        }

        public String format() {
            switch (permission) {
                case READ:
                    return objectId + ":R";
                case WRITE:
                    return objectId + ":W";
                case READ_WRITE:
                    return objectId + ":R/W";
                case SWITCH:
                    return objectId + ":allow";
                default:
                    return objectId + ":unknown";
            }
        }
    }
    private final int domainCount;
    private final int fileCount;

    private final Map<String, List<CapabilityEntry>> capabilityLists = new LinkedHashMap<>();

    private final List<FileObject> files = new ArrayList<>();

    private static final String[] COLORS = {"Red", "Blue", "Green", "Orange", "Pink", "Yellow", "Purple"};

    public CapabilityLists() {
        domainCount = ThreadLocalRandom.current().nextInt(3,8);

        fileCount = ThreadLocalRandom.current().nextInt(3,8);

        for (int i = 1; i <= domainCount; i++) {
            capabilityLists.put("D" + i, new ArrayList<>());
        }

        for(int i = 1; i <= fileCount; i++) {
            files.add(new FileObject("F" + i, randomColor()));
        }

        generateCapabilities();
    }

    public String randomColor() {
        return COLORS [ThreadLocalRandom.current().nextInt(COLORS.length)];
    }

    public int getDomainCount(){
        return domainCount;
    }

    public int getFileCount() {
        return fileCount;
    }

    public FileObject getFile(int index) {
        return files.get(index);
    }

    private void generateCapabilities() {
        for (int d = 1; d <= domainCount; d++) {

            List<CapabilityEntry> list = capabilityLists.get("D" + d);

            for (int f = 1; f <= fileCount; f++) {
                int choice = ThreadLocalRandom.current().nextInt(4);

                Permission permission;

                switch(choice) {
                    case 0:
                        continue;
                    case 1:
                        permission = Permission.READ;
                        break;
                    case 2:
                        permission = Permission.WRITE;
                        break;
                    default:
                        permission = Permission.READ_WRITE;
                        break;
                }

                list.add(new CapabilityEntry("F" + f, permission));
            }

            for (int target = 1; target <= domainCount; target ++) {
                if (target == d) {
                    continue;
                }

                if (ThreadLocalRandom.current().nextBoolean()) {
                   list.add(new CapabilityEntry("D" + target, Permission.SWITCH));
                }
            }
        }
    }

    public void printCapilities() {
        System.out.println("Access control scheme: Capability List");
        System.out.println("Domain Count: " + domainCount);
        System.out.println("Object Count: " + fileCount);

        for (Map.Entry<String, List<CapabilityEntry>> domain : capabilityLists.entrySet()) {
            StringJoiner entries = new StringJoiner(", ");

            for (CapabilityEntry entry : domain.getValue()) {
                entries.add(entry.format());
            }

            System.out.println(domain.getKey() + " --> " + entries);
        }

        System.out.println();
    }

    public boolean arbitrator (String domainId, String objectId, Permission requestedPermission) {
        List <CapabilityEntry> list = capabilityLists.get(domainId);

        boolean allowed = false;

        if (list != null) {
            for (CapabilityEntry entry : list) {
                if (!entry.getObjectId().equals(objectId)) {
                    continue;
                }

                Permission granted = entry.getPermission();

                if (requestedPermission == Permission.READ) {
                    allowed = granted == Permission.READ || granted == Permission.READ_WRITE;
                }
                else if (requestedPermission == Permission.WRITE) {
                    allowed = granted == Permission.WRITE || granted == Permission.READ_WRITE;
                }
                else if (requestedPermission == Permission.SWITCH) {
                    allowed = granted == Permission.SWITCH;
                }

                break;
            }
        }

        System.out.println("[Arbitrator] Checking " + domainId + " -> " + objectId + " (" + requestedPermission + ")");
        System.out.println("[Arbitrator] Permission " + (allowed ? "granted" : "denied"));

        return allowed;
    }

    public void runSimulation () {
        printCapilities();

        List<DomainThread> threads = new ArrayList<>();

        for (int i = 0; i < domainCount; i++) {
            threads.add(new DomainThread(this, i, "D" + (i+1)));
        }

        for (DomainThread thread : threads) {
            thread.start();
        }

        for (DomainThread thread : threads ) {
            try {
                thread.join();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Simulation interupted");

                return;
            }
        }

        System.out.println("All threads completed.");
    }

}
