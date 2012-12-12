package com.diventi.mobipaper.xml;

import java.io.IOException;
import java.io.Serializable;

public class MobiImage implements Serializable {

  private static final long serialVersionUID = 1L;
  
  public MobiImage(String url, String localUrl, String noticiaUrl) {
    this.url        = url;
    this.localUrl   = localUrl;
    this.noticiaUrl = noticiaUrl; 
  }
  
  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.writeUTF(url);
    out.writeUTF(localUrl);
    out.writeUTF(noticiaUrl);
  }
  
  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    url        = in.readUTF();
    localUrl   = in.readUTF();
    noticiaUrl = in.readUTF();
  }
  
  public String url;
  public String localUrl;
  public String noticiaUrl;
  
}
