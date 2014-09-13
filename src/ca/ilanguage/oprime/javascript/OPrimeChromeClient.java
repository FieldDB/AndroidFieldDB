package ca.ilanguage.oprime.javascript;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import ca.ilanguage.oprime.Config;
import ca.ilanguage.oprime.R;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

public class OPrimeChromeClient extends WebChromeClient {

  @Override
  public boolean onConsoleMessage(ConsoleMessage cm) {
    if (cm.message() == null) {
      return true;
    }
    // if (D)
    Log.e(Config.TAG, cm.message() + " -- From line " + cm.lineNumber() + " of " + cm.sourceId());

    /*
     * Handle CORS server refusal to connect by telling user the entire error.
     */
    if (cm.message().startsWith("XMLHttpRequest cannot load")) {
      Log.d(Config.TAG, "CORS error. Please contact server administrator.");
    }
    return true;
  }

  /**
   * Could override like this, but that woudl make the saveApp funciton
   * obligatory on the apps if it has been 30 seconds, then save the app, and
   * redirect back to here after its done if(mLastUnloadSaveAppCalledTimestamp -
   * System.currentTimeMillis() > 30000){ Log.d(TAG,
   * "Calling window.saveApp("+url+")");
   * view.loadUrl("javascript:window.saveApp("+url+")");
   * mLastUnloadSaveAppCalledTimestamp = System.currentTimeMillis(); return
   * true; }else{ return super.onJsBeforeUnload(view, url, message, result); }
   */
  @Override
  public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
    if (view == null) {
      return true;
    }
    if (view.getContext() == null) {
      return true;
    }
    view.loadUrl("javascript:window.saveApp()");
    Log.d(Config.TAG, "Calling window.saveApp()");

    return super.onJsBeforeUnload(view, url, message, result);
  }

  @Override
  public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
    if (view == null) {
      return true;
    }
    if (view.getContext() == null) {
      return true;
    }
    new AlertDialog.Builder(view.getContext()).setTitle("").setMessage(message).setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        result.confirm();
      }
    }).setCancelable(false).create().show();
    return true;
  }

  @Override
  public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
    if (view == null) {
      return true;
    }
    if (view.getContext() == null) {
      return true;
    }
    new AlertDialog.Builder(view.getContext()).setTitle("").setMessage(message).setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        result.confirm();
      }
    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        result.cancel();
      }
    }).setCancelable(false).create().show();

    return true;
  }

  @Override
  public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
    if (view == null) {
      return true;
    }
    if (view.getContext() == null) {
      return true;
    }
    if (message.toLowerCase().contains("date")) {
      // Get today's date
      Calendar calendar = Calendar.getInstance();

      if ((defaultValue != null) && (defaultValue.length() > 0)) {
        // Set it to the previously-entered date, if it's formatted correctly
        try {
          calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(defaultValue));
        } catch (ParseException e) {
          Log.i(Config.TAG, "Incorrectly formatted date: " + defaultValue);
        }
      }

      // Create the dialog
      DatePickerDialog dialog = new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
          // Send result back to JS
          result.confirm(year + "-" + String.format("%02d", (monthOfYear + 1)) + "-" + String.format("%02d", (dayOfMonth)) + " 00:00:00");
        }
      }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

      // Ensure that he window.prompt even cancels successfully when the user
      // clicks "Cancel"
      dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Unknown", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          if (which == DialogInterface.BUTTON_NEGATIVE) {
            // Send cancel back to JS
            result.cancel();
          }
        }

      });

      dialog.setCancelable(false);

      // Add the title to the dialog
      dialog.setTitle(message);

      // Set the date to appear in the dialog
      dialog.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

      // Display DatePickerDialog
      dialog.show();
    } else {
      // get prompts.xml view
      LayoutInflater li = LayoutInflater.from(view.getContext());
      View promptsView = li.inflate(R.layout.dialog_edit_text, null);

      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());

      // set prompts.xml to alertdialog builder
      alertDialogBuilder.setView(promptsView);

      final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

      // If there was a previous value, display it
      if (defaultValue != null) {
        userInput.setText(defaultValue);
      }

      if (message.toLowerCase().endsWith("number")) {
        userInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
      }
      TextView prompt = (TextView) promptsView.findViewById(R.id.prompt);
      prompt.setText(message);
      // set dialog message
      alertDialogBuilder.setCancelable(false).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          // get user input and set it to result edit text
          Log.d(Config.TAG, userInput.getText().toString());
          result.confirm(userInput.getText().toString());
        }
      }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          result.cancel();
        }
      });

      // create alert dialog
      AlertDialog alertDialog = alertDialogBuilder.create();

      // show it
      alertDialog.show();
    }

    return true;
  }
}
