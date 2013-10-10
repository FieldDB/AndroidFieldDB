package ca.ilanguage.oprime.model;

import java.io.Serializable;

public class Touch implements Serializable {
  private static final long serialVersionUID = -6910004898670050860L;
  public int                height;
  public long               time;
  public int                width;
  public float              x;
  public float              y;

  public Touch() {
    super();
    this.x = 0;
    this.y = 0;
    this.width = 1;
    this.height = 1;
    this.time = System.currentTimeMillis();
  }

  public Touch(float x, float y, int width, int height, long time) {
    super();
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.time = time;
  }

  public int getHeight() {
    return this.height;
  }

  public long getTime() {
    return this.time;
  }

  public int getWidth() {
    return this.width;
  }

  public float getX() {
    return this.x;
  }

  public float getY() {
    return this.y;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public void setX(float x) {
    this.x = x;
  }

  public void setY(float y) {
    this.y = y;
  }

  @Override
  public String toString() {
    return this.x + ":" + this.width + "," + this.y + ":" + "height";
  }

}
