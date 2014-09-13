package ca.ilanguage.oprime.model;

public class PageUrlGetPair {
  String delimiter = "#"; // "\\?"
  String filename;
  String getString;

  public PageUrlGetPair(String filename, String getString) {
    super();
    this.filename = filename;
    this.getString = getString;
  }

  public String getDelimiter() {
    return this.delimiter;
  }

  public String getFilename() {
    return this.filename;
  }

  public String getGetString() {
    return this.getString;
  }

  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public void setGetString(String getString) {
    this.getString = getString;
  }

  @Override
  public String toString() {
    return this.filename + this.delimiter + this.getString;
  }

}
