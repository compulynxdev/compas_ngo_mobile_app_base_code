package com.compastbc.ui.synchronization.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.utils.AppLogger;
import com.compastbc.synchronization.transfer.TransferManager;
import com.compastbc.synchronization.transfer.TransferService;
import com.compastbc.synchronization.transfer.TransferStatus;
import com.compastbc.ui.base.BaseFragment;

import java.util.Objects;

/**
 * Fragment that displays a single RecyclerView
 */
public class TransferFragment extends BaseFragment {

    private static final String TAG = "TransferFragment";

    private BroadcastReceiver mBroadcastReceiver;

    private RecyclerView mRecyclerView;
    private TextView mTextView;

    private boolean isSender = false;

    public static TransferFragment newInstance(boolean isSender) {

        Bundle args = new Bundle();

        TransferFragment fragment = new TransferFragment();
        args.putBoolean("IsSender", isSender);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isSender = getArguments().getBoolean("IsSender", false);
        }
    }

    @Override
    protected void setUp(View view) {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Create layout parameters for full expansion
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        // Create a container
        ViewGroup parentView = new LinearLayout(getContext());
        parentView.setLayoutParams(layoutParams);

        // Setup the adapter and recycler view
        final TransferAdapter adapter = new TransferAdapter(getContext(), isSender);
        mRecyclerView = new RecyclerView(Objects.requireNonNull(getContext()));
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setLayoutParams(layoutParams);
        mRecyclerView.setVisibility(View.GONE);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(Objects.requireNonNull(getContext()), DividerItemDecoration.VERTICAL));
        parentView.addView(mRecyclerView);

        // Setup the empty view
        mTextView = new TextView(getContext());
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setLayoutParams(layoutParams);
        mTextView.setTextColor(mTextView.getTextColors().withAlpha(60));
        mTextView.setText(R.string.activity_transfer_empty_text);
        parentView.addView(mTextView);


        // Enable swipe-to-dismiss
        new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START | ItemTouchHelper.END) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                        // Calculate the position of the item and retrieve its status
                        int position = viewHolder.getAdapterPosition();
                        TransferStatus transferStatus = adapter.getStatus(position);

                        // Remove the item from the adapter
                        adapter.remove(position);

                        // If none remain, reshow the empty text
                        if (adapter.getItemCount() == 0) {
                            mRecyclerView.setVisibility(View.GONE);
                            mTextView.setVisibility(View.VISIBLE);
                        }

                        // Remove the item from the service
                        Intent removeIntent = new Intent(getContext(), TransferService.class)
                                .setAction(TransferService.ACTION_REMOVE_TRANSFER)
                                .putExtra(TransferService.EXTRA_TRANSFER, transferStatus.getId());
                        Objects.requireNonNull(getContext()).startService(removeIntent);
                    }
                }
        ).attachToRecyclerView(mRecyclerView);

        // Disable change animations (because they are really, really ugly)
        ((DefaultItemAnimator) Objects.requireNonNull(mRecyclerView.getItemAnimator())).setSupportsChangeAnimations(false);

        // Setup the broadcast receiver
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                TransferStatus transferStatus = intent.getParcelableExtra(TransferManager.EXTRA_STATUS);
                assert transferStatus != null;
                adapter.update(transferStatus);

                if (adapter.getItemCount() == 1) {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mTextView.setVisibility(View.GONE);
                }
            }
        };

        return parentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp(view);
    }

    @Override
    public void onStart() {
        super.onStart();

        AppLogger.i(TAG, "registering broadcast receiver");

        // Start listening for broadcasts
        Objects.requireNonNull(getContext()).registerReceiver(mBroadcastReceiver,
                new IntentFilter(TransferManager.TRANSFER_UPDATED));

        // Get fresh data from the service
        Intent broadcastIntent = new Intent(getContext(), TransferService.class)
                .setAction(TransferService.ACTION_BROADCAST);
        getContext().startService(broadcastIntent);
    }

    @Override
    public void onStop() {
        super.onStop();

        AppLogger.i(TAG, "unregistering broadcast receiver");

        // Stop listening for broadcasts
        Objects.requireNonNull(getContext()).unregisterReceiver(mBroadcastReceiver);
    }
}