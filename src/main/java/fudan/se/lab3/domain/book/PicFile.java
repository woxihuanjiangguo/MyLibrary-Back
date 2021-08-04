package fudan.se.lab3.domain.book;

import javax.persistence.*;

@Entity
public class PicFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fileId")
    private int fileId;

   // @Transient
    @Column(name = "image", columnDefinition = "mediumBlob")
    private byte[] file;

    public PicFile(byte[] file) {
        this.file = file;
    }

    public PicFile() {
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public int getFileId() {
        return fileId;
    }


}
