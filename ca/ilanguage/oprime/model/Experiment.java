package ca.ilanguage.oprime.model;

import java.util.ArrayList;

public class Experiment {
  Participant                   participant;
  ArrayList<SubExperimentBlock> subExperiments;
  String                        title;

  public Experiment() {
    super();
    this.title = "Untitled";
    this.participant = new Participant();

  }

  public Experiment(Participant participant) {
    super();
    this.participant = participant;

  }

  public Experiment(String title) {
    super();
    this.title = title;
    this.participant = new Participant();

  }

  public Participant getParticipant() {
    return this.participant;
  }

  public ArrayList<SubExperimentBlock> getSubExperiments() {
    return this.subExperiments;
  }

  public String getTitle() {
    return this.title;
  }

  public void setParticipant(Participant participant) {
    this.participant = participant;
  }

  public void setSubExperiments(ArrayList<SubExperimentBlock> subExperiments) {
    this.subExperiments = subExperiments;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getLanguage() {
    return "en";
  }

}
