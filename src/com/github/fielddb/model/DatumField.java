package com.github.fielddb.model;

public class DatumField {
  protected String label;
  protected String value;
  protected String mask;
  protected String encrypted;
  protected String shouldBeEncrypted;
  protected String help;
  protected String size;
  protected String showToUserTypes;
  protected String userchooseable;

  public DatumField(String label, String value, String mask, String encrypted, String shouldBeEncrypted, String help,
      String size, String showToUserTypes, String userchooseable) {
    super();
    this.label = label;
    this.value = value;
    this.mask = mask;
    this.encrypted = encrypted;
    this.shouldBeEncrypted = shouldBeEncrypted;
    this.help = help;
    this.size = size;
    this.showToUserTypes = showToUserTypes;
    this.userchooseable = userchooseable;
  }

  public DatumField(String label, String value) {
    super();
    this.label = label;
    this.value = value;
    this.mask = value;
    this.encrypted = "false";
    this.shouldBeEncrypted = "true";
    this.help = "Field created on an Android App";
    this.showToUserTypes = "all";
    this.userchooseable = "false";
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getMask() {
    return mask;
  }

  public void setMask(String mask) {
    this.mask = mask;
  }

  public String getEncrypted() {
    return encrypted;
  }

  public void setEncrypted(String encrypted) {
    this.encrypted = encrypted;
  }

  public String getShouldBeEncrypted() {
    return shouldBeEncrypted;
  }

  public void setShouldBeEncrypted(String shouldBeEncrypted) {
    this.shouldBeEncrypted = shouldBeEncrypted;
  }

  public String getHelp() {
    return help;
  }

  public void setHelp(String help) {
    this.help = help;
  }

  public String getSize() {
    return size;
  }

  public void setSize(String size) {
    this.size = size;
  }

  public String getShowToUserTypes() {
    return showToUserTypes;
  }

  public void setShowToUserTypes(String showToUserTypes) {
    this.showToUserTypes = showToUserTypes;
  }

  public String getUserchooseable() {
    return userchooseable;
  }

  public void setUserchooseable(String userchooseable) {
    this.userchooseable = userchooseable;
  }

}