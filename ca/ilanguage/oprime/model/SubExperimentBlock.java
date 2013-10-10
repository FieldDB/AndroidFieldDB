package ca.ilanguage.oprime.model;

import java.io.Serializable;
import java.util.ArrayList;

import ca.ilanguage.oprime.Config;

import com.google.gson.Gson;

public class SubExperimentBlock implements Serializable {
  private static final long               serialVersionUID                    = -3637915995040502723L;
  protected boolean                       autoAdvanceStimuliOnTouch           = false;
  protected boolean                       autoAdvanceStimuliOnTouchIsPossible = true;
  protected String                        description                         = Config.EMPTYSTRING;
  protected int                           displayedStimuli                    = 0;
  protected String                        intentToCallAfterThisSubExperiment  = "";
  protected String                        intentToCallThisSubExperiment       = Config.INTENT_START_SUB_EXPERIMENT;
  protected String                        language                            = Config.DEFAULT_LANGUAGE;
  protected String                        resultsFileWithoutSuffix            = Config.EMPTYSTRING;
  protected long                          startTime                           = 0;
  protected ArrayList<? extends Stimulus> stimuli;
  protected String                        title                               = Config.EMPTYSTRING;

  public SubExperimentBlock() {
    super();
    this.title = Config.EMPTYSTRING;
    this.language = Config.DEFAULT_LANGUAGE;
    this.description = Config.EMPTYSTRING;
    this.startTime = System.currentTimeMillis();
    // this.stimuli = new ArrayList<Stimulus>();

  }

  public SubExperimentBlock(String title) {
    super();
    this.title = title;
    this.language = Config.DEFAULT_LANGUAGE;
    this.description = Config.EMPTYSTRING;
    this.startTime = System.currentTimeMillis();
    // this.stimuli = new ArrayList<Stimulus>();

  }

  public SubExperimentBlock(String title, String language, String description, ArrayList<? extends Stimulus> stimuli,
      String resultsFile) {
    super();
    this.title = title;
    this.language = language;
    this.description = description;
    this.resultsFileWithoutSuffix = resultsFile;
    this.stimuli = stimuli;
    this.startTime = System.currentTimeMillis();
  }

  public SubExperimentBlock(String title, String language, String description, ArrayList<? extends Stimulus> stimuli,
      String resultsFile, String intentToCall) {
    super();
    this.title = title;
    this.language = language;
    this.description = description;
    this.resultsFileWithoutSuffix = resultsFile;
    this.stimuli = stimuli;
    this.startTime = System.currentTimeMillis();
    this.intentToCallThisSubExperiment = intentToCall;
  }

  public SubExperimentBlock(String title, String language, String description, ArrayList<? extends Stimulus> stimuli,
      String resultsFile, String intentToCall, boolean autoAdvanceStimuliOnTouchIsPossible,
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

  public String getDescription() {
    return this.description;
  }

  public int getDisplayedStimuli() {
    return this.displayedStimuli;
  }

  public String getIntentToCallAfterThisSubExperiment() {
    return this.intentToCallAfterThisSubExperiment;
  }

  public String getIntentToCallThisSubExperiment() {
    return this.intentToCallThisSubExperiment;
  }

  public String getLanguage() {
    return this.language;
  }

  public String getResultsFileWithoutSuffix() {
    return this.resultsFileWithoutSuffix;
  }

  public String getResultsJson() {
    Gson gson = new Gson();
    String json = gson.toJson(this);
    return json;
  }

  public long getStartTime() {
    return this.startTime;
  }

  public ArrayList<? extends Stimulus> getStimuli() {
    return this.stimuli;
  }

  public String getTitle() {
    return this.title;
  }

  public boolean isAutoAdvanceStimuliOnTouch() {
    if (this.autoAdvanceStimuliOnTouchIsPossible) {
      return this.autoAdvanceStimuliOnTouch;
    } else {
      return false;
    }
  }

  public boolean isAutoAdvanceStimuliOnTouchIsPossible() {
    return this.autoAdvanceStimuliOnTouchIsPossible;
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

  public void setAutoAdvanceStimuliOnTouch(boolean autoAdvanceStimuliOnTouchPreference) {
    this.autoAdvanceStimuliOnTouch = autoAdvanceStimuliOnTouchPreference;
  }

  public void setAutoAdvanceStimuliOnTouchIsPossible(boolean autoAdvanceStimuliOnTouchIsPossible) {
    this.autoAdvanceStimuliOnTouchIsPossible = autoAdvanceStimuliOnTouchIsPossible;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setDisplayedStimuli(int displayedStimuli) {
    this.displayedStimuli = displayedStimuli;
  }

  public void setIntentToCallAfterThisSubExperiment(String intentToCallAfterThisSubExperiment) {
    this.intentToCallAfterThisSubExperiment = intentToCallAfterThisSubExperiment;
  }

  public void setIntentToCallThisSubExperiment(String intentToCallThisSubExperiment) {
    this.intentToCallThisSubExperiment = intentToCallThisSubExperiment;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public void setResultsFileWithoutSuffix(String resultsFileWithoutSuffix) {
    this.resultsFileWithoutSuffix = resultsFileWithoutSuffix;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public void setStimuli(ArrayList<? extends Stimulus> stimuli) {
    this.stimuli = null;
    this.stimuli = stimuli;
  }

  public void setTitle(String title) {
    this.title = title;
  }

}
