package com.github.opensourcefieldlinguistics.fielddb.service;

import android.app.IntentService;
import android.content.Intent;

public class RegisterUserService extends IntentService {

	public RegisterUserService(String name) {
		super(name);
	}

	public RegisterUserService() {
		super("RegisterUserService");
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		// TODO Auto-generated method stub

	}

}
