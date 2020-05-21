package org.remad.dto;

import java.io.Serializable;

public class Data implements Serializable {

    public Data() {}

    /**
     * This is factory method to create a new Data instance.
     */
    public static Data createData(String fullFileName, byte[] file) {
        Data data = new Data();
        data.setFullFileName(fullFileName);
        data.setFile(file);

        return data;
    }

    public String getFullFileName() {
        return fullFileName;
    }

    public void setFullFileName(String fullFileName) {
        if(fullFileName == null || fullFileName.isEmpty()) {
            throw new IllegalStateException();
        }
        this.fullFileName = fullFileName;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    private String fullFileName; // Required
    private byte[] file; // Required;
}
