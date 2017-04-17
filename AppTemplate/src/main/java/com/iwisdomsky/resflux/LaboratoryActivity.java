package com.iwisdomsky.resflux;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.iwisdomsky.resflux.adapter.*;
import java.util.*;


public class LaboratoryActivity extends Activity
{
    

	private ProgressBar mProgress;
	private ListView mListView;
	private ArrayList<String> mPackagesList;
	private PackageListAdapter adapter;
	EditText filter;
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.laboratory);
		mProgress = (ProgressBar)findViewById(R.id.loading);
		mListView = (ListView)findViewById(R.id.packagelist);		
		mListView.setFastScrollEnabled(true);
		new Thread(loadPackagesList()).start();


				
	}


	
	
	private Runnable loadPackagesList(){
		return new Runnable(){
			@Override
			public void run(){				
				PackageManager pm = getPackageManager();
				List<ApplicationInfo> apps = pm.getInstalledApplications(0);
				Collections.sort(apps,new ApplicationInfo.DisplayNameComparator(pm));
				mPackagesList = new ArrayList<String>();
				for ( int i=0; i<apps.size(); i++ ) {
					String s = apps.get(i).packageName+"||"+apps.get(i).loadLabel(pm);
					mPackagesList.add(s);
				}				
				runOnUiThread(new Runnable(){
					public void run(){		
						loadPackagesCallback();
					}
				});
			}	
		};	
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
				experiment.putExtra("package_name",mPackagesList.get(p3).substring(0, mPackagesList.get(p3).indexOf("|")));
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
