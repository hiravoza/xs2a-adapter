package de.adorsys.xs2a.adapter.rest.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class ConsentsResponseTO {
    private String consentStatus;

    private String consentId;

    private List<AuthenticationObjectTO> scaMethods;

    private AuthenticationObjectTO chosenScaMethod;

    private ChallengeDataTO challengeData;

    @JsonProperty("_links")
    private Map<String, HrefTypeTO> links;

    private String psuMessage;

    public String getConsentStatus() {
        return consentStatus;
    }

    public void setConsentStatus(String consentStatus) {
        this.consentStatus = consentStatus;
    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public List<AuthenticationObjectTO> getScaMethods() {
        return scaMethods;
    }

    public void setScaMethods(List<AuthenticationObjectTO> scaMethods) {
        this.scaMethods = scaMethods;
    }

    public AuthenticationObjectTO getChosenScaMethod() {
        return chosenScaMethod;
    }

    public void setChosenScaMethod(AuthenticationObjectTO chosenScaMethod) {
        this.chosenScaMethod = chosenScaMethod;
    }

    public ChallengeDataTO getChallengeData() {
        return challengeData;
    }

    public void setChallengeData(ChallengeDataTO challengeData) {
        this.challengeData = challengeData;
    }

    public Map<String, HrefTypeTO> getLinks() {
        return links;
    }

    public void setLinks(Map<String, HrefTypeTO> links) {
        this.links = links;
    }

    public String getPsuMessage() {
        return psuMessage;
    }

    public void setPsuMessage(String psuMessage) {
        this.psuMessage = psuMessage;
    }
}
