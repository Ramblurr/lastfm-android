package fm.last.android.activity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import fm.last.android.LastFMApplication;
import fm.last.android.R;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

public class BugReport extends DialogPreference {
	private EditText mBug = null;
	
	public BugReport(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void showDialog(Bundle state) {
		super.showDialog(state);
        mBug = (EditText)getDialog().findViewById(R.id.bug);
   		LastFMApplication.getInstance().tracker.trackPageView("/BugReport");
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if(positiveResult) {
			// These are the files to include in the ZIP file
		    String[] filenames = new String[]{"player.log", "scrobbler.log"};
		    
		    // Create a buffer for reading the files
		    byte[] buf = new byte[1024];
		    
		    try {
		        // Create the ZIP file
		        String outFilename = "/sdcard/lastfm-logs.zip";
		        Log.i("Last.fm", "Creating " + outFilename);
		        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
		    
		        // Compress the files
		        for (int i=0; i<filenames.length; i++) {
		        	try {
		            FileInputStream in = new FileInputStream(getContext().getFilesDir().getAbsolutePath() + "/" + filenames[i]);
		    
		            // Add ZIP entry to output stream.
		            out.putNextEntry(new ZipEntry(filenames[i]));
		            
		            // Transfer bytes from the file to the ZIP file
		            int len;
		            while ((len = in.read(buf)) > 0) {
		                out.write(buf, 0, len);
		            }
		    
		            // Complete the entry
		            out.closeEntry();
		            in.close();
		        	} catch (FileNotFoundException e) {
		        		Log.i("Last.fm", "Skipping non-existing log file: " + filenames[i]);
		        	}
		        }

		        //Run 'logcat' and store the output as a file inside our zip
		        ArrayList<String> commandLine = new ArrayList<String>();
                commandLine.add("logcat");
                commandLine.add("-d");
                commandLine.add("-v");
                commandLine.add("time");
                
                Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[0]));
                InputStream in = process.getInputStream();
                
	            // Add ZIP entry to output stream.
	            out.putNextEntry(new ZipEntry("logcat.log"));

	            int len;
                while ((len = in.read(buf)) > 0) {
                	out.write(buf,0,len);
                }
		        
                out.closeEntry();
                in.close();
                
		        // Complete the ZIP file
		        out.close();
		        
		        String bugReport = 
		        	"Description\n" +
		        	"===========\n" +
		        	mBug.getText() + "\n" +
		        	"\nAdditional Info\n" +
		        	"===========\n" +
		        	"App version: " + getContext().getPackageManager().getPackageInfo("fm.last.android", 0).versionName + "\n" +
		        	"Device: " + Build.MODEL + "\n" +
		        	"Android version: " + Build.VERSION.RELEASE + "\n" +
		        	"Firmware fingerprint: " + Build.FINGERPRINT + "\n";
		        
		        String address[] = {"Last.fm Client Team <sam@last.fm>"};
		        Intent email = new Intent(Intent.ACTION_SEND);
		        email.putExtra(Intent.EXTRA_EMAIL, address);
		        email.putExtra(Intent.EXTRA_TEXT, bugReport);
		        email.putExtra(Intent.EXTRA_SUBJECT, "Bug Report: Last.fm for Android");
		        email.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + outFilename));
		        email.setType("message/rfc822");
		        getContext().startActivity(Intent.createChooser(email, "Email:"));  
		    } catch (Exception e) {
		    	e.printStackTrace();
		    }
		}
	}
}
