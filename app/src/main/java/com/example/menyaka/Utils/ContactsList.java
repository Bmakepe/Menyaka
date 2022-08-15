package com.example.menyaka.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;

import com.example.menyaka.CountryIso2Phone;
import com.example.menyaka.Models.PhoneContacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class ContactsList {

    private List<PhoneContacts> contactsList = new ArrayList<>();
    private HashSet<String> mobileNoSet = new HashSet<>();
    public Context context;
    public String name, phone;

    public ContactsList(List<PhoneContacts> contactsList, Context context) {
        this.contactsList = contactsList;
        this.context = context;
    }

    public void readContacts(){
        try{
            contactsList.clear();
            String ISOPrefix = getCountryISO();

            @SuppressLint("Recycle") Cursor phones = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null);

            assert phones != null;
            while(phones.moveToNext()){
                name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String pic = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                String id = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

                phone = phone.replace(" ", "");
                phone = phone.replace("-", "");
                phone = phone.replace("(", "");
                phone = phone.replace(")", "");

                if(!String.valueOf(phone.charAt(0)).equals("+"))
                    phone = ISOPrefix + phone;

                if(!mobileNoSet.contains(phone)){
                    contactsList.add(new PhoneContacts(name, phone, pic, id));
                    mobileNoSet.add(phone);
                }

                Collections.sort(contactsList, new Comparator<PhoneContacts>() {
                    @Override
                    public int compare(PhoneContacts o1, PhoneContacts o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });

            }

        }catch (NullPointerException ignored){}
    }

    private String getCountryISO() {
        String iso = null;

        try{
            TelephonyManager telephonyManager = (TelephonyManager)context.getApplicationContext().getSystemService(context.getApplicationContext().TELEPHONY_SERVICE);
            if(telephonyManager.getNetworkCountryIso() != null){
                if(!telephonyManager.getNetworkCountryIso().toString().equals("")){
                    iso = telephonyManager.getNetworkCountryIso().toString();
                }
            }

        }catch (NullPointerException ignored){

        }

        return CountryIso2Phone.getPhone(iso);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PhoneContacts> getContactsList() {
        return Collections.unmodifiableList(contactsList);
    }
}
