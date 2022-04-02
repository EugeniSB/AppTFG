package com.eugenisb.alphatest;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchableAdapter extends BaseAdapter implements Filterable {

    private List<String>originalData = null;
    private List<String>filteredData = null;
    private LayoutInflater mInflater;
    private ItemFilter mFilter = new ItemFilter();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userEmail;
    private Map usersMap;

    public SearchableAdapter(Context context, List<String> data, Map usersMap, String userEmail) {
        this.filteredData = data ;
        this.originalData = data ;
        this.usersMap = usersMap;
        this.userEmail = userEmail;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return filteredData.size();
    }

    public Object getItem(int position) {
        return filteredData.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unnecessary calls
        // to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.contactlist_item, null);
            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.list_item_string);

            // Bind the data efficiently with the holder.

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        Button addBtn = (Button)convertView.findViewById(R.id.add_btn);
        TextView usernameClicked = (TextView)convertView.findViewById(R.id.list_item_string);

        addBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //do something
                String stringUserClicked = usernameClicked.getText().toString();
                Toast.makeText(addBtn.getContext(), "Contact request sent to " + stringUserClicked, Toast.LENGTH_SHORT).show();
                originalData.remove(stringUserClicked);
                filteredData.remove(stringUserClicked);
                notifyDataSetChanged();
                sendRequest(stringUserClicked);
            }
        });

        // If weren't re-ordering this you could rely on what you set last time
        holder.text.setText(filteredData.get(position));

        return convertView;
    }

    public void sendRequest(String stringUserClicked){

        String stringEmailClicked = (String) usersMap.get(stringUserClicked);
        Log.d(TAG, stringEmailClicked);

    }

    static class ViewHolder {
        TextView text;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<String> list = originalData;

            int count = list.size();
            final ArrayList<String> nlist = new ArrayList<String>(count);

            String filterableString ;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i);
                if (filterableString.toLowerCase().contains(filterString)) {
                    nlist.add(filterableString);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<String>) results.values;
            notifyDataSetChanged();
        }

    }
}