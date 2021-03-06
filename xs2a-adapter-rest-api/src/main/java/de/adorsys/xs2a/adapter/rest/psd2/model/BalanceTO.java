package de.adorsys.xs2a.adapter.rest.psd2.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class BalanceTO {
    private AmountTO balanceAmount;

    private String balanceType;

    private Boolean creditLimitIncluded;

    private OffsetDateTime lastChangeDateTime;

    private LocalDate referenceDate;

    private String lastCommittedTransaction;

    public AmountTO getBalanceAmount() {
        return balanceAmount;
    }

    public void setBalanceAmount(AmountTO balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public String getBalanceType() {
        return balanceType;
    }

    public void setBalanceType(String balanceType) {
        this.balanceType = balanceType;
    }

    public Boolean getCreditLimitIncluded() {
        return creditLimitIncluded;
    }

    public void setCreditLimitIncluded(Boolean creditLimitIncluded) {
        this.creditLimitIncluded = creditLimitIncluded;
    }

    public OffsetDateTime getLastChangeDateTime() {
        return lastChangeDateTime;
    }

    public void setLastChangeDateTime(OffsetDateTime lastChangeDateTime) {
        this.lastChangeDateTime = lastChangeDateTime;
    }

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(LocalDate referenceDate) {
        this.referenceDate = referenceDate;
    }

    public String getLastCommittedTransaction() {
        return lastCommittedTransaction;
    }

    public void setLastCommittedTransaction(String lastCommittedTransaction) {
        this.lastCommittedTransaction = lastCommittedTransaction;
    }
}
