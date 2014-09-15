package com.github.opensourcefieldlinguistics.fielddb.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.github.opensourcefieldlinguistics.fielddb.R;

public class Stimulus implements Serializable {

  private static final long serialVersionUID       = -4023355491498842498L;
  protected int             audioFileId            = R.raw.gammatone;
  protected String          audioFilePath          = "";
  protected long            audioOffset            = 0;
  protected int             imageFileId            = R.drawable.speech_bubbles;
  protected String          imageFilePath          = "";

  protected String          label                  = "";

  protected long            reactionTimePostOffset = 0;

  protected long            startTime              = 0;
  protected long            totalReactionTime      = 0;
  public ArrayList<Touch>   touches                = new ArrayList<Touch>();
  protected String          videoFilePath          = "";

  public Stimulus() {
    super();
  }

  public Stimulus(int imageid) {
    super();
    this.imageFileId = imageid;
  }

  public Stimulus(int imageid, int audioid) {
    super();
    this.audioFileId = audioid;
    this.imageFileId = imageid;
  }

  public Stimulus(int imageid, String label) {
    super();
    this.imageFileId = imageid;
    this.label = label;
  }

  public Stimulus(String audioFilePath, String imageFilePath, String videoFilePath) {
    super();
    this.audioFilePath = audioFilePath;
    this.imageFilePath = imageFilePath;
    this.videoFilePath = videoFilePath;
  }

  public Stimulus(String audioFilePath, String imageFilePath, String videoFilePath, ArrayList<Touch> touches,
      long totalReactionTime, long reactionTimePostOffset) {
    super();
    this.audioFilePath = audioFilePath;
    this.imageFilePath = imageFilePath;
    this.videoFilePath = videoFilePath;
    this.totalReactionTime = totalReactionTime;
    this.reactionTimePostOffset = reactionTimePostOffset;
  }

  public int getAudioFileId() {
    return this.audioFileId;
  }

  public String getAudioFilePath() {
    return this.audioFilePath;
  }

  public long getAudioOffset() {
    return this.audioOffset;
  }

  public int getImageFileId() {
    return this.imageFileId;
  }

  public String getImageFilePath() {
    return this.imageFilePath;
  }

  public String getLabel() {
    return this.label;
  }

  public long getReactionTimePostOffset() {
    return this.reactionTimePostOffset;
  }

  public long getStartTime() {
    return this.startTime;
  }

  public long getTotalReactionTime() {
    return this.totalReactionTime;
  }

  public ArrayList<Touch> getTouches() {
    return this.touches;
  }

  public String getVideoFilePath() {
    return this.videoFilePath;
  }

  public void setAudioFileId(int audioFileId) {
    this.audioFileId = audioFileId;
  }

  public void setAudioFilePath(String audioFilePath) {
    this.audioFilePath = audioFilePath;
  }

  public void setAudioOffset(long audioOffset) {
    this.audioOffset = audioOffset;
  }

  public void setImageFileId(int imageFileId) {
    this.imageFileId = imageFileId;
  }

  public void setImageFilePath(String imageFilePath) {
    this.imageFilePath = imageFilePath;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setReactionTimePostOffset(long reactionTimePostOffset) {
    this.reactionTimePostOffset = reactionTimePostOffset;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public void setTotalReactionTime(long totalReactionTime) {
    this.totalReactionTime = totalReactionTime;
  }

  public void setTouches(ArrayList<Touch> touches) {
    this.touches = touches;
  }

  public void setVideoFilePath(String videoFilePath) {
    this.videoFilePath = videoFilePath;
  }

  @Override
  public String toString() {
    String s = this.label + this.touches.get(this.touches.size()).toString();
    return s;
  }

}
