

public class ObjectACL {

    private String objectName;
    private boolean fileObject;
    private List<PermissionEntry> enteries;


    public ObjectACL(String objectName, boolean fileObject) {
        this.objectName = objectName;
        this.fileObject = fileObject;
        this.enteries = new ArrayList<>;
    }

    public static class PermissionEntry {
        private String domainName;
        private boolean canRead;
        private boolean canWrite;
        private boolean canSwitch;

        private PermissionEntry(String domainName, boolean canRead, boolean canWrite, boolean canSwitch) {
            this.domainName = domainName;
            this.canRead = canRead;
            this.canWrite = canWrite;
            this.canSwitch = canSwitch;
        }


    }
}