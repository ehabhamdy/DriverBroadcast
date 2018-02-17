package com.ehab.driverbroadcast.model;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ehab.driverbroadcast.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ehabhamdy on 2/17/18.
 */

public class ReservationsAdapter extends RecyclerView.Adapter<ReservationsAdapter.ReservationViewHolder> {

    public static final String TAG = ReservationsAdapter.class.getName();
    private Context mContext;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    private List<String> mReservationIds = new ArrayList<>();
    private List<BusReservationInformation> mReservation = new ArrayList<>();

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener{
        void onClick(String code);
    }

    public ReservationsAdapter(final Context context, DatabaseReference ref) {
        mContext = context;
        mDatabaseReference = ref;

        // Create child event listener
        // [START child_event_listener_recycler]
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new Reservation has been added, add it to the displayed list
                BusReservationInformation Reservation = dataSnapshot.getValue(BusReservationInformation.class);

                // Update RecyclerView
                mReservationIds.add(dataSnapshot.getKey());
                mReservation.add(Reservation);
                notifyItemInserted(mReservation.size() - 1);

                itemClickListener = (OnItemClickListener) context;
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so displayed the changed comment.
                BusReservationInformation newOrder = dataSnapshot.getValue(BusReservationInformation.class);
                String commentKey = dataSnapshot.getKey();

                int reservationIndex = mReservationIds.indexOf(commentKey);
                if (reservationIndex > -1) {
                    // Replace with the new mData
                    mReservation.set(reservationIndex, newOrder);

                    // Update the RecyclerView
                    notifyItemChanged(reservationIndex);
                } else {
                    Log.w(TAG, "onChildChanged:unknown_child:" + commentKey);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so remove it.
                String reservationKey = dataSnapshot.getKey();

                int reservationIndex = mReservationIds.indexOf(reservationKey);
                if (reservationIndex > -1) {
                    // Remove mData from the list
                    mReservationIds.remove(reservationIndex);
                    mReservation.remove(reservationIndex);

                    // Update the RecyclerView
                    notifyItemRemoved(reservationIndex);
                } else {
                    Log.w(TAG, "onChildRemoved:unknown_child:" + reservationKey);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A comment has changed position, use the key to determine if we are
                // displaying this comment and if so move it.
                BusReservationInformation movedReservation = dataSnapshot.getValue(BusReservationInformation.class);
                String reservationKey = dataSnapshot.getKey();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(mContext, "Failed to load orders.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        ref.addChildEventListener(mChildEventListener);
        // END child_event_listener_recycler
    }




    @Override
    public ReservationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.reservation_list_item, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReservationViewHolder holder, int position) {
        BusReservationInformation order = mReservation.get(position);
        holder.dateTextView.setText(order.getDate());
        holder.fromTextView.setText(order.getFrom());
        holder.toTextView.setText(order.getTo());
        holder.seatsTextView.setText(order.getSeats());
    }

    @Override
    public int getItemCount() {
        return mReservation.size();
    }

    public void cleanupListener() {
        if (mChildEventListener != null) {
            mDatabaseReference.removeEventListener(mChildEventListener);
        }
    }

    class ReservationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView dateTextView;
        public TextView fromTextView;
        public TextView toTextView;
        public TextView seatsTextView;


        public ReservationViewHolder(View itemView) {
            super(itemView);

            dateTextView = (TextView) itemView.findViewById(R.id.name_textview);
            fromTextView = (TextView) itemView.findViewById(R.id.from_textview);
            toTextView = (TextView) itemView.findViewById(R.id.to_textview);
            seatsTextView = (TextView) itemView.findViewById(R.id.seats_textview);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getLayoutPosition();
            String reservationCode = mReservationIds.get(position);
            itemClickListener.onClick(reservationCode);
        }
    }

}
