package com.iwisdomsky.resflux.adapter;


import android.app.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.iwisdomsky.resflux.*;
import java.util.*;

public class PackageListAdapter extends ArrayAdapter<String> implements Filterable {

	private final Activity mContext;
	public ArrayList<String> mPackages;
	public ArrayList<String> mFilteredPackages;
	private ApkFilter mApkFilter;
	
	
	static class ViewHolder {
		public TextView name;
		public TextView source;
		public ImageView icon;
	}

	
	public PackageListAdapter(Activity context, ArrayList<String> packages) {
		super (context, R.layout.package_item, packages);
		this.mContext = context;
		this.mPackages = packages;
		this.mFilteredPackages = new ArrayList<String>(packages);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) { 
		View rowView = convertView;

		// reuse views

		if ( rowView == null ) { 
			LayoutInflater inflater = mContext.getLayoutInflater(); 
			rowView = inflater.inflate(R.layout.package_item, null);

			// configure view holder 
			ViewHolder viewHolder = new ViewHolder(); 
			viewHolder.name = (TextView) rowView.findViewById(R.id.app_name); 		
			viewHolder.source = (TextView) rowView.findViewById(R.id.app_source); 	
			viewHolder.icon = (ImageView) rowView.findViewById(R.id.app_icon); 
			rowView.setTag(viewHolder); 
		}

		// fill data 
		ViewHolder holder = (ViewHolder) rowView.getTag();
		try
		{
			String packageStr;

			if (mFilteredPackages.get(position).indexOf("|") == -1)
			{
				packageStr = mFilteredPackages.get(position);
			}
			else
			{
				packageStr = (mFilteredPackages.get(position)).substring(0, mFilteredPackages.get(position).indexOf("|"));
			}

			AndroidPackage pkg = new AndroidPackage(mContext, packageStr);

			// fill holder with data
			holder.name.setText(pkg.LABEL);
			holder.source.setText(pkg.SOURCE);
			holder.icon.setImageDrawable(pkg.ICON);

			// color coding red = system, green = phone, yellow = sdcard
			if (pkg.SOURCE.startsWith("/system"))
				holder.source.setTextColor(0xFFFF7777);
			else if (pkg.SOURCE.startsWith("/data"))
				holder.source.setTextColor(0xFF77FF77);
			else
				holder.source.setTextColor(0xFFFFFF77);
		} catch (IndexOutOfBoundsException e)
		{
			Log.e("ResFlux", "getView: indexoutofbounds");
		}

		return rowView; 
	}

	@Override
	public int getCount() {
		return mFilteredPackages.size();
	}


	@Override
	public Filter getFilter() {
		if (mApkFilter == null)
			mApkFilter = new ApkFilter();

		return mApkFilter;
	}

	private class ApkFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			// Create a FilterResults object
			FilterResults results = new FilterResults();

			// If the constraint (search string/pattern) is null
			// or its length is 0, i.e., its empty then
			// we just set the `values` property to the
			// original list which contains all of them
			if (constraint == null || constraint.length() == 0) {
				results.values = mPackages;
				results.count = mPackages.size();
			}
			else {
				// Some search constraint has been passed
				// so let's filter accordingly
				ArrayList<String> filteredRes = new ArrayList<String>();

				// We'll go through all the res and see
				// if they contain the supplied string
				for (String c : mPackages) {
					if ((c.toUpperCase().contains( constraint.toString().toUpperCase() ))) {
						// if `contains` == true then add it
						// to our filtered list
						filteredRes.add(c);
					}
				}

				// Finally set the filtered values and size/count
				results.values = filteredRes;
				results.count = filteredRes.size();
			}

			// Return our FilterResults object
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			mFilteredPackages.clear();
			mFilteredPackages.addAll((ArrayList<String>) results.values);
			notifyDataSetChanged();
		}
	}
		
		
}
