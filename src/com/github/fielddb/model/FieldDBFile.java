package com.github.fielddb.model;

public class FieldDBFile {
  protected String filename;
  protected String description;
  protected String URL;

  public FieldDBFile(String filename, String description, String uRL) {
    super();
    this.filename = filename;
    this.description = description;
    URL = uRL;
  }

  public FieldDBFile(String filename) {
    super();
    this.filename = filename;
    this.description = "";
    this.URL = filename;
  }

  public String getFilename() {
    return this.filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getURL() {
    return URL;
  }

  public void setURL(String uRL) {
    URL = uRL;
  }

}