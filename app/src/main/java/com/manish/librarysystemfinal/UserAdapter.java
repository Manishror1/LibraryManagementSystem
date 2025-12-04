package com.manish.librarysystemfinal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {

    Context context;
    ArrayList<UserModel> list;
    boolean adminMode;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public UserAdapter(Context context, ArrayList<UserModel> list, boolean adminMode) {
        this.context = context;
        this.list = list;
        this.adminMode = adminMode;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.user_manage_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder h, int position) {

        UserModel u = list.get(position);

        h.txtName.setText(u.name);
        h.txtEmail.setText(u.email);

        ArrayList<String> roles = new ArrayList<>();
        roles.add("admin");
        roles.add("student");
        roles.add("librarian");
        roles.add("vendor");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_dropdown_item, roles);
        h.spinnerRole.setAdapter(adapter);

        int index = roles.indexOf(u.role);
        if (index >= 0) h.spinnerRole.setSelection(index);

        // show approve button only if pending role
        if ("pendingLibrarian".equals(u.role)) {
            h.btnApprove.setVisibility(View.VISIBLE);
            h.btnApprove.setText("Approve Librarian");
        } else if ("pendingVendor".equals(u.role)) {
            h.btnApprove.setVisibility(View.VISIBLE);
            h.btnApprove.setText("Approve Vendor");
        } else {
            h.btnApprove.setVisibility(View.GONE);
        }

        h.btnApprove.setOnClickListener(v -> {
            String approvedRole = "pendingLibrarian".equals(u.role) ? "librarian" : "vendor";
            db.collection("users").document(u.uid)
                    .update("role", approvedRole, "approved", true)
                    .addOnSuccessListener(a ->
                            Toast.makeText(context, "Approved!", Toast.LENGTH_SHORT).show());
        });

        h.spinnerRole.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int i, long l) {
                String newRole = roles.get(i);
                if (!newRole.equals(u.role)) {
                    db.collection("users").document(u.uid)
                            .update("role", newRole)
                            .addOnSuccessListener(a ->
                                    Toast.makeText(context, "Role updated", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> adapterView) {}
        });

        h.btnDelete.setOnClickListener(v -> {
            db.collection("users").document(u.uid)
                    .delete()
                    .addOnSuccessListener(a ->
                            Toast.makeText(context, "User deleted", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtEmail;
        Spinner spinnerRole;
        Button btnApprove, btnDelete;

        MyViewHolder(View v) {
            super(v);
            txtName = v.findViewById(R.id.txtName);
            txtEmail = v.findViewById(R.id.txtEmail);
            spinnerRole = v.findViewById(R.id.spinnerRole);
            btnApprove = v.findViewById(R.id.btnApprove);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
