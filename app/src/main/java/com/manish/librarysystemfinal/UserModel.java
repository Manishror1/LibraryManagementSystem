package com.manish.librarysystemfinal;

public class UserModel {
    public String uid;
    public String name;
    public String email;
    public String role;      // admin, librarian, vendor, student, pendingLibrarian, pendingVendor
    public Boolean approved; // can be null

    public UserModel() {} // Firestore needs empty constructor

    public UserModel(String uid, String name, String email, String role, Boolean approved) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
        this.approved = approved;
    }
}
