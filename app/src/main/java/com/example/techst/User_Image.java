package com.example.techst;

public class User_Image {

     String imagename;
     String uri;
     String scannedtext;

    public User_Image() {
    }

    public User_Image(String imagename, String uri, String scannedtext) {
        this.imagename = imagename;
        this.uri = uri;
        this.scannedtext = scannedtext;
    }

    public String getImagename() {
        return imagename;
    }

    public void setImagename(String imagename) {
        this.imagename = imagename;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getScannedtext() {
        return scannedtext;
    }

    public void setScannedtext(String scannedtext) {
        this.scannedtext = scannedtext;
    }
}
