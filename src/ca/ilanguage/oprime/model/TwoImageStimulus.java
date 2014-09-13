package ca.ilanguage.oprime.model;

import java.util.ArrayList;

import ca.ilanguage.oprime.R;

public class TwoImageStimulus extends Stimulus {
  /**
	 * 
	 */
  private static final long serialVersionUID   = 4599224930707785294L;
  protected int             leftImageFileId;
  protected String          leftImageFilePath  = "";
  protected int             rightImageFileId;
  protected String          rightImageFilePath = "";

  public TwoImageStimulus() {
    super();
    this.leftImageFileId = R.drawable.androids_experimenter_kids;
    this.rightImageFileId = R.drawable.androids_experimenter_kids;
  }

  public TwoImageStimulus(int imageid) {
    super(imageid);
    this.leftImageFileId = imageid;
    this.rightImageFileId = imageid;
  }

  public TwoImageStimulus(int leftid, int rightid) {
    super(leftid);
    this.leftImageFileId = leftid;
    this.rightImageFileId = rightid;
  }

  public TwoImageStimulus(int leftid, int rightid, String label) {
    super(leftid, label);
    this.leftImageFileId = leftid;
    this.rightImageFileId = rightid;
    this.label = label;
  }

  public TwoImageStimulus(int imageid, String label) {
    super(imageid, label);
    this.leftImageFileId = imageid;
    this.rightImageFileId = imageid;
    this.label = label;
  }

  public TwoImageStimulus(String audioFilePath, String lImageFilePath, String rImageFilePath, String videoFilePath) {
    super(audioFilePath, lImageFilePath, videoFilePath);
    this.leftImageFilePath = lImageFilePath;
    this.rightImageFilePath = rImageFilePath;
  }

  public TwoImageStimulus(String audioFilePath, String lImageFilePath, String rImageFilePath, String videoFilePath,
      ArrayList<Touch> touches, long totalReactionTime, long reactionTimePostOffset) {
    super(audioFilePath, lImageFilePath, videoFilePath, touches, totalReactionTime, reactionTimePostOffset);
    this.leftImageFilePath = lImageFilePath;
    this.rightImageFilePath = rImageFilePath;
  }

  public int getLeftImageFileId() {
    return this.leftImageFileId;
  }

  public String getLeftImageFilePath() {
    return this.leftImageFilePath;
  }

  public int getRightImageFileId() {
    return this.rightImageFileId;
  }

  public String getRightImageFilePath() {
    return this.rightImageFilePath;
  }

  public void setLeftImageFileId(int leftImageFileId) {
    this.leftImageFileId = leftImageFileId;
  }

  public void setLeftImageFilePath(String leftImageFilePath) {
    this.leftImageFilePath = leftImageFilePath;
  }

  public void setRightImageFileId(int rightImageFileId) {
    this.rightImageFileId = rightImageFileId;
  }

  public void setRightImageFilePath(String rightImageFilePath) {
    this.rightImageFilePath = rightImageFilePath;
  }

}
