package ca.ilanguage.oprime.content;

import java.io.Serializable;
import java.util.ArrayList;

import com.google.gson.Gson;

public class SubExperimentBlock implements Serializable {
  private static final long serialVersionUID = -3637915995040502723L;
  protected String title = OPrime.EMPTYSTRING;
  protected String language = OPrime.DEFAULT_LANGUAGE;
  protected String description = OPrime.EMPTYSTRING;
  protected ArrayList<? extends Stimulus> stimuli;
  protected String resultsFileWithoutSuffix = OPrime.EMPTYSTRING;
  protected long startTime = 0;
  protected int displayedStimuli = 0;
  protected String intentToCallThisSubExperiment = OPrime.INTENT_START_SUB_EXPERIMENT;
  protected String intentToCallAfterThisSubExperiment = "";
  protected boolean autoAdvanceStimuliOnTouchIsPossible = true;
  protected boolean autoAdvanceStimuliOnTouch = false;

  public SubExperimentBlock() {
    super();
    this.title = OPrime.EMPTYSTRING;
    this.language = OPrime.DEFAULT_LANGUAGE;
    this.description = OPrime.EMPTYSTRING;
    this.startTime = System.currentTimeMillis();
    // this.stimuli = new ArrayList<Stimulus>();

  }

  public SubExperimentBlock(String title) {
    super();
    this.title = title;
    this.language = OPrime.DEFAULT_LANGUAGE;
    this.description = OPrime.EMPTYSTRING;
    this.startTime = System.currentTimeMillis();
    // this.stimuli = new ArrayList<Stimulus>();

  }

  public SubExperimentBlock(String title, String language, String description,
      ArrayList<? extends Stimulus> stimuli, String resultsFile) {
    super();
    this.title = title;
    this.language = language;
    this.description = description;
    this.resultsFileWithoutSuffix = resultsFile;
    this.stimuli = stimuli;
    this.startTime = System.currentTimeMillis();
  }

  public SubExperimentBlock(String title, String language, String description,
      ArrayList<? extends Stimulus> stimuli, String resultsFile,
      String intentToCall) {
    super();
    this.title = title;
    this.language = language;
    this.description = description;
    this.resultsFileWithoutSuffix = resultsFile;
    this.stimuli = stimuli;
    this.startTime = System.currentTimeMillis();
    this.intentToCallThisSubExperiment = intentToCall;
  }

  public SubExperimentBlock(String title, String language, String description,
      ArrayList<? extends Stimulus> stimuli, String resultsFile,
      String intentToCall, boolean autoAdvanceStimuliOnTouchIsPossible,
      boolean autoAdvanceStimuliOnTouch) {
    super();
    this.title = title;
    this.language = language;
    this.description = description;
    this.resultsFileWithoutSuffix = resultsFile;
    this.stimuli = stimuli;
    this.startTime = System.currentTimeMillis();
    this.intentToCallThisSubExperiment = intentToCall;
    this.autoAdvanceStimuliOnTouch = autoAdvanceStimuliOnTouch;
    this.autoAdvanceStimuliOnTouchIsPossible = autoAdvanceStimuliOnTouchIsPossible;
  }

  public boolean isExperimentProbablyComplete() {
    boolean complete = false;
    if (this.stimuli != null) {
      if (this.stimuli.size() > 0) {
        float completed = (this.displayedStimuli / this.stimuli.size());
        complete = completed > .8;
      }
    }
    return complete;
  }

  public String getResultsJson() {
    Gson gson = new Gson();
    String json = gson.toJson(this);
    return json;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ArrayList<? extends Stimulus> getStimuli() {
    return stimuli;
  }

  public void setStimuli(ArrayList<? extends Stimulus> stimuli) {
    this.stimuli = null;
    this.stimuli = stimuli;
  }

  public String getResultsFileWithoutSuffix() {
    return resultsFileWithoutSuffix;
  }

  public void setResultsFileWithoutSuffix(String resultsFileWithoutSuffix) {
    this.resultsFileWithoutSuffix = resultsFileWithoutSuffix;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public int getDisplayedStimuli() {
    return displayedStimuli;
  }

  public void setDisplayedStimuli(int displayedStimuli) {
    this.displayedStimuli = displayedStimuli;
  }

  public String getIntentToCallThisSubExperiment() {
    return intentToCallThisSubExperiment;
  }

  public void setIntentToCallThisSubExperiment(
      String intentToCallThisSubExperiment) {
    this.intentToCallThisSubExperiment = intentToCallThisSubExperiment;
  }

  public String getIntentToCallAfterThisSubExperiment() {
    return intentToCallAfterThisSubExperiment;
  }

  public void setIntentToCallAfterThisSubExperiment(
      String intentToCallAfterThisSubExperiment) {
    this.intentToCallAfterThisSubExperiment = intentToCallAfterThisSubExperiment;
  }

  public boolean isAutoAdvanceStimuliOnTouch() {
    if (this.autoAdvanceStimuliOnTouchIsPossible) {
      return autoAdvanceStimuliOnTouch;
    } else {
      return false;
    }
  }

  public void setAutoAdvanceStimuliOnTouch(
      boolean autoAdvanceStimuliOnTouchPreference) {
    this.autoAdvanceStimuliOnTouch = autoAdvanceStimuliOnTouchPreference;
  }

  public boolean isAutoAdvanceStimuliOnTouchIsPossible() {
    return autoAdvanceStimuliOnTouchIsPossible;
  }

  public void setAutoAdvanceStimuliOnTouchIsPossible(
      boolean autoAdvanceStimuliOnTouchIsPossible) {
    this.autoAdvanceStimuliOnTouchIsPossible = autoAdvanceStimuliOnTouchIsPossible;
  }

}
