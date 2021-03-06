package com.ehab.driverbroadcast.utils;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ehab.driverbroadcast.R;
import com.ehab.driverbroadcast.model.Driver;
import com.ehab.driverbroadcast.ui.ActivityLogin;
import com.ehab.driverbroadcast.ui.ProfileActivity;
import com.ehab.driverbroadcast.ui.TodayReservationsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

/**
 * Created by ehabhamdy on 7/3/17.
 */

public class NavigationDrawerUtil {
    Drawer drawer;
    AccountHeader headerResult;

    public static final String  SUB_LINE_EXTRA = "Line_Extra_Data";
    public static final String DATE_EXTRA = "Extra_Date";
    public static final String TIME_EXTRA = "Extra_Time";
    public static final String USERNAME_EXTRA = "Username_Data";
    public static final String EMAIL_EXTRA = "email_Data";

    public Drawer getDrawer() {
        return drawer;
    }

    public Drawer SetupNavigationDrawer(Toolbar mToolbar, final Activity activity , final Driver user) {
        // Create the AccountHeader

        headerResult = new AccountHeaderBuilder()
                .withActivity(activity)
                .withHeaderBackground(R.drawable.drawer_bg)
                .addProfiles(
                        new ProfileDrawerItem().withName(user.username).withEmail(user.email).withIcon(activity.getResources().getDrawable(R.drawable.toobar_icon))
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();

        //new DrawerBuilder().withActivity(this).withAccountHeader(headerResult).build();

        //if you want to update the items at a later time it is recommended to keep it in a variable
        PrimaryDrawerItem main = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.nav_main_label).withIcon(R.drawable.ic_room_black_24dp);
        PrimaryDrawerItem todayReservations = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.nav_reservations_label).withIcon(R.drawable.ic_trending_up_black_24dp);
        PrimaryDrawerItem profile = new PrimaryDrawerItem().withIdentifier(3).withName(R.string.nav_profile_label).withIcon(R.drawable.ic_person_black_24dp);
        PrimaryDrawerItem logout = new PrimaryDrawerItem().withIdentifier(5).withName(R.string.nav_logout_label).withIcon(R.drawable.ic_out_black_24dp);
        //create the drawer and remember the `Drawer` result object
        drawer = new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(mToolbar)
                .addDrawerItems(
                        main,
                        new DividerDrawerItem(),
                        todayReservations,
                        new DividerDrawerItem(),
                        profile,
                        new DividerDrawerItem(),
                        logout)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        switch (position) {
                            case 1:
                                drawer.closeDrawer();
                                return true;
                            case 3:
                                Intent openSubscriptionIntent = new Intent(activity.getApplicationContext(), TodayReservationsActivity.class);
                                openSubscriptionIntent.putExtra(DATE_EXTRA, user.date);
                                openSubscriptionIntent.putExtra(TIME_EXTRA, user.time);
                                activity.startActivity(openSubscriptionIntent);
                                drawer.closeDrawer();
                                return true;
                            case 5:
                                Intent openProfileIntent = new Intent(activity.getApplicationContext(), ProfileActivity.class);
                                openProfileIntent.putExtra(USERNAME_EXTRA, user.username);
                                openProfileIntent.putExtra(EMAIL_EXTRA, user.email);
                                openProfileIntent.putExtra(SUB_LINE_EXTRA, String.valueOf(user.line));
                                activity.startActivity(openProfileIntent);
                                drawer.closeDrawer();
                                return true;
                            case 7:
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(activity.getApplicationContext(), ActivityLogin.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                activity.startActivity(intent);
                                activity.finish();
                                break;

                        }
                        return true;
                    }
                })
                .withAccountHeader(headerResult)
                .build();
        return drawer;
    }
}
