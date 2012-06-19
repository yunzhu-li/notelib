package org.fatlyz.notelib;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NBExpandableListAdapter extends BaseExpandableListAdapter {
	//Members
	private List<? extends Map<String, ?>> mGroupData;
	private int mExpandedGroupLayout;
	private int mCollapsedGroupLayout;
	private String[] mGroupFrom;
	private int[] mGroupTo;

	private List<? extends List<? extends Map<String, ?>>> mChildData;
	private int mChildLayout;
	private int mLastChildLayout;
	private String[] mChildFrom;
	private int[] mChildTo;

	private LayoutInflater mInflater;


	public NBExpandableListAdapter(Context context, List<? extends Map<String, ?>> groupData,
			int groupLayout, String[] groupFrom, int[] groupTo,
			List<? extends List<? extends Map<String, ?>>> childData, int childLayout,
			String[] childFrom, int[] childTo) {
		this(context, groupData, groupLayout, groupLayout, groupFrom, groupTo, childData,
				childLayout, childLayout, childFrom, childTo);
	}

	public NBExpandableListAdapter(Context context, List<? extends Map<String, ?>> groupData,
			int expandedGroupLayout, int collapsedGroupLayout, String[] groupFrom, int[] groupTo,
			List<? extends List<? extends Map<String, ?>>> childData, int childLayout,
			String[] childFrom, int[] childTo) {
		this(context, groupData, expandedGroupLayout, collapsedGroupLayout, groupFrom, groupTo,
				childData, childLayout, childLayout, childFrom, childTo);
	}

	public NBExpandableListAdapter(Context context, List<? extends Map<String, ?>> groupData,
			int expandedGroupLayout, int collapsedGroupLayout, String[] groupFrom, int[] groupTo,
			List<? extends List<? extends Map<String, ?>>> childData, int childLayout,
			int lastChildLayout, String[] childFrom, int[] childTo) {
		mGroupData = groupData;
		mExpandedGroupLayout = expandedGroupLayout;
		mCollapsedGroupLayout = collapsedGroupLayout;
		mGroupFrom = groupFrom;
		mGroupTo = groupTo;

		mChildData = childData;
		mChildLayout = childLayout;
		mLastChildLayout = lastChildLayout;
		mChildFrom = childFrom;
		mChildTo = childTo;

		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public Object getChild(int groupPosition, int childPosition) {
		return mChildData.get(groupPosition).get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	/**
	 * Instantiates a new View for a child.
	 * @param isLastChild Whether the child is the last child within its group.
	 * @param parent The eventual parent of this new View.
	 * @return A new child View
	 */
	public View newChildView(boolean isLastChild, ViewGroup parent) {
		return mInflater.inflate((isLastChild) ? mLastChildLayout : mChildLayout, parent, false);
	}

	public int getChildrenCount(int groupPosition) {
		return mChildData.get(groupPosition).size();
	}

	public Object getGroup(int groupPosition) {
		return mGroupData.get(groupPosition);
	}

	public int getGroupCount() {
		return mGroupData.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	/**
	 * Instantiates a new View for a group.
	 * @param isExpanded Whether the group is currently expanded.
	 * @param parent The eventual parent of this new View.
	 * @return A new group View
	 */
	public View newGroupView(boolean isExpanded, ViewGroup parent) {
		return mInflater.inflate((isExpanded) ? mExpandedGroupLayout : mCollapsedGroupLayout,
				parent, false);
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public boolean hasStableIds() {
		return true;
	}

	private void bindView(View view, Map<String, ?> data, String[] from, int[] to) {
		   int len = to.length;
		   
		   for (int i = 0; i < len; i++) {
			   View v = view.findViewById(to[i]);
			   if (v != null) {
				   if (v instanceof ImageView)
				   {
					   //if null
					   if (data.get(from[i]) == null){
						   ((ImageView)v).setImageDrawable(null);
					   }else
					   //if Bitmap
					   if (data.get(from[i]) instanceof Bitmap)
					   {
						   ((ImageView)v).setImageBitmap((Bitmap) data.get(from[i]));
					   //if ResID
					   }else{
						   ((ImageView)v).setImageResource((Integer) data.get(from[i]));
					   }
				   }else
					   if (v instanceof TextView)
					   {
						   ((TextView)v).setText((String) data.get(from[i]));
					   }
			   }//if
		   }//for
	}

	public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
			ViewGroup parent) {
		View v;
		if (convertView == null) {
			v = newGroupView(isExpanded, parent);
		} else {
			v = convertView;
		}
		
		bindView(v, mGroupData.get(groupPosition), mGroupFrom, mGroupTo);
		return v;
	}

	public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
			View convertView, ViewGroup parent) {
		View v;
		if (convertView == null) {
			v = newChildView(isLastChild, parent);
		} else {
			v = convertView;
		}


		bindView(v, mChildData.get(groupPosition).get(childPosition), mChildFrom, mChildTo);
		return v;
	}

	
	
}
