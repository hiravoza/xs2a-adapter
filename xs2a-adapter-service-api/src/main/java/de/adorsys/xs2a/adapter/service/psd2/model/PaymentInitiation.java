package de.adorsys.xs2a.adapter.service.psd2.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public class PaymentInitiation {
    private String endToEndIdentification;
    private AccountReference debtorAccount;
    private Amount instructedAmount;
    private AccountReference creditorAccount;
    private String creditorAgent;
    private String creditorAgentName;
    private String creditorName;
    private Address creditorAddress;
    private String remittanceInformationUnstructured;

    // periodic
    private LocalDate startDate;
    private LocalDate endDate;
    private String executionRule;
    private String frequency;
    private String dayOfExecution;

    // bulk
    private Boolean batchBookingPreferred;
    private LocalDate requestedExecutionDate;
    private OffsetDateTime requestedExecutionTime;
    private List<PaymentInitiationBulkElement> payments;

    public String getEndToEndIdentification() {
        return endToEndIdentification;
    }

    public void setEndToEndIdentification(String endToEndIdentification) {
        this.endToEndIdentification = endToEndIdentification;
    }

    public AccountReference getDebtorAccount() {
        return debtorAccount;
    }

    public void setDebtorAccount(AccountReference debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public Amount getInstructedAmount() {
        return instructedAmount;
    }

    public void setInstructedAmount(Amount instructedAmount) {
        this.instructedAmount = instructedAmount;
    }

    public AccountReference getCreditorAccount() {
        return creditorAccount;
    }

    public void setCreditorAccount(AccountReference creditorAccount) {
        this.creditorAccount = creditorAccount;
    }

    public String getCreditorAgent() {
        return creditorAgent;
    }

    public void setCreditorAgent(String creditorAgent) {
        this.creditorAgent = creditorAgent;
    }

    public String getCreditorAgentName() {
        return creditorAgentName;
    }

    public void setCreditorAgentName(String creditorAgentName) {
        this.creditorAgentName = creditorAgentName;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public void setCreditorName(String creditorName) {
        this.creditorName = creditorName;
    }

    public Address getCreditorAddress() {
        return creditorAddress;
    }

    public void setCreditorAddress(Address creditorAddress) {
        this.creditorAddress = creditorAddress;
    }

    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured;
    }

    public void setRemittanceInformationUnstructured(String remittanceInformationUnstructured) {
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getExecutionRule() {
        return executionRule;
    }

    public void setExecutionRule(String executionRule) {
        this.executionRule = executionRule;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDayOfExecution() {
        return dayOfExecution;
    }

    public void setDayOfExecution(String dayOfExecution) {
        this.dayOfExecution = dayOfExecution;
    }

    public Boolean getBatchBookingPreferred() {
        return batchBookingPreferred;
    }

    public void setBatchBookingPreferred(Boolean batchBookingPreferred) {
        this.batchBookingPreferred = batchBookingPreferred;
    }

    public LocalDate getRequestedExecutionDate() {
        return requestedExecutionDate;
    }

    public void setRequestedExecutionDate(LocalDate requestedExecutionDate) {
        this.requestedExecutionDate = requestedExecutionDate;
    }

    public OffsetDateTime getRequestedExecutionTime() {
        return requestedExecutionTime;
    }

    public void setRequestedExecutionTime(OffsetDateTime requestedExecutionTime) {
        this.requestedExecutionTime = requestedExecutionTime;
    }

    public List<PaymentInitiationBulkElement> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentInitiationBulkElement> payments) {
        this.payments = payments;
    }
}
