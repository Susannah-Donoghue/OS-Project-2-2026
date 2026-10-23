import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileObject {
    private final String name;
    private String content;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public FileObject(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public ReentrantReadWriteLock getLock() {
        return lock;
    }

    public String read() {
        return content;
    }

    public void write(String newContent) {
        content = newContent;
    }
}
