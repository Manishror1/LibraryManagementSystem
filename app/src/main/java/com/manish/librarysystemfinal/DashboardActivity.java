package com.manish.librarysystemfinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;

    TextView tvWelcome, tvRoleTitle, tvAdminCommission;

    // Admin Analytics
    TextView tvTotalUsers, tvTotalBooks, tvPendingBooks, tvApprovedBooks,
            tvRejectedBooks, tvTotalFines, tvUnpaidFines;

    // Admin earnings text
    TextView tvAdminTotalEarnings;

    // Vendor earnings text
    TextView tvVendorEarnings;

    Button btnAction1, btnAction2, btnAction3, btnAction4, btnLogout;

    // Layout Sections
    LinearLayout layoutAdminManageUsers, layoutAdminAnalytics, layoutAdminEarnings;
    LinearLayout layoutLibrarian, layoutVendor, layoutStudent;

    RecyclerView recyclerAdminUsers;
    RecyclerView recyclerStudentBorrowed;

    // Librarian shared recycler (books / fines)
    RecyclerView recyclerLibrarian;
    ArrayList<Book> librarianBooks = new ArrayList<>();
    LibrarianBookAdapter librarianBookAdapter;

    ArrayList<StudentFine> librarianFines = new ArrayList<>();
    LibrarianFineAdapter librarianFineAdapter;

    // Admin users list
    ArrayList<UserModel> userList = new ArrayList<>();
    UserAdapter userAdapter;

    // Vendor rejected list
    RecyclerView recyclerVendorRejected;

    String currentRole = "guest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Basic UI
        tvWelcome = findViewById(R.id.tvWelcome);
        tvRoleTitle = findViewById(R.id.tvRoleTitle);
        tvAdminCommission = findViewById(R.id.tvAdminCommission);

        btnAction1 = findViewById(R.id.btnAction1);
        btnAction2 = findViewById(R.id.btnAction2);
        btnAction3 = findViewById(R.id.btnAction3);
        btnAction4 = findViewById(R.id.btnAction4);
        btnLogout = findViewById(R.id.btnLogout);

        // Sections
        layoutAdminManageUsers = findViewById(R.id.layoutAdminManageUsers);
        layoutAdminAnalytics = findViewById(R.id.layoutAdminAnalytics);
        layoutAdminEarnings = findViewById(R.id.layoutAdminEarnings);
        layoutLibrarian = findViewById(R.id.layoutLibrarian);
        layoutVendor = findViewById(R.id.layoutVendor);
        layoutStudent = findViewById(R.id.layoutStudent);

        // Admin Analytics TextViews
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalBooks = findViewById(R.id.tvTotalBooks);
        tvPendingBooks = findViewById(R.id.tvPendingBooks);
        tvApprovedBooks = findViewById(R.id.tvApprovedBooks);
        tvRejectedBooks = findViewById(R.id.tvRejectedBooks);
        tvTotalFines = findViewById(R.id.tvTotalFines);
        tvUnpaidFines = findViewById(R.id.tvUnpaidFines);

        // Earnings TextViews
        tvAdminTotalEarnings = findViewById(R.id.tvAdminTotalEarnings);
        tvVendorEarnings = findViewById(R.id.tvVendorEarnings);

        // Recyclers
        recyclerAdminUsers = findViewById(R.id.recyclerAdminUsers);
        recyclerAdminUsers.setLayoutManager(new LinearLayoutManager(this));

        recyclerStudentBorrowed = findViewById(R.id.recyclerStudentBorrowed);
        recyclerStudentBorrowed.setLayoutManager(new LinearLayoutManager(this));

        recyclerLibrarian = findViewById(R.id.recyclerLibrarianBooks);
        recyclerLibrarian.setLayoutManager(new LinearLayoutManager(this));

        // Vendor rejected list recycler
        recyclerVendorRejected = findViewById(R.id.recyclerVendorRejected);
        if (recyclerVendorRejected != null) {
            recyclerVendorRejected.setLayoutManager(new LinearLayoutManager(this));
        }

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        });

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserRole();
    }

    private void loadUserRole() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String name = doc.getString("name");
                    currentRole = doc.getString("role");

                    tvWelcome.setText("Welcome, " + name + " 👋");
                    tvRoleTitle.setText("Role: " + currentRole);

                    if (currentRole.equalsIgnoreCase("admin")) {
                        tvAdminCommission.setVisibility(View.VISIBLE);
                        loadAdminCommission();
                    } else {
                        tvAdminCommission.setVisibility(View.GONE);
                    }

                    setButtonsForRole(currentRole);
                });
    }

    private void setButtonsForRole(String role) {

        hideAllSections();

        // ------------------ ADMIN ------------------
        if (role.equalsIgnoreCase("admin")) {

            btnAction1.setText("Manage Users");
            btnAction2.setText("Analytics");
            btnAction3.setText("Earnings");
            btnAction4.setVisibility(View.GONE);

            btnAction1.setOnClickListener(v -> {
                hideAllSections();
                layoutAdminManageUsers.setVisibility(View.VISIBLE);
                loadAllUsers();
            });

            btnAction2.setOnClickListener(v -> {
                hideAllSections();
                layoutAdminAnalytics.setVisibility(View.VISIBLE);
                loadAdminAnalytics();
            });

            btnAction3.setOnClickListener(v -> {
                hideAllSections();
                layoutAdminEarnings.setVisibility(View.VISIBLE);
                loadAdminEarnings();
            });
        }

        // ------------------ LIBRARIAN ------------------
        else if (role.equalsIgnoreCase("librarian")) {

            btnAction1.setText("All Books");
            btnAction2.setText("Pending");
            btnAction3.setText("Fines");
            btnAction4.setText("Return Requests");

            btnAction4.setVisibility(View.VISIBLE);

            btnAction1.setOnClickListener(v -> showLibrarianBooks("all"));
            btnAction2.setOnClickListener(v -> showLibrarianBooks("pending"));
            btnAction3.setOnClickListener(v -> showLibrarianFines());

            btnAction4.setOnClickListener(v ->
                    startActivity(new Intent(DashboardActivity.this,
                            LibrarianApproveRequestsActivity.class))
            );
        }

        // ------------------ VENDOR ------------------
        else if (role.equalsIgnoreCase("vendor")) {

            btnAction1.setText("Add Book");
            btnAction2.setText("My Books");
            btnAction3.setText("Rejected Books");
            btnAction4.setText("Earnings");
            btnAction4.setVisibility(View.VISIBLE);



            btnAction1.setOnClickListener(v ->
                    startActivity(new Intent(DashboardActivity.this, VendorAddBookActivity.class)));

            btnAction2.setOnClickListener(v ->
                    startActivity(new Intent(DashboardActivity.this, VendorMyBooksActivity.class)));

            btnAction3.setOnClickListener(v -> {
                hideAllSections();
                layoutVendor.setVisibility(View.VISIBLE);
                loadVendorRejectedBooks();

            });
            btnAction4.setOnClickListener(v -> {
                hideAllSections();
                layoutVendor.setVisibility(View.VISIBLE);
                loadVendorEarnings();

            });
        }

        // ------------------ STUDENT ------------------
        else if (role.equalsIgnoreCase("student")) {

            btnAction1.setText("Borrowed Books");
            btnAction2.setText("My Fines");
            btnAction3.setText("Return Book");
            btnAction4.setVisibility(View.GONE);

            btnAction1.setOnClickListener(v -> showStudentBorrowedBooks());
            btnAction2.setOnClickListener(v -> showStudentFines());
            btnAction3.setOnClickListener(v -> showStudentBorrowedBooks());
        }
    }

    // ---------- LOAD REJECTED BOOKS (VENDOR) ----------
    private void loadVendorRejectedBooks() {

        if (recyclerVendorRejected == null) return;

        ArrayList<RejectedBook> rejectedList = new ArrayList<>();
        RejectedBooksAdapter adapter = new RejectedBooksAdapter(this, rejectedList);
        recyclerVendorRejected.setAdapter(adapter);

        String vendorId = auth.getCurrentUser().getUid();

        new Thread(() -> {
            List<RejectedBook> data = AppDatabase
                    .getInstance(this)
                    .rejectedBookDao()
                    .getRejectedBooksByVendor(vendorId);

            runOnUiThread(() -> {
                rejectedList.clear();
                rejectedList.addAll(data);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    // ---------- LIBRARIAN: BOOKS ----------
    private void showLibrarianBooks(String statusFilter) {

        hideAllSections();
        layoutLibrarian.setVisibility(View.VISIBLE);

        if (librarianBookAdapter == null) {
            librarianBooks = new ArrayList<>();
            librarianBookAdapter = new LibrarianBookAdapter(this, librarianBooks);
        }
        recyclerLibrarian.setAdapter(librarianBookAdapter);

        Query q;

        if ("all".equalsIgnoreCase(statusFilter)) {
            q = db.collection("books")
                    .whereEqualTo("status", "approved");
        } else if ("pending".equalsIgnoreCase(statusFilter)) {
            q = db.collection("books")
                    .whereEqualTo("status", "pending");
        } else {
            q = db.collection("books");
        }

        q.get().addOnSuccessListener(snaps -> {
            librarianBooks.clear();

            for (DocumentSnapshot d : snaps) {
                Book b = d.toObject(Book.class);
                if (b != null) {
                    b.id = d.getId();
                    librarianBooks.add(b);
                }
            }

            librarianBookAdapter.notifyDataSetChanged();

            if (librarianBooks.isEmpty()) {
                Toast.makeText(this, "No books for this filter.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------- LIBRARIAN: FINES ----------
    private void showLibrarianFines() {

        hideAllSections();
        layoutLibrarian.setVisibility(View.VISIBLE);

        if (librarianFineAdapter == null) {
            librarianFines = new ArrayList<>();
            librarianFineAdapter = new LibrarianFineAdapter(this, librarianFines);
        }
        recyclerLibrarian.setAdapter(librarianFineAdapter);

        db.collection("fines").get().addOnSuccessListener(snaps -> {
            librarianFines.clear();

            for (DocumentSnapshot d : snaps) {
                StudentFine f = d.toObject(StudentFine.class);
                if (f != null) {
                    f.id = d.getId();
                    librarianFines.add(f);
                }
            }

            librarianFineAdapter.notifyDataSetChanged();
        });
    }

    // ---------- STUDENT: BORROWED BOOKS ----------
    private void showStudentBorrowedBooks() {

        hideAllSections();
        layoutStudent.setVisibility(View.VISIBLE);

        AppDatabase localDb = AppDatabase.getInstance(this);
        List<BorrowedBook> list = localDb.borrowedBookDao().getAll();

        BorrowedBooksListAdapter adapter = new BorrowedBooksListAdapter(this, list);
        recyclerStudentBorrowed.setAdapter(adapter);
    }

    // ---------- STUDENT: FINES ----------
    private void showStudentFines() {

        hideAllSections();
        layoutStudent.setVisibility(View.VISIBLE);

        RecyclerView recycler = findViewById(R.id.recyclerStudentFines);
        TextView tvNoFines = findViewById(R.id.tvStudentNoFines);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<StudentFine> fineList = new ArrayList<>();
        StudentFineAdapter adapter = new StudentFineAdapter(this, fineList);
        recycler.setAdapter(adapter);

        String uid = auth.getCurrentUser().getUid();

        db.collection("fines").whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(snaps -> {

                    fineList.clear();
                    for (DocumentSnapshot d : snaps) {
                        StudentFine f = d.toObject(StudentFine.class);
                        if (f != null) fineList.add(f);
                    }

                    adapter.notifyDataSetChanged();

                    if (fineList.isEmpty()) {
                        tvNoFines.setVisibility(View.VISIBLE);
                        recycler.setVisibility(View.GONE);
                    } else {
                        tvNoFines.setVisibility(View.GONE);
                        recycler.setVisibility(View.VISIBLE);
                    }
                });
    }

    // ---------- ADMIN: USERS ----------
    private void loadAllUsers() {
        db.collection("users").get().addOnSuccessListener(snaps -> {
            userList.clear();

            for (QueryDocumentSnapshot d : snaps) {
                UserModel u = d.toObject(UserModel.class);
                u.uid = d.getId();
                userList.add(u);
            }

            userAdapter = new UserAdapter(this, userList, true);
            recyclerAdminUsers.setAdapter(userAdapter);
        });
    }

    // ---------- ADMIN: ANALYTICS ----------
    private void loadAdminAnalytics() {

        // Total users
        db.collection("users").get().addOnSuccessListener(s ->
                tvTotalUsers.setText(String.valueOf(s.size()))
        );

        // Total books
        db.collection("books").get().addOnSuccessListener(s ->
                tvTotalBooks.setText(String.valueOf(s.size()))
        );

        // Pending books
        db.collection("books").whereEqualTo("status", "pending")
                .get().addOnSuccessListener(s ->
                        tvPendingBooks.setText(String.valueOf(s.size()))
                );

        db.collection("books").whereEqualTo("status", "approved")
                .get().addOnSuccessListener(s ->
                        tvApprovedBooks.setText(String.valueOf(s.size()))
                );

        db.collection("books").whereEqualTo("status", "rejected")
                .get().addOnSuccessListener(s ->
                        tvRejectedBooks.setText(String.valueOf(s.size()))
                );


        // Fines: total and unpaid, and show unpaid in card
        db.collection("fines").get().addOnSuccessListener(snaps -> {

            double total = 0;
            double unpaid = 0;

            for (DocumentSnapshot d : snaps) {
                StudentFine f = d.toObject(StudentFine.class);
                if (f == null) continue;

                total += f.amount;
                if (!f.paid) {
                    unpaid += f.amount;
                }
            }

            // These three are text-only helpers (you kept them hidden)
            tvTotalFines.setText("$" + total);
            tvApprovedBooks.setText(""); // not used now
            tvRejectedBooks.setText(""); // not used now

            // This one is inside the card
            tvUnpaidFines.setText("$" + unpaid);
        });
    }

    // ---------- ADMIN: Commission label at top ----------
    private void loadAdminCommission() {
        db.collection("commission").document("admin")
                .get()
                .addOnSuccessListener(doc -> {
                    Double amount = doc.getDouble("total");
                    if (amount == null) amount = 0.0;
                    tvAdminCommission.setText("Commission: ₹" + amount);
                });
    }

    // ---------- ADMIN: Earnings panel ----------
    private void loadAdminEarnings() {

        tvAdminTotalEarnings.setText("Loading…");

        db.collection("fines").whereEqualTo("paid", true)
                .get()
                .addOnSuccessListener(snaps -> {

                    double paidFines = 0;
                    for (DocumentSnapshot d : snaps) {
                        StudentFine f = d.toObject(StudentFine.class);
                        if (f != null) paidFines += f.amount;
                    }

                    double finalPaidFines = paidFines;

                    db.collection("commission").document("admin")
                            .get()
                            .addOnSuccessListener(doc -> {
                                Double commission = doc.getDouble("total");
                                if (commission == null) commission = 0.0;

                                double totalEarnings = finalPaidFines + commission;
                                tvAdminTotalEarnings.setText(
                                        "Total Earnings (Paid fines + Commission): ₹" + totalEarnings
                                );
                            })
                            .addOnFailureListener(e -> {
                                tvAdminTotalEarnings.setText("Error loading commission");
                            });

                })
                .addOnFailureListener(e -> tvAdminTotalEarnings.setText("Error loading fines"));
    }

    // ---------- VENDOR: Earnings ----------
    private void loadVendorEarnings() {
        if (tvVendorEarnings == null) return;

        String vendorId = auth.getCurrentUser().getUid();

        db.collection("vendor_earnings").document(vendorId)
                .get()
                .addOnSuccessListener(doc -> {
                    Double total = doc.getDouble("total");
                    if (total == null) total = 0.0;
                    tvVendorEarnings.setText("My Earnings: ₹" + total);
                    tvVendorEarnings.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    tvVendorEarnings.setText("My Earnings: ₹0");
                    tvVendorEarnings.setVisibility(View.VISIBLE);
                });
    }

    // ---------- HIDE ALL ----------
    private void hideAllSections() {
        layoutAdminManageUsers.setVisibility(View.GONE);
        layoutAdminAnalytics.setVisibility(View.GONE);
        layoutAdminEarnings.setVisibility(View.GONE);
        layoutLibrarian.setVisibility(View.GONE);
        layoutVendor.setVisibility(View.GONE);
        layoutStudent.setVisibility(View.GONE);
    }
}
