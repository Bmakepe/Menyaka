package com.example.menyaka;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.menyaka.Adapters.PhoneContactsAdapter;
import com.example.menyaka.Models.PhoneContacts;
import com.example.menyaka.Utils.ContactsList;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    TextView header;
    RecyclerView contactsRecycler;

    List<PhoneContacts> contactsList;
    ContactsList phoneContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view_layout);

        contactsRecycler = findViewById(R.id.recycler_View);
        header = findViewById(R.id.recyclerHeader);
        header.setText("Contacts");

        contactsRecycler.setHasFixedSize(true);
        contactsRecycler.hasFixedSize();
        contactsRecycler.setLayoutManager(new LinearLayoutManager(this));

        contactsList = new ArrayList<>();
        phoneContacts = new ContactsList(contactsList, this);

        phoneContacts.readContacts();

        PhoneContactsAdapter contactsAdapter = new PhoneContactsAdapter(contactsList, this);
        contactsRecycler.setAdapter(contactsAdapter);

        findViewById(R.id.recyclerBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}