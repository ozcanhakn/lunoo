package com.lumoo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.QueryProductDetailsResult;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.common.collect.ImmutableList;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.annotations.NonNull;

public class BuyCreditActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    // Aklınıza takılan değiştirilmesini istediğiniz başka bir yer var mı acaba? yoksa paketleyip yükleyelim play store'a


    TextView credit;
    ConstraintLayout onehundredCredit, fivehundredCredit, thousandCredit, thousandCredit1;

    TextView priceMigfer, priceAslan, priceKelebek, priceAskoKusko, priceSarmasik, priceHilal
            , priceElmas, priceGoril, priceAtes, priceMelek;

    // Ödeme entegrasyonu yapılacak...
    ConstraintLayout productMigder, productAslan, productKelebek, productAskoKusko, productSarmasik
            , productHilal, productElmas, productGoril, productAtes, productMelek;

    private BillingClient billingClient;
    private Map<String, ProductDetails> productDetailsMap = new HashMap<>();

    String currentId;

    TextView firstOptionsCoins,secondOptionsCoins,thirdOptionsCoins,fourthOptionsCoins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buy_credit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        credit = findViewById(R.id.diamondAmount);
        onehundredCredit = findViewById(R.id.diamond100);
        fivehundredCredit = findViewById(R.id.diamond300);
        thousandCredit = findViewById(R.id.diamond559);
        thousandCredit1 = findViewById(R.id.diamond859);

        // Çerçevelerin Fiyatlarını Tanımladık...
        priceMigfer = findViewById(R.id.txtMigferFramePrice);
        priceAslan = findViewById(R.id.txtAslanFramePrice);
        priceKelebek = findViewById(R.id.txtKelebekFramePrice);
        priceAskoKusko = findViewById(R.id.txtAskoKuskoFramePrice);
        priceSarmasik = findViewById(R.id.txtSarmasikFramePrice);
        priceHilal = findViewById(R.id.txtHilalFramePrice);
        priceElmas = findViewById(R.id.txtElmasFramePrice);
        priceGoril = findViewById(R.id.txtGorilFramePrice);
        priceAtes = findViewById(R.id.txtAtesFramePrice);
        priceMelek = findViewById(R.id.txtMelekFramePrice);

        // Çerçeve satın alma butonları
        productMigder = findViewById(R.id.frameMigfer);
        productAslan = findViewById(R.id.frameAslan);
        productKelebek = findViewById(R.id.frameKelebek);
        productAskoKusko = findViewById(R.id.frameAskoKusko);
        productSarmasik = findViewById(R.id.frameSarmasik);
        productHilal = findViewById(R.id.frameHilal);
        productElmas = findViewById(R.id.frameElmas);
        productGoril = findViewById(R.id.frameGoril);
        productAtes = findViewById(R.id.frameAtes);
        productMelek = findViewById(R.id.frameMelek);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        currentId = user.getUid();

        initFramesPrice();
        setupBillingClient();
        init();
        initFindview();

        thousandCredit1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPurchaseFlow("onehundred1");
            }
        });

        onehundredCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPurchaseFlow("onehundred");
            }
        });

        fivehundredCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPurchaseFlow("fivehundred");
            }
        });

        thousandCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPurchaseFlow("thousand");
            }
        });

        writeCreditAmount();
    }

    private void initFindview() {
        firstOptionsCoins = findViewById(R.id.firstOptionsCoins);
        secondOptionsCoins = findViewById(R.id.secondOptionsCoins);
        thirdOptionsCoins = findViewById(R.id.thirdOptionsCoins);
        fourthOptionsCoins = findViewById(R.id.fourthOptionsCoins);
    }

    private void setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
                .setListener(this)
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .enableAutoServiceReconnection() // Enable auto reconnection
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails();
                    queryPurchases(); // Query existing purchases on setup
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to Google Play
                Log.d("BillingClient", "Service disconnected");
            }
        });
    }

    private void queryProductDetails() {
        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();

        // Add all product IDs
        String[] productIds = {
                "onehundred", "fivehundred", "thousand", "onehundred1",
                "productGoril", "productMigfer", "productKelebek", "productElmas",
                "productHilal", "productMelek", "productAslan", "productAtes",
                "productSarmasik", "productAskoKusko"
        };

        for (String productId : productIds) {
            productList.add(
                    QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
            );
        }

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, new ProductDetailsResponseListener() {
            @Override
            public void onProductDetailsResponse(@NonNull BillingResult billingResult,
                                                 @NonNull QueryProductDetailsResult queryProductDetailsResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    for (ProductDetails productDetails : queryProductDetailsResult.getProductDetailsList()) {
                        productDetailsMap.put(productDetails.getProductId(), productDetails);
                        Log.d("ProductDetails", "Product: " + productDetails.getProductId()
                                + " Price: " + productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice());
                    }
                } else {
                    Log.e("ProductDetails", "Failed to query product details: " + billingResult.getDebugMessage());
                }
            }
        });
    }

    private void queryPurchases() {
        QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build();

        billingClient.queryPurchasesAsync(params, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult,
                                                 @NonNull List<Purchase> purchases) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    for (Purchase purchase : purchases) {
                        handlePurchase(purchase);
                    }
                }
            }
        });
    }

    private void startPurchaseFlow(String productId) {
        ProductDetails productDetails = productDetailsMap.get(productId);
        if (productDetails != null) {
            Log.d("PurchaseFlow", "Starting purchase flow for Product: " + productId);

            List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                    ImmutableList.of(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetails)
                                    .setOfferToken(productDetails.getOneTimePurchaseOfferDetails().getOfferToken())
                                    .build()
                    );

            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build();

            BillingResult billingResult = billingClient.launchBillingFlow(this, billingFlowParams);

            if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                Log.e("PurchaseFlow", "Failed to launch billing flow: " + billingResult.getDebugMessage());
            }
        } else {
            Log.e("PurchaseFlow", "ProductDetails is null for Product: " + productId);
            Toast.makeText(this, "Ürün bilgileri yüklenemedi, lütfen tekrar deneyin", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d("Purchase", "User canceled the purchase flow.");
            Toast.makeText(this, "Satın alma iptal edildi", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("Purchase", "Purchase failed with response code: " + billingResult.getResponseCode());
            Toast.makeText(this, "Satın alma başarısız: " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePurchase(Purchase purchase) {
        // Verify purchase first (implement your verification logic here)

        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            // Acknowledge the purchase if not already acknowledged
            if (!purchase.isAcknowledged()) {
                acknowledgePurchase(purchase.getPurchaseToken());
            }

            // Grant access to the purchased product
            grantAccessToProduct(purchase);
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            Toast.makeText(this, "Satın alma işlemi beklemede", Toast.LENGTH_SHORT).show();
        }
    }

    private void acknowledgePurchase(String purchaseToken) {
        AcknowledgePurchaseParams acknowledgePurchaseParams =
                AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchaseToken)
                        .build();

        billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d("Purchase", "Purchase acknowledged successfully");
                    Toast.makeText(BuyCreditActivity.this, "Satın alım başarılı", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("Purchase", "Failed to acknowledge purchase: " + billingResult.getDebugMessage());
                }
            }
        });
    }

    private void grantAccessToProduct(Purchase purchase) {
        List<String> productIds = purchase.getProducts();
        for (String productId : productIds) {
            switch (productId) {
                case "onehundred":
                    grantAccessToOneTimePlan();
                    break;
                case "fivehundred":
                    grantAccesToFiveHundredPlan();
                    break;
                case "thousand":
                    grantAccesToThousandPlan();
                    break;
                case "onehundred1":
                    grantAccessToOneTimePlan10();
                    break;
                case "productMelek":
                    grantAccessMelek();
                    break;
                case "productKelebek":
                    grantAccessKelebek();
                    break;
                case "productGoril":
                    grantAccessGoril();
                    break;
                case "productHilal":
                    grantAccessHilal();
                    break;
                case "productAtes":
                    grantAccessAtes();
                    break;
                case "productMigfer":
                    grantAccessMigfer();
                    break;
                case "productAslan":
                    grantAccessAslan();
                    break;
                case "productAskoKusko":
                    grantAccessAskoKusko();
                    break;
                case "productElmas":
                    grantAccessElmas();
                    break;
                case "productSarmasik":
                    grantAccessSarmasik();
                    break;
                default:
                    Log.w("Purchase", "Unknown product: " + productId);
            }
        }
    }

    private void grantAccessSarmasik() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        databaseReference.child("Kullanıcılar").child(uid).child("frameList")
                .push()
                .setValue("Sarmasik")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Sarmaşık çerçevesi eklendi!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Ekleme başarısız!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void grantAccessElmas() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        databaseReference.child("Kullanıcılar").child(uid).child("frameList")
                .push()
                .setValue("Elmas")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Elmas çerçevesi eklendi!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Ekleme başarısız!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void grantAccessAslan() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        databaseReference.child("Kullanıcılar").child(uid).child("frameList")
                .push()
                .setValue("Aslan")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Aslan çerçevesi eklendi!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Ekleme başarısız!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void grantAccessKelebek() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        databaseReference.child("Kullanıcılar").child(uid).child("frameList")
                .push()
                .setValue("Kelebek")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Kelebek çerçevesi eklendi!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Ekleme başarısız!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void grantAccessAtes() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        databaseReference.child("Kullanıcılar").child(uid).child("frameList")
                .push()
                .setValue("Ates")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Ateş çerçevesi eklendi!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Ekleme başarısız!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void grantAccessHilal() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        databaseReference.child("Kullanıcılar").child(uid).child("frameList")
                .push()
                .setValue("Hilal")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Hilal çerçevesi eklendi!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Ekleme başarısız!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void grantAccessGoril() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        databaseReference.child("Kullanıcılar").child(uid).child("frameList")
                .push()
                .setValue("Goril")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Goril çerçevesi eklendi!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Ekleme başarısız!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void grantAccessMigfer() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        databaseReference.child("Kullanıcılar").child(uid).child("frameList")
                .push()
                .setValue("Migfer")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Miğfer çerçevesi eklendi!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Ekleme başarısız!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void grantAccessAskoKusko() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        databaseReference.child("Kullanıcılar").child(uid).child("frameList")
                .push()
                .setValue("AskoKusko")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Aşko-Kuşko çerçevesi eklendi!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Ekleme başarısız!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void grantAccessMelek() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        databaseReference.child("Kullanıcılar").child(uid).child("frameList")
                .push()
                .setValue("Melek")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "MELEK çerçevesi eklendi!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Ekleme başarısız!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void grantAccessToOneTimePlan10() {
        Log.d("ABONE1", "grantAccessToOneTimePlan: metot çalıştı");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = user.getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("serviceProviders").child(currentUserId);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer creditAmount = snapshot.child("credit").getValue(Integer.class);
                    if (creditAmount != null) {
                        int newCreditAmount = creditAmount + 1100;
                        reference.child("credit").setValue(newCreditAmount);
                        Toast.makeText(BuyCreditActivity.this, "Bakiyeniz Başarıyla Yüklendi", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Database error: " + error.getMessage());
            }
        });
    }

    private void grantAccesToThousandPlan() {
        Log.d("ABONE1", "grantAccesToThousandPlan: metot çalıştı");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = user.getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("serviceProviders").child(currentUserId);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer creditAmount = snapshot.child("credit").getValue(Integer.class);
                    if (creditAmount != null) {
                        int newCreditAmount = creditAmount + 550;
                        reference.child("credit").setValue(newCreditAmount);
                        Toast.makeText(BuyCreditActivity.this, "Bakiyeniz Başarıyla Yüklendi", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Database error: " + error.getMessage());
            }
        });
    }

    private void grantAccesToFiveHundredPlan() {
        Log.d("ABONE1", "grantAccesToFiveHundredPlan: metot çalıştı");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = user.getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("serviceProviders").child(currentUserId);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer creditAmount = snapshot.child("credit").getValue(Integer.class);
                    if (creditAmount != null) {
                        int newCreditAmount = creditAmount + 120;
                        reference.child("credit").setValue(newCreditAmount);
                        Toast.makeText(BuyCreditActivity.this, "Bakiyeniz Başarıyla Yüklendi", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Database error: " + error.getMessage());
            }
        });
    }

    private void grantAccessToOneTimePlan() {
        Log.d("ABONE1", "grantAccessToOneTimePlan: metot çalıştı");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = user.getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("serviceProviders").child(currentUserId);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer creditAmount = snapshot.child("credit").getValue(Integer.class);
                    if (creditAmount != null) {
                        int newCreditAmount = creditAmount + 20;
                        reference.child("credit").setValue(newCreditAmount);
                        Toast.makeText(BuyCreditActivity.this, "Bakiyeniz Başarıyla Yüklendi", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Database error: " + error.getMessage());
            }
        });
    }

    public void init() {
        productAslan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPurchaseFlow("productAslan");
            }
        });

        productMelek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPurchaseFlow("productMelek");
            }
        });

        productMigder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPurchaseFlow("productMigfer");
            }
        });

        productKelebek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPurchaseFlow("productKelebek");
            }
        });

        productAtes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPurchaseFlow("productAtes");
            }
        });

        productAskoKusko.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPurchaseFlow("productAskoKusko");
            }
        });

        productHilal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPurchaseFlow("productHilal");
            }
        });

        productElmas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPurchaseFlow("productElmas");
            }
        });

        productSarmasik.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPurchaseFlow("productSarmasik");
            }
        });

        productGoril.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPurchaseFlow("productGoril");
            }
        });
    }

    public void writeCreditAmount() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("serviceProviders").child(currentId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer credit1 = snapshot.child("credit").getValue(Integer.class);
                    if (credit1 != null) {
                        credit.setText(String.valueOf(credit1));
                    } else {
                        credit.setText("0");
                    }
                } else {
                    credit.setText("0");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Database error: " + error.getMessage());
            }
        });
    }

    public void initFramesPrice() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Frames");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                String gorilFiyati = snapshot.child("goril").child("price").getValue(String.class);
                String kelebekFiyati = snapshot.child("kelebek").child("price").getValue(String.class);
                String migferFiyati = snapshot.child("migfer").child("price").getValue(String.class);
                String elmasFiyati = snapshot.child("elmas").child("price").getValue(String.class);
                String atesFiyati = snapshot.child("ates").child("price").getValue(String.class);
                String hilalFiyati = snapshot.child("hilal").child("price").getValue(String.class);
                String melekFiyati = snapshot.child("melek").child("price").getValue(String.class);
                String askokuskoFiyati = snapshot.child("askokusko").child("price").getValue(String.class);
                String aslanFiyati = snapshot.child("aslan").child("price").getValue(String.class);
                String sarmasikFiyati = snapshot.child("sarmasik").child("price").getValue(String.class);
                String firstOptionsCoin = snapshot.child("coin1").child("price").getValue(String.class);
                String secondOptionsCoin = snapshot.child("coin2").child("price").getValue(String.class);
                String thirdOptionsCoin = snapshot.child("coin3").child("price").getValue(String.class);
                String fourthOptionsCoin = snapshot.child("coin4").child("price").getValue(String.class);


                if (aslanFiyati != null) priceAslan.setText(aslanFiyati);
                if (migferFiyati != null) priceMigfer.setText(migferFiyati);
                if (kelebekFiyati != null) priceKelebek.setText(kelebekFiyati);
                if (atesFiyati != null) priceAtes.setText(atesFiyati);
                if (gorilFiyati != null) priceGoril.setText(gorilFiyati);
                if (sarmasikFiyati != null) priceSarmasik.setText(sarmasikFiyati);
                if (melekFiyati != null) priceMelek.setText(melekFiyati);
                if (hilalFiyati != null) priceHilal.setText(hilalFiyati);
                if (elmasFiyati != null) priceElmas.setText(elmasFiyati);
                if (askokuskoFiyati != null) priceAskoKusko.setText(askokuskoFiyati);
                if (firstOptionsCoin != null) firstOptionsCoins.setText(firstOptionsCoin);
                if (secondOptionsCoin != null) secondOptionsCoins.setText(secondOptionsCoin);
                if (thirdOptionsCoin != null) thirdOptionsCoins.setText(thirdOptionsCoin);
                if (fourthOptionsCoin != null) fourthOptionsCoins.setText(fourthOptionsCoin);
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                Log.e("Firebase", "Database error in initFramesPrice: " + error.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }
}