package com.example.tp;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends BaseActivity {

    private static final List<String> notifications = new ArrayList<>();
    /** Counts messages added since the user last opened NotificationActivity */
    private static int unreadCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Mark all as read when screen opens
        unreadCount = 0;

        RecyclerView rv = findViewById(R.id.rvNotifications);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(this));
            rv.setAdapter(new NotificationAdapter(notifications));
        }

        if (findViewById(R.id.btnBackNotif) != null)
            findViewById(R.id.btnBackNotif).setOnClickListener(v -> finish());

        if (findViewById(R.id.btnContinueNotif) != null)
            findViewById(R.id.btnContinueNotif).setOnClickListener(v -> finish());
    }

    /** Called from anywhere to add a notification and bump the badge. */
    public static void addNotification(String message) {
        notifications.add(0, message);
        unreadCount++;
    }

    /** Returns the current number of unread notifications (resets on open). */
    public static int getUnreadCount() {
        return unreadCount;
    }
}
