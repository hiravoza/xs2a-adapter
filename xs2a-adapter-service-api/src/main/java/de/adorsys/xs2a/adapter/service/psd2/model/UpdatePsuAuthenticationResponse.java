package de.adorsys.xs2a.adapter.service.psd2.model;

import java.util.List;
import java.util.Map;

public class UpdatePsuAuthenticationResponse {
    private AuthenticationObject chosenScaMethod;

    private ChallengeData challengeData;

    private List<AuthenticationObject> scaMethods;

    private Map<String, HrefType> links;

    private String scaStatus;

    private String psuMessage;

    public AuthenticationObject getChosenScaMethod() {
        return chosenScaMethod;
    }

    public void setChosenScaMethod(AuthenticationObject chosenScaMethod) {
        this.chosenScaMethod = chosenScaMethod;
    }

    public ChallengeData getChallengeData() {
        return challengeData;
    }

    public void setChallengeData(ChallengeData challengeData) {
        this.challengeData = challengeData;
    }

    public List<AuthenticationObject> getScaMethods() {
        return scaMethods;
    }

    public void setScaMethods(List<AuthenticationObject> scaMethods) {
        this.scaMethods = scaMethods;
    }

    public Map<String, HrefType> getLinks() {
        return links;
    }

    public void setLinks(Map<String, HrefType> links) {
        this.links = links;
    }

    public String getScaStatus() {
        return scaStatus;
    }

    public void setScaStatus(String scaStatus) {
        this.scaStatus = scaStatus;
    }

    public String getPsuMessage() {
        return psuMessage;
    }

    public void setPsuMessage(String psuMessage) {
        this.psuMessage = psuMessage;
    }
}