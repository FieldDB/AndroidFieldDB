package ca.ilanguage.oprime.database;

import java.util.ArrayList;

public class User {
	protected String _id;
	protected String _rev;
	protected String username;
	protected String firstname;
	protected String lastname;
	protected String email;
	protected String gravatar;
	protected String affiliation;
	protected String researchInterest;
	protected String description;
	protected String subtitle;
	protected ArrayList<String> coments;
	protected String actualJSON;

	public User(String _id, String _rev, String username, String firstname,
			String lastname, String email, String gravatar, String affiliation,
			String researchInterest, String description, String subtitle,
			ArrayList<String> coments, String actualJSON) {
		super();
		this._id = _id;
		this._rev = _rev;
		this.username = username;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.gravatar = gravatar;
		this.affiliation = affiliation;
		this.researchInterest = researchInterest;
		this.description = description;
		this.subtitle = subtitle;
		this.coments = coments;
		this.actualJSON = actualJSON;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String get_rev() {
		return _rev;
	}

	public void set_rev(String _rev) {
		this._rev = _rev;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getGravatar() {
		return gravatar;
	}

	public void setGravatar(String gravatar) {
		this.gravatar = gravatar;
	}

	public String getAffiliation() {
		return affiliation;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	public String getResearchInterest() {
		return researchInterest;
	}

	public void setResearchInterest(String researchInterest) {
		this.researchInterest = researchInterest;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public ArrayList<String> getComents() {
		return coments;
	}

	public void setComents(ArrayList<String> coments) {
		this.coments = coments;
	}

	public String getActualJSON() {
		return actualJSON;
	}

	public void setActualJSON(String actualJSON) {
		this.actualJSON = actualJSON;
	}

}
