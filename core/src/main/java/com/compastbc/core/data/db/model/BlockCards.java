package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity(nameInDb = "BlockCards")
public class BlockCards {

    @Id
    private Long id;
    private String cardNo;
    private String identityNo;

    @Generated(hash = 911367244)
    public BlockCards(Long id, String cardNo, String identityNo) {
        this.id = id;
        this.cardNo = cardNo;
        this.identityNo = identityNo;
    }

    @Generated(hash = 1623907508)
    public BlockCards() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getIdentityNo() {
        return identityNo;
    }

    public void setIdentityNo(String identityNo) {
        this.identityNo = identityNo;
    }
}
