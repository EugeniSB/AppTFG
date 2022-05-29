package com.eugenisb.alphatest.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import com.eugenisb.alphatest.R;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequestsAdapter extends BaseAdapter implements Filterable {

    private List<String>originalData = null;
    private List<String>filteredData = null;
    private LayoutInflater mInflater;
    private RequestsAdapter.ItemFilter mFilter = new RequestsAdapter.ItemFilter();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId;
    private String username;
    private Map usersMap;

    public RequestsAdapter(Context context, List<String> data, Map usersMap, String username, String userId) {
        this.filteredData = data ;
        this.originalData = data ;
        this.usersMap = usersMap;
        this.userId = userId;
        this.username = username;
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

        RequestsAdapter.ViewHolder holder;


        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.contact_request_item, null);

            holder = new RequestsAdapter.ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.request_list_item_string);


            convertView.setTag(holder);
        } else {

            holder = (RequestsAdapter.ViewHolder) convertView.getTag();
        }

        ImageButton acceptBtn = (ImageButton)convertView.findViewById(R.id.accept_btn);
        ImageButton declineBtn = (ImageButton)convertView.findViewById(R.id.decline_btn);
        TextView usernameClicked = (TextView)convertView.findViewById(R.id.request_list_item_string);


        acceptBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String stringUserClicked = usernameClicked.getText().toString();
                //Toast.makeText(acceptBtn.getContext(), "Contact request sent to " + stringUserClicked, Toast.LENGTH_SHORT).show();
                originalData.remove(stringUserClicked);
                filteredData.remove(stringUserClicked);
                notifyDataSetChanged();
                acceptRequest(stringUserClicked);
            }
        });

        declineBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String stringUserClicked = usernameClicked.getText().toString();
                //Toast.makeText(declineBtn.getContext(), "Contact request sent to " + stringUserClicked, Toast.LENGTH_SHORT).show();
                originalData.remove(stringUserClicked);
                filteredData.remove(stringUserClicked);
                notifyDataSetChanged();
                declineRequest(stringUserClicked);
            }
        });

        holder.text.setText(filteredData.get(position));

        return convertView;
    }

    public void acceptRequest(String stringUserClicked){

        String stringUserIdClicked = (String) usersMap.get(stringUserClicked);

        db.collection("users").document(stringUserIdClicked).update(
                "contacts", FieldValue.arrayUnion(userId));
        db.collection("users").document(userId).update(
                "contacts", FieldValue.arrayUnion(stringUserIdClicked));

        /*
        db.collection("users").document(stringUserIdClicked).update(
                "contacts." + userId, username);
        db.collection("users").document(userId).update(
                "contacts." + stringUserIdClicked, stringUserClicked);

         */

        db.collection("users").document(stringUserIdClicked).update(
                "contactRequestsSent." + userId, FieldValue.delete());
        db.collection("users").document(userId).update(
                "contactRequestsSent." + stringUserIdClicked, FieldValue.delete());

        db.collection("users").document(stringUserIdClicked).update(
                "contactRequests." + userId, FieldValue.delete());
        db.collection("users").document(userId).update(
                "contactRequests." + stringUserIdClicked, FieldValue.delete());
    }

    public void declineRequest(String stringUserClicked){

        String stringUserIdClicked = (String) usersMap.get(stringUserClicked);

        db.collection("users").document(stringUserIdClicked).update(
                "contactRequestsSent." + userId, FieldValue.delete());
        db.collection("users").document(userId).update(
                "contactRequestsSent." + stringUserIdClicked, FieldValue.delete());

        db.collection("users").document(stringUserIdClicked).update(
                "contactRequests." + userId, FieldValue.delete());
        db.collection("users").document(userId).update(
                "contactRequests." + stringUserIdClicked, FieldValue.delete());

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
