package ca.ilanguage.oprime.model;

import java.io.Serializable;
import java.util.ArrayList;

import ca.ilanguage.oprime.R;

public class Stimulus implements Serializable {

  private static final long serialVersionUID       = -4023355491498842498L;
  protected int             audioFileId            = R.raw.ploep;
  protected String          audioFilePath          = "";
  protected long            audioOffset            = 0;
  protected int             imageFileId            = R.drawable.androids_experimenter_kids;
  protected String          imageFilePath          = "";

  protected String          label                  = "";

  protected long            reactionTimePostOffset = 0;

  protected long            startTime              = 0;
  protected long            totalReactionTime      = 0;
  public ArrayList<Touch>   touches                = new ArrayList<Touch>();
  protected String          videoFilePath          = "";

  public Stimulus() {
    super();
    this.audioFilePath = "";
    this.audioFileId = R.raw.ploep;
    this.imageFilePath = "";
    this.imageFileId = R.drawable.androids_experimenter_kids;
    this.videoFilePath = "";
    this.touches = new ArrayList<Touch>();
    this.totalReactionTime = 0;
    this.reactionTimePostOffset = 0;
  }

  public Stimulus(int imageid) {
    super();
    this.audioFilePath = "";
    this.audioFileId = R.raw.ploep;
    this.imageFilePath = "";
    this.imageFileId = imageid;
    this.videoFilePath = "";
    this.touches = new ArrayList<Touch>();
    this.totalReactionTime = 0;
    this.reactionTimePostOffset = 0;
  }

  public Stimulus(int imageid, int audioid) {
    super();
    this.audioFilePath = "";
    this.audioFileId = audioid;
    this.imageFilePath = "";
    this.imageFileId = imageid;
    this.videoFilePath = "";
    this.touches = new ArrayList<Touch>();
    this.totalReactionTime = 0;
    this.reactionTimePostOffset = 0;
  }

  public Stimulus(int imageid, String label) {
    super();
    this.audioFilePath = "";
    this.audioFileId = R.raw.ploep;
    this.imageFilePath = "";
    this.imageFileId = imageid;
    this.videoFilePath = "";
    this.touches = new ArrayList<Touch>();
    this.totalReactionTime = 0;
    this.reactionTimePostOffset = 0;
    this.label = label;
  }

  public Stimulus(String audioFilePath, String imageFilePath, String videoFilePath) {
    super();
    this.audioFilePath = audioFilePath;
    this.imageFilePath = imageFilePath;
    this.videoFilePath = videoFilePath;
    this.touches = new ArrayList<Touch>();
    this.totalReactionTime = 0;
    this.reactionTimePostOffset = 0;
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
