package com.example.mobilalkfejlprojekt;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;

public class CartActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "order_notification_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final int ALARM_PERMISSION_REQUEST_CODE = 456;
    
    private RecyclerView recyclerView;
    private TextView emptyCartTextView;
    private TextView totalPriceTextView;
    private Button orderButton;
    private CartAdapter adapter;
    private ArrayList<Item> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        createNotificationChannel();
        checkNotificationPermission();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Kosár");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.cartRecyclerView);
        emptyCartTextView = findViewById(R.id.emptyCartTextView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        orderButton = findViewById(R.id.orderButton);

        cartItems = new ArrayList<>();
        adapter = new CartAdapter(this, cartItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ArrayList<Item> receivedItems = (ArrayList<Item>) getIntent().getSerializableExtra("cartItems");
        if (receivedItems != null) {
            cartItems.addAll(receivedItems);
            adapter.notifyDataSetChanged();
        }

        updateCartView();

        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!cartItems.isEmpty()) {
                    if (checkNotificationPermission()) {
                        if (checkAlarmPermission()) {
                            sendOrderNotification();
                            scheduleOrderStatusUpdates();
                            clearCart();
                        } else {
                            requestAlarmPermission();
                        }
                    } else {
                        Toast.makeText(CartActivity.this, 
                            "Az értesítések engedélyezése szükséges a megrendeléshez", 
                            Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Értesítések engedélyezve", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Értesítések engedélyezése szükséges", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean checkAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            return alarmManager.canScheduleExactAlarms();
        }
        return true;
    }

    private void requestAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivityForResult(intent, ALARM_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ALARM_PERMISSION_REQUEST_CODE) {
            if (checkAlarmPermission()) {
                sendOrderNotification();
                scheduleOrderStatusUpdates();
                clearCart();
            } else {
                Toast.makeText(this, 
                    "A pontos időzítés engedélyezése szükséges a megrendeléshez", 
                    Toast.LENGTH_LONG).show();
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Order Notifications";
            String description = "Notifications for order status";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendOrderNotification() {
        Intent intent = new Intent(this, MainLoggedInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Rendelés")
                .setContentText("Rendelés sikeresen leadva")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void clearCart() {
        cartItems.clear();
        adapter.notifyDataSetChanged();
        updateCartView();
        Toast.makeText(this, "Rendelés sikeresen leadva", Toast.LENGTH_SHORT).show();
        
        Intent resultIntent = new Intent();
        resultIntent.putExtra("cartItems", new ArrayList<Item>());
        setResult(RESULT_OK, resultIntent);
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("cartItems", cartItems);
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }

    private void updateCartView() {
        if (cartItems.isEmpty()) {
            emptyCartTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            totalPriceTextView.setVisibility(View.GONE);
            orderButton.setVisibility(View.GONE);
        } else {
            emptyCartTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            totalPriceTextView.setVisibility(View.VISIBLE);
            orderButton.setVisibility(View.VISIBLE);

            int totalPrice = 0;
            for (Item item : cartItems) {
                String priceStr = item.getPrice().replace(" Flurbo", "").trim();
                totalPrice += Integer.parseInt(priceStr);
            }
            totalPriceTextView.setText("Összesen: " + totalPrice + " Flurbo");
        }
    }

    private void scheduleOrderStatusUpdates() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        
        //Kis időtartam tesztelhetőség miatt
        scheduleStatusUpdate(alarmManager, 5, "Rendelésedet elkezdtük készíteni");
        scheduleStatusUpdate(alarmManager, 10, "Rendelésed elkészült");
        scheduleStatusUpdate(alarmManager, 15, "Rendelésedet a futár felvette");
        scheduleStatusUpdate(alarmManager, 20, "Rendelésed megérkezett");
    }

    private void scheduleStatusUpdate(AlarmManager alarmManager, int seconds, String message) {
        Intent intent = new Intent(this, OrderStatusReceiver.class);
        intent.putExtra("message", message);
        intent.putExtra("notificationId", seconds);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, 
            seconds,
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, seconds);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
                );
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
            );
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 