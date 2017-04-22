package com.iwisdomsky.resflux;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import com.iwisdomsky.resflux.adapter.*;

import java.io.File;
import java.util.*;


public class LaboratoryActivity extends Activity
{
    

	private ProgressBar mProgress;
	private ListView mListView;
	private ArrayList<String> mPackagesList;
	private PackageListAdapter adapter;
	EditText filter;

	public static int REQUEST_CODE_FULL = 0;
	public static int REQUEST_CODE_ONLY_MODDED = 1;



	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.laboratory);
		mProgress = (ProgressBar)findViewById(R.id.loading);
		mListView = (ListView)findViewById(R.id.packagelist);		
		mListView.setFastScrollEnabled(true);

		int requestCode = getIntent().getIntExtra("REQUEST_CODE",0);
		new Thread(loadPackagesList(requestCode)).start();


				
	}


	
	
	private Runnable loadPackagesList(final int request_code){
		return new Runnable(){
			@Override
			public void run(){

				PackageManager pm = getPackageManager();

				Log.d("Load package list with request code:" + request_code);
				mPackagesList = new ArrayList<String>();


				if (request_code == LaboratoryActivity.REQUEST_CODE_FULL)
				{
					List<ApplicationInfo> apps = pm.getInstalledApplications(0);
					Collections.sort(apps, new ApplicationInfo.DisplayNameComparator(pm));

					//full load
					for (int i = 0; i < apps.size(); i++)
					{
						String s = apps.get(i).packageName + "||" + apps.get(i).loadLabel(pm);
						mPackagesList.add(s);
					}
				}
				else if (request_code == LaboratoryActivity.REQUEST_CODE_ONLY_MODDED)
				{
					List<ApplicationInfo> apps = new ArrayList<ApplicationInfo>();

					//only modded
					File packages_dir = new File(getFilesDir(), "packages");
					// each packages
					for (File p : packages_dir.listFiles())
					{
						// if package has mods
						if (isPackageDirHasContents(p))
						{
							try
							{
								apps.add(pm.getApplicationInfo(p.getName(), 0));
							} catch (PackageManager.NameNotFoundException e)
							{
							}
						}/*
							// if the dir has no existing mods then do some cleaning
						else
						{
							Utils.deleteFile(p);
						}*/
					}
					// sort packages alphabetically
					Collections.sort(apps, new ApplicationInfo.DisplayNameComparator(pm));

					for (int i = 0; i < apps.size(); i++)
					{
						String s = apps.get(i).packageName + "||" + apps.get(i).loadLabel(pm);
						mPackagesList.add(s);
					}
				}



				runOnUiThread(new Runnable(){
					public void run(){		
						loadPackagesCallback();
					}
				});
			}	
		};	
	}

	// check if the packages dir has packages with mods
	public boolean isPackageDirHasContents(File package_dir){
		for ( File sub : package_dir.listFiles() )
			if ( sub.listFiles().length > 0) {
				return true;
			}
		return false;
	}

	private void loadPackagesCallback(){
		mProgress.setVisibility(View.GONE);
		mListView.setVisibility(View.VISIBLE);
		adapter = new PackageListAdapter(LaboratoryActivity.this,mPackagesList);
		mListView.setAdapter(adapter);	
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
			{				
				Intent experiment = new Intent(LaboratoryActivity.this,ExperimentActivity.class);
				experiment.putExtra("package_name",adapter.mFilteredPackages.get(p3).substring(0, adapter.mFilteredPackages.get(p3).indexOf("|")));
				startActivity(experiment);
			}
		});

		filter = (EditText)findViewById(R.id.lab_filter);

		filter.addTextChangedListener(new TextWatcher(){
			public void beforeTextChanged(CharSequence a,int b,int c,int d){

			}
			public void onTextChanged(CharSequence a,int b,int c,int d){
				Log.d("Resflux", "onTextChanged apk filter: " + a);
				adapter.getFilter().filter(a.toString());
			}
			public void afterTextChanged(Editable e){

			}
		});

		Button clearFilter = (Button)findViewById(R.id.lab_filter_button);
		clearFilter.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				filter.setText("");
			}
		});

	}


	
}
