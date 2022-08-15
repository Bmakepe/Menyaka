package com.example.menyaka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.craftman.cardform.Card;
import com.craftman.cardform.CardForm;
import com.craftman.cardform.OnPayBtnClickListner;

public class CardPaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_payment_layout);

        Intent intent = getIntent();
        String finalPrice = intent.getExtras().getString("finalPrice");

        CardForm cardForm = findViewById(R.id.card_form);

        cardForm.setAmount("M " + finalPrice);
        cardForm.setCardNameError("Enter valid card name details");
        cardForm.setCardNumberError("Enter valid card number details");
        cardForm.setCvcError("Incorrect CVC number");
        cardForm.setExpiryDateError("Enter the expiry shown on your card");

        cardForm.setPayBtnClickListner(new OnPayBtnClickListner() {
            @Override
            public void onClick(Card card) {
                Toast.makeText(CardPaymentActivity.this, "To be processed", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CardPaymentActivity.this, OrderReviewActivity.class));
            }
        });

        findViewById(R.id.cardPaymentBackBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}