package com.iwisdomsky.resflux;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;


public class MainActivity extends Activity
{

	ScrollView mRooms;
	ProgressBar mProgress;

	Button continueExperimentButton;
	Button newExperimentButton;
	Button importButton;
	Button exportButton;
	Button helpButton;
	Button aboutButton;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		mRooms = (ScrollView) findViewById(R.id.rooms);
		mProgress = (ProgressBar) findViewById(R.id.loading);

		continueExperimentButton = (Button) findViewById(R.id.continue_experiment_button);
		newExperimentButton = (Button) findViewById(R.id.new_experiment_button);
		importButton = (Button) findViewById(R.id.import_button);
		exportButton = (Button) findViewById(R.id.export_button);
		helpButton = (Button) findViewById(R.id.help_button);
		aboutButton = (Button) findViewById(R.id.about_button);

		continueExperimentButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View view)
			{
				laboratory(LaboratoryActivity.REQUEST_CODE_ONLY_MODDED);
			}
		});

		newExperimentButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View view)
			{
				laboratory(LaboratoryActivity.REQUEST_CODE_FULL);
			}
		});

		importButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View view)
			{
				importt();
			}
		});

		exportButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View view)
			{
				export();
			}
		});

		helpButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View view)
			{
				help();
			}
		});

		aboutButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View view)
			{
				about();
			}
		});




		getFilesDir().mkdir();
		Utils.mkDirs(Environment.getExternalStorageDirectory(),"Resflux");
		Utils.mkDirs(getFilesDir(),"packages");		
		
		if ( false/*!isXposedInstalled()*/ ) {
			new AlertDialog.Builder(this)
			.setTitle("Can't detect Xposed!")
			.setMessage("It seems that Xposed is not yet installed in your system.\nXposed Framework is required to be installed first in order for Resflux to work.\n\nDo you want to download the Xposed Installer?")
			.setPositiveButton("Download", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface p1, int p2)
				{
					String url = "http://forum.xda-developers.com/showpost.php?p=44034334&postcount=2315";
					if ( Build.VERSION.SDK_INT >= 15 )
						url = "http://repo.xposed.info/module/de.robv.android.xposed.installer";
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url));
					startActivity(i);
					finish();
				}
			})
			.setNegativeButton("Not yet", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface p1, int p2)
				{
					finish();
				}
			})
			.setOnCancelListener(new DialogInterface.OnCancelListener(){
				public void onCancel(DialogInterface p1)
				{
					finish();
				}			
			}).create().show();
			
		}
		
		new Thread(loadReqs()).start();		
			
	}
	
	public void laboratory(int request_code) {
		Intent i = new Intent(MainActivity.this,LaboratoryActivity.class);
		i.putExtra("REQUEST_CODE", request_code);
		startActivity(i);
	}
	
	public void importt() {
		Intent i = new Intent(MainActivity.this,ImportActivity.class);
		startActivity(i);
	}
	
	
	public void export() {
		Intent i = new Intent(MainActivity.this,ExportActivity.class);
		startActivity(i);
	}

	public void compile() {
		try
		{
			ApplicationInfo ai = getPackageManager().getApplicationInfo("com.iwisdomsky.resflux.compiler", 0);
			Intent i = getPackageManager().getLaunchIntentForPackage("com.iwisdomsky.resflux.compiler");
			startActivity(i);
		}
		catch (PackageManager.NameNotFoundException e)
		{
			new AlertDialog.Builder(this)
				.setTitle("Resflux Compiler is not installed!")
				.setMessage("It seems the Resflux Compiler is not installed. You need to download and install the Resflux Compiler in order to use this feature.\n\n"+
							"Resflux Compiler will allow you to export your Resflux mods into its stand-alone xposed module form.\n\nCheck READ ME! for more details")
				.create().show();			
		}

	
	}	
	
	public void about() {
		LayoutInflater i = getLayoutInflater();
		View vv = i.inflate(R.layout.about,null);
		vv.setMinimumWidth(Constants.DIALOG_MIN_WIDTH);
		vv.setMinimumHeight(Constants.DIALOG_MIN_WIDTH);	
		new AlertDialog.Builder(this)
		.setView(vv)
		.create().show();
	}
	
	public void help() {
		LayoutInflater i = getLayoutInflater();
		View vv = i.inflate(R.layout.help,null);
		vv.setMinimumWidth(Constants.DIALOG_MIN_WIDTH);
		vv.setMinimumHeight(Constants.DIALOG_MIN_WIDTH);	
		new AlertDialog.Builder(this)
			.setView(vv)
			.create().show();
	}
	
	private Runnable loadReqs(){
		return new Runnable(){
			public void run()
			{		
				new File(Environment.getExternalStorageDirectory(),"Resflux").mkdir();
				AssetFile asset2 = new AssetFile(MainActivity.this,"icon.png");
				//if ( !asset.isExtracted() )
					asset2.extract();
				runOnUiThread(new Runnable(){
					public void run()
					{
						mProgress.setVisibility(View.GONE);
						mRooms.setVisibility(View.VISIBLE);
					}
				});
			}			
		};
	}

	public boolean isXposedInstalled(){
		try
		{
			DataInputStream in = new DataInputStream(Runtime.getRuntime().exec("app_process").getErrorStream());
			String line;
			while( (line = in.readLine()) != null )
				if ( line.toLowerCase().contains("xposed") ){
					in.close();					
					return true;
				}
		}
		catch (IOException e)
		{}
		return false;
	} 
	
	
}
