package com.compastbc.ui.reports.commodityreport;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.compastbc.R;
import com.compastbc.core.data.network.model.CommodityReportBean;
import com.compastbc.core.data.network.model.ReportModel;
import com.compastbc.core.utils.AppLogger;
import com.compastbc.ui.reports.expandable_adapter.model.SectionHeader;
import com.compastbc.ui.reports.expandable_adapter.model.SectionSubHeader;
import com.compastbc.ui.reports.expandable_adapter.viewholder.HeaderViewHolderTwo;
import com.compastbc.ui.reports.expandable_adapter.viewholder.SubHeaderViewHolder;
import com.elder.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.elder.expandablerecyclerview.ExpandableRowOnClickListener;
import com.elder.expandablerecyclerview.ExpandableRowSubViewOnClickListener;

import java.util.List;
import java.util.Locale;

/**
 * Created by chris on 2016-12-05.
 */

public class CommodityAdapter
        extends ExpandableRecyclerViewAdapter<SectionHeader, SectionSubHeader, ReportModel, HeaderViewHolderTwo, SubHeaderViewHolder, CommodityAdapter.CommodityViewHolder>
        implements ExpandableRowSubViewOnClickListener<SectionHeader, SectionSubHeader, ReportModel>,
        ExpandableRowOnClickListener<SectionHeader, SectionSubHeader, ReportModel> {

    private final Context context;

    //----------------- data source for section content -----------------\\
    // Separate data source for the content of each section.
    // Make changes to the data source as you normally would in a RecyclerView.
    // After changes --> update????
    private final List<CommodityReportBean> headerList;

    CommodityAdapter(Context context, List<CommodityReportBean> headerList) {
        // Provide the ExpandableRecyclerViewAdapter with class information for our view holders
        super(HeaderViewHolderTwo.class,
                SubHeaderViewHolder.class,
                CommodityViewHolder.class);

        // Set the reference to context
        this.context = context;
        this.headerList = headerList;
        // Set the on click listener for sub views of our rows.
        this.expandableRowSubViewOnClickListener = this;
        this.expandableRowOnClickListener = this;
    }

    // endregion
    //================================================================================

    //================================================================================
    // region EXPANDABLE: CREATING SECTIONS

    @Override
    public int getNumberOfSections() {
        // Return the number of sections we want in the recycler view
        return headerList.size();
    }

    @Override
    public SectionHeader getHeaderForSection(int sectionIndex) {
        // Create each header section
        CommodityReportBean mainBean = headerList.get(sectionIndex);
        return new SectionHeader(mainBean.getTitle(), mainBean.getTtlAmt(), mainBean.getCurrency());
    }

    @Override
    public SectionSubHeader getSubHeaderForSection(int sectionIndex) {
        // Create each sub header section
        // using the same section sub header for each section
        // you can easily change this according to the section index
        return new SectionSubHeader("Loading", "Error Loading - retry");
    }

    @Override
    public List<ReportModel> getContentForSection(int sectionIndex) {
        return headerList.get(sectionIndex).getModelList();
    }

    @Override
    public HeaderViewHolderTwo createSectionHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header_row, parent, false);
        return new HeaderViewHolderTwo(view);
    }

    @Override
    public SubHeaderViewHolder createSectionSubHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subheader_row, parent, false);
        return new SubHeaderViewHolder(view);
    }

    @Override
    public CommodityViewHolder createSectionContentViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.commodity_row, parent, false);
        return new CommodityViewHolder(view);
    }

    // endregion
    //================================================================================

    //================================================================================
    // region EXPANDABLE: BINDING VIEW HOLDER VIEWS

    @Override
    public void bindSectionHeaderViewHolder(HeaderViewHolderTwo holder,
                                            SectionHeader header,
                                            int sectionIndex,
                                            int expansionState) {
        // set header text
        holder.setHeaderText(header.getTitle().concat("\n").concat(context.getString(R.string.TotalAmount)).concat(" ").concat(String.format(Locale.getDefault(), "%s %.2f", header.getCurrency(), Double.parseDouble(header.getAmount()))));
        if (expansionState == EXPANSION_STATE_MINIMIZED) {
            holder.setExpandButtonRotation(0);
        } else {
            holder.setExpandButtonRotation(180);
        }

        // explicitly register the image button for click listening
        // DO NOT implement OnClickListener in this subclass.
        // OnClickListener is implemented by ExpandableRecyclerViewAdapter and delegates to
        // ExpandableRowSubViewOnClickListener (this must be implemented here).
    }

    @Override
    public void bindSectionSubHeaderViewHolder(SubHeaderViewHolder holder,
                                               SectionSubHeader subHeader,
                                               int sectionIndex,
                                               int expansionState) {
        // Only need to handle loading and error, sub header is hidden for other states

        switch (expansionState) {
            case ExpandableRecyclerViewAdapter.EXPANSION_STATE_LOADING_CONTENT:
                holder.setSubHeaderText(subHeader.getLoadingString());
                break;
            case ExpandableRecyclerViewAdapter.EXPANSION_STATE_ERROR_LOADING:
                holder.setSubHeaderText(subHeader.getErrorString());
                break;
        }
    }

    @Override
    public void bindSectionContentViewHolder(CommodityViewHolder viewHolder, ReportModel tmpBean, int sectionIndex, int expansionState) {
        viewHolder.tvName.setText(tmpBean.getName());
        viewHolder.tvValue.setText(String.format(Locale.getDefault(), "%s %.2f", tmpBean.getCurrency(), Double.parseDouble(tmpBean.getValue())));
    }

    // endregion
    //================================================================================

    //================================================================================
    // region EXPANDABLE: OTHER STATE METHODS

    @Override
    public int getDefaultExpansionStateForSection(int sectionIndex) {
        // Provide the default state of each section.
        return EXPANSION_STATE_MINIMIZED;
    }

    @Override
    public boolean shouldShowSectionSubHeader(int sectionIndex, int expansionState) {
        // Only show the sub header is the section is loading or had an error
        return expansionState == EXPANSION_STATE_LOADING_CONTENT ||
                expansionState == EXPANSION_STATE_ERROR_LOADING;
    }

    @Override
    public int getSavedStateForSection(int sectionIndex, int expansionState) {
        if (expansionState == EXPANSION_STATE_EXPANDED)
            return expansionState;
        else
            return EXPANSION_STATE_MINIMIZED;
    }

    // endregion
    //================================================================================

    //================================================================================
    // region EXPANDABLE ROW ON CLICK LISTENER

    @Override
    public void sectionHeaderClicked(SectionHeader header, int sectionIndex, int expansionState) {
        AppLogger.i(String.valueOf(sectionIndex), "section Header Clicked");
        if (expansionState == EXPANSION_STATE_MINIMIZED) {
            if (sectionHasContent(sectionIndex)) {
                setExpansionStateExpanded(sectionIndex);
            } /*else {
                    setExpansionStateLoading(sectionIndex);

                    ExampleTwoActivity activity = (ExampleTwoActivity) context;

                    switch (sectionIndex)
                    {
                        case 0:
                            activity.loadDogs();
                            break;
                        case 1:
                            activity.loadCats();
                            break;
                        case 2:
                            activity.loadGoats();
                            break;
                    }
            }*/
        } else if (expansionState == EXPANSION_STATE_EXPANDED) {
            setExpansionStateMinimized(sectionIndex);
        }
    }

    @Override
    public void sectionSubHeaderClicked(SectionSubHeader subHeader, int sectionIndex, int expansionState) {
        // If there is an error, reload the section
        if (expansionState == EXPANSION_STATE_ERROR_LOADING) {
            // Reload
            setExpansionStateLoading(sectionIndex);

          /*  ExampleTwoActivity activity = (ExampleTwoActivity) context;

            switch (sectionIndex)
            {
                case 0:
                    activity.loadDogs();
                    break;
                case 1:
                    activity.loadCats();
                    break;
                case 2:
                    activity.loadGoats();
                    break;
            }*/
        }
    }

    @Override
    public void sectionContentClicked(ReportModel content, int sectionIndex, int sectionContentIndex) {
        AppLogger.i(String.valueOf(sectionIndex), "section Content Clicked");
    }

    // endregion
    //================================================================================

    //================================================================================
    // region EXPANDABLE ROW SUB VIEW ON CLICK LISTENER

    @Override
    public void onSubViewClickedInSectionHeader(View view, SectionHeader header, int sectionIndex, int expansionState) {
        AppLogger.i(String.valueOf(sectionIndex), "Subview in section header Clicked");
    }

    @Override
    public void onSubViewClickedInSectionSubHeader(View view, SectionSubHeader subHeader, int sectionIndex, int expansionState) {
        AppLogger.i(String.valueOf(sectionIndex), "sub view clicked in sub header");
    }

    @Override
    public void onSubViewClickedInSectionContentRow(View view, ReportModel content, int sectionIndex, int sectionContentIndex) {
        AppLogger.i(String.valueOf(sectionIndex), "sub view clicked in content row");
    }

    class CommodityViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvValue;

        CommodityViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_progName);
            tvValue = itemView.findViewById(R.id.tv_value);

        }
    }


}
