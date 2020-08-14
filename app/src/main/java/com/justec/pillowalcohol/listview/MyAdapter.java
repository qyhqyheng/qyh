package com.justec.pillowalcohol.listview;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.justec.pillowalcohol.R;
import java.util.List;

public class MyAdapter extends BaseExpandableListAdapter {
	private List<GroupBean> list;
	private Context context;
	private Handler handler;
	ClickInview clickInview;

	public MyAdapter(List<GroupBean> list, Context context) {
		this.list = list;
		this.context = context;
		//this.clickInview = clickInview;
	/*	handler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				notifyDataSetChanged();
				super.handleMessage(msg);
			}
		};*/
	}


    /*供外界更新数据的方法*/
    public void refresh(ExpandableListView expandableListView, int groupPosition){
        handler.sendMessage(new Message());
        //必须重新伸缩之后才能更新数据
        expandableListView.collapseGroup(groupPosition);
        expandableListView.expandGroup(groupPosition);
    }

	@Override
	public int getGroupCount() {
		return list.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
        return list.get(groupPosition).getChildren().size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return list.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if (list != null && childPosition < list.size()) {
            Log.d("Jerry.Xiao","children =2 ");
			return list.get(groupPosition).getChildren().get(childPosition);
		}
		return null;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
							 View convertView, ViewGroup parent) {
		GroupHolder holder;
		if (convertView == null) {
			holder = new GroupHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_group, null);
			holder.title = (TextView) convertView
					.findViewById(R.id.group_title);
			holder.iv = (ImageView) convertView.findViewById(R.id.group_ico);
			convertView.setTag(holder);
		} else {
			holder = (GroupHolder) convertView.getTag();
		}
		holder.title.setText(list.get(groupPosition).getGroupName());
		if (isExpanded) {
			holder.iv.setImageResource(R.drawable.rounds_open);
		} else {
			holder.iv.setImageResource(R.drawable.rounds_close);
		}
		return convertView;
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
		ChildHolder holder;
		if (convertView == null) {
			holder = new ChildHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_child, null);
			holder.date = (TextView) convertView.findViewById(R.id.child_name);
			holder.dataValue = (TextView) convertView.findViewById(R.id.child_sign);
			holder.ivDelete = convertView.findViewById(R.id.iv_delete_item);
			convertView.setTag(holder);
		} else {
			holder = (ChildHolder) convertView.getTag();
		}
        Log.d("Jerry.Xiao","children =3 ");
		ChildBean cb = list.get(groupPosition).getChildren().get(childPosition);
		holder.date.setText(cb.getdate());
		holder.dataValue.setText(cb.getdateValue());
		holder.ivDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                Log.d("Jerry.Xiao","children = 4");
               /* list.get(groupPosition).getChildren().remove(childPosition);
                notifyDataSetChanged();*/
				clickInview.click_delete(groupPosition,childPosition);
			}
		});
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	class GroupHolder {
		TextView title;
		ImageView iv;
	}

	class ChildHolder {
		TextView date, dataValue;
		ImageView ivDelete;
	}
	public interface  ClickInview
	{
		void click_delete(int groupPosition,int position);
	}
	public void setClickInview(ClickInview clickInview) {
		this.clickInview = clickInview;
	}
}