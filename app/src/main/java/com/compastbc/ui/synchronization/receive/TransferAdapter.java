package com.compastbc.ui.synchronization.receive;

import android.content.Context;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppLogger;
import com.compastbc.core.utils.AppUtils;
import com.compastbc.synchronization.transfer.TransferStatus;
import com.compastbc.synchronization.util.Settings;

import java.io.File;

/**
 * Transfer adapter that shows transfers in progress
 */
class TransferAdapter extends RecyclerView.Adapter<TransferAdapter.ViewHolder> {

    private final Context mContext;
    private final Settings mSettings;
    private final SparseArray<TransferStatus> mStatuses = new SparseArray<>();
    private final boolean isSender;

    /**
     * Create a new transfer adapter
     */
    public TransferAdapter(Context context, boolean isSender) {
        mContext = context;
        this.isSender = isSender;
        mSettings = new Settings(mContext);
    }

    /**
     * Update the information for a transfer in the sparse array
     */
    void update(TransferStatus transferStatus) {
        int index = mStatuses.indexOfKey(transferStatus.getId());
        if (index < 0) {
            mStatuses.put(transferStatus.getId(), transferStatus);
            notifyItemInserted(mStatuses.size());
        } else {
            mStatuses.setValueAt(index, transferStatus);
            notifyItemChanged(index);
        }
    }

    /**
     * Retrieve the status for the specified index
     */
    TransferStatus getStatus(int index) {
        return mStatuses.valueAt(index);
    }

    /**
     * Remove the specified transfer from the sparse array
     */
    void remove(int index) {
        mStatuses.removeAt(index);
        notifyItemRemoved(index);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_transfer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final TransferStatus transferStatus = mStatuses.valueAt(position);

        // Generate transfer byte string
        CharSequence bytesText;
        if (transferStatus.getBytesTotal() == 0) {
            bytesText = mContext.getString(R.string.adapter_transfer_unknown);
        } else {
            bytesText = mContext.getString(
                    R.string.adapter_transfer_bytes,
                    Formatter.formatShortFileSize(mContext, transferStatus.getBytesTransferred()),
                    Formatter.formatShortFileSize(mContext, transferStatus.getBytesTotal())
            );
        }

        // Set the attributes
        holder.mIcon.setImageResource(R.drawable.ic_download_file);
        holder.mDevice.setText(transferStatus.getRemoteDeviceName());
        holder.mProgress.setProgress(transferStatus.getProgress());
        holder.mBytes.setText(bytesText);

        // Display the correct state string in the correct style
        switch (transferStatus.getState()) {
            case Connecting:
            case Transferring:
                if (transferStatus.getState() == TransferStatus.State.Connecting) {
                    holder.mState.setText(R.string.adapter_transfer_connecting);
                } else {
                    holder.mState.setText(mContext.getString(R.string.adapter_transfer_transferring,
                            transferStatus.getProgress()));
                }
                holder.mState.setTextColor(ContextCompat.getColor(mContext, android.R.color.darker_gray));
               /* holder.mStop.setVisibility(View.VISIBLE);
                holder.mStop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent stopIntent = new Intent(mContext, TransferService.class)
                                .setAction(TransferService.ACTION_STOP_TRANSFER)
                                .putExtra(TransferService.EXTRA_TRANSFER, transferStatus.getId());
                        mContext.startService(stopIntent);
                    }
                });*/
                break;
            case Succeeded:
                holder.mState.setText(R.string.adapter_transfer_succeeded);
                holder.mState.setTextColor(ContextCompat.getColor(mContext,
                        mSettings.getTheme() == R.style.LightTheme ?
                                R.color.colorSuccess : R.color.colorSuccessDark));
                //holder.mStop.setVisibility(View.INVISIBLE);

                if (isSender) {
                    AppLogger.e("TransferManger", "file delete in sender device");
                    File path = new File(Environment.getExternalStorageDirectory() +
                            File.separator.concat(AppConstants.FOLDER_NAME).concat("/").concat(AppConstants.FILE_NAME));
                    AppUtils.deleteFile(path);
                }
                break;
            case Failed:
                holder.mState.setText(mContext.getString(R.string.adapter_transfer_failed,
                        transferStatus.getError()));
                holder.mState.setTextColor(ContextCompat.getColor(mContext,
                        mSettings.getTheme() == R.style.LightTheme ?
                                R.color.colorError : R.color.colorErrorDark));
                //holder.mStop.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mStatuses.size();
    }

    /**
     * View holder for individual transfers
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIcon;
        private final TextView mDevice;
        private final TextView mState;
        private final ProgressBar mProgress;
        private final TextView mBytes;

        ViewHolder(View itemView) {
            super(itemView);

            mIcon = itemView.findViewById(R.id.transfer_icon);
            mDevice = itemView.findViewById(R.id.transfer_device);
            mState = itemView.findViewById(R.id.transfer_state);
            mProgress = itemView.findViewById(R.id.transfer_progress);
            mBytes = itemView.findViewById(R.id.transfer_bytes);

        }
    }
}