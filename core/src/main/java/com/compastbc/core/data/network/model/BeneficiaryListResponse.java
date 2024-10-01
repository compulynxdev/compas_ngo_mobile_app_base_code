package com.compastbc.core.data.network.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Hemant Sharma on 09-10-19.
 * Divergent software labs pvt. ltd
 */
public class BeneficiaryListResponse {

    /**
     * content : [{"physicalAdd":"","firstName":"amitabh bachhan","idPassPortNo":"123123","surName":"","gender":"M","dateOfBirth":"2019-08-26","cardno":"4440883333336011","memberId":1}]
     * pageable : {"sort":{"sorted":false,"unsorted":true},"offset":0,"pageSize":20,"pageNumber":0,"unpaged":false,"paged":true}
     * totalElements : 1
     * totalPages : 1
     * last : true
     * size : 20
     * number : 0
     * sort : {"sorted":false,"unsorted":true}
     * first : true
     * numberOfElements : 1
     */

    private PageableBean pageable;
    private int totalElements;
    private int totalPages;
    private boolean last;
    private int size;
    private int number;
    private SortBeanX sort;
    private boolean first;
    private int numberOfElements;
    private List<ContentBean> content;

    public PageableBean getPageable() {
        return pageable;
    }

    public void setPageable(PageableBean pageable) {
        this.pageable = pageable;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public SortBeanX getSort() {
        return sort;
    }

    public void setSort(SortBeanX sort) {
        this.sort = sort;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public List<ContentBean> getContent() {
        return content;
    }

    public void setContent(List<ContentBean> content) {
        this.content = content;
    }

    public static class PageableBean {
        /**
         * sort : {"sorted":false,"unsorted":true}
         * offset : 0
         * pageSize : 20
         * pageNumber : 0
         * unpaged : false
         * paged : true
         */

        private SortBean sort;
        private int offset;
        private int pageSize;
        private int pageNumber;
        private boolean unpaged;
        private boolean paged;

        public SortBean getSort() {
            return sort;
        }

        public void setSort(SortBean sort) {
            this.sort = sort;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public void setPageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        public boolean isUnpaged() {
            return unpaged;
        }

        public void setUnpaged(boolean unpaged) {
            this.unpaged = unpaged;
        }

        public boolean isPaged() {
            return paged;
        }

        public void setPaged(boolean paged) {
            this.paged = paged;
        }

        public static class SortBean {
            /**
             * sorted : false
             * unsorted : true
             */

            private boolean sorted;
            private boolean unsorted;

            public boolean isSorted() {
                return sorted;
            }

            public void setSorted(boolean sorted) {
                this.sorted = sorted;
            }

            public boolean isUnsorted() {
                return unsorted;
            }

            public void setUnsorted(boolean unsorted) {
                this.unsorted = unsorted;
            }
        }
    }

    public static class SortBeanX {
        /**
         * sorted : false
         * unsorted : true
         */

        private boolean sorted;
        private boolean unsorted;

        public boolean isSorted() {
            return sorted;
        }

        public void setSorted(boolean sorted) {
            this.sorted = sorted;
        }

        public boolean isUnsorted() {
            return unsorted;
        }

        public void setUnsorted(boolean unsorted) {
            this.unsorted = unsorted;
        }
    }

    public static class ContentBean implements Parcelable {
        public static final Creator<ContentBean> CREATOR = new Creator<ContentBean>() {
            @Override
            public ContentBean createFromParcel(Parcel in) {
                return new ContentBean(in);
            }

            @Override
            public ContentBean[] newArray(int size) {
                return new ContentBean[size];
            }
        };
        /**
         * physicalAdd :
         * firstName : amitabh bachhan
         * idPassPortNo : 123123
         * surName :
         * gender : M
         * dateOfBirth : 2019-08-26
         * cardno : 4440883333336011
         * memberId : 1
         * "bioStatus": false
         * "mobileNo": ""
         * benfImage
         */

        private String physicalAdd;
        private String firstName;
        private String idPassPortNo;
        private String surName;
        private String gender;
        private String dateOfBirth;
        private String cardno;
        private int memberId;
        private boolean bioStatus;
        private String mobileNo;
        private String benfImage;

        public ContentBean() {
        }

        protected ContentBean(Parcel in) {
            physicalAdd = in.readString();
            firstName = in.readString();
            idPassPortNo = in.readString();
            surName = in.readString();
            gender = in.readString();
            dateOfBirth = in.readString();
            cardno = in.readString();
            memberId = in.readInt();
            bioStatus = in.readByte() != 0;
            mobileNo = in.readString();
            benfImage = in.readString();
        }

        public String getPhysicalAdd() {
            return physicalAdd == null || physicalAdd.equals("null") ? "" : physicalAdd;
        }

        public void setPhysicalAdd(String physicalAdd) {
            this.physicalAdd = physicalAdd;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getIdPassPortNo() {
            return idPassPortNo;
        }

        public void setIdPassPortNo(String idPassPortNo) {
            this.idPassPortNo = idPassPortNo;
        }

        public String getSurName() {
            return surName == null ? "" : surName;
        }

        public void setSurName(String surName) {
            this.surName = surName;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public String getCardno() {
            return cardno;
        }

        public void setCardno(String cardno) {
            this.cardno = cardno;
        }

        public int getMemberId() {
            return memberId;
        }

        public void setMemberId(int memberId) {
            this.memberId = memberId;
        }

        public boolean isBioStatus() {
            return bioStatus;
        }

        public void setBioStatus(boolean bioStatus) {
            this.bioStatus = bioStatus;
        }

        public String getMobileNo() {
            return mobileNo == null ? "" : mobileNo;
        }

        public void setMobileNo(String mobileNo) {
            this.mobileNo = mobileNo;
        }

        public String getBenfImage() {
            return benfImage == null ? "" : benfImage;
        }

        public void setBenfImage(String benfImage) {
            this.benfImage = benfImage;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(physicalAdd);
            parcel.writeString(firstName);
            parcel.writeString(idPassPortNo);
            parcel.writeString(surName);
            parcel.writeString(gender);
            parcel.writeString(dateOfBirth);
            parcel.writeString(cardno);
            parcel.writeInt(memberId);
            parcel.writeByte((byte) (bioStatus ? 1 : 0));
            parcel.writeString(mobileNo);
            parcel.writeString(benfImage);
        }
    }
}
