package Utils;

public class SourceLocation {
    private long lineNo;
    private long linePos;
    private long filePos;
    private String fileName;

    public long getLineNo() {
        return lineNo;
    }
    public long getLinePos() {
        return linePos;
    }
    public long getFilePos() {
        return filePos;
    }
    public String getFileName() {
        return fileName;
    }

    public SourceLocation(String fileName, long lineNo, long linePos, long filePos) {
        this.fileName = fileName;
        this.lineNo = lineNo;
        this.linePos = linePos;
        this.filePos = filePos;
    }

    protected SourceLocation(SourceLocation copy) {
        this.fileName = copy.fileName;
        this.lineNo   = copy.lineNo;
        this.linePos  = copy.linePos;
        this.filePos  = copy.filePos;
    }

    @Override
    public String toString() {
        return getFileName() + ":" + getLineNo() + ":" + getLinePos();
    }
}
