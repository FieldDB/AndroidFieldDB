package com.github.opensourcefieldlinguistics.fielddb;

/* https://github.com/ACRA/acralyzer/wiki/setup */
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.annotation.ReportsCrashes;

import com.github.opensourcefieldlinguistics.fielddb.lessons.Config;
import com.github.opensourcefieldlinguistics.fielddb.lessons.georgian.R;

import android.app.Application;

@ReportsCrashes(formKey = "", formUri = "", reportType = org.acra.sender.HttpSender.Type.JSON, httpMethod = org.acra.sender.HttpSender.Method.PUT, formUriBasicAuthLogin = "see_private_constants", formUriBasicAuthPassword = "see_private_constants")
public class FieldDBApplication extends Application {
	@Override
	public final void onCreate() {
		super.onCreate();

		ACRAConfiguration config = ACRA.getNewDefaultConfig(this);
		config.setFormUri(Config.ACRA_SERVER_URL);
		config.setFormUriBasicAuthLogin(Config.ACRA_USER);
		config.setFormUriBasicAuthPassword(Config.ACRA_PASS);

		/* https://github.com/OpenSourceFieldlinguistics/FieldDB/issues/1435 */
		boolean doesAcraSupportKeystoresWorkaroundForSNIMissingVirtualhosts = false;
		if (doesAcraSupportKeystoresWorkaroundForSNIMissingVirtualhosts) {

			// Get an instance of the Bouncy Castle KeyStore format
			KeyStore trusted;
			try {
				trusted = KeyStore.getInstance("BKS");
				// Get the raw resource, which contains the keystore with
				// your trusted certificates (root and any intermediate certs)
				InputStream in = getApplicationContext().getResources()
						.openRawResource(R.raw.sslkeystore);
				try {
					// Initialize the keystore with the provided trusted
					// certificates
					// Also provide the password of the keystore
					trusted.load(in, Config.KEYSTORE_PASS.toCharArray());
					// TODO waiting for https://github.com/ACRA/acra/pull/132
					// config.setKeyStore(trusted);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (CertificateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} catch (KeyStoreException e1) {
				e1.printStackTrace();
			}

		} else {
			// TODO waiting for https://github.com/ACRA/acra/pull/132
			config.setDisableSSLCertValidation(true);
		}

		ACRA.setConfig(config);

		ACRA.init(this);
	}
}
