package de.adorsys.xs2a.adapter.service.impl;

import de.adorsys.xs2a.adapter.adapter.BaseAccountInformationService;
import de.adorsys.xs2a.adapter.http.StringUri;
import de.adorsys.xs2a.adapter.service.Response;
import de.adorsys.xs2a.adapter.service.RequestHeaders;
import de.adorsys.xs2a.adapter.service.model.StartScaProcessResponse;
import de.adorsys.xs2a.adapter.service.model.ConsentCreationResponse;
import de.adorsys.xs2a.adapter.service.model.ConsentStatusResponse;
import de.adorsys.xs2a.adapter.service.model.Consents;
import de.adorsys.xs2a.adapter.service.impl.mapper.ScaStatusResponseMapper;
import de.adorsys.xs2a.adapter.service.impl.mapper.UnicreditCreateConsentResponseMapper;
import de.adorsys.xs2a.adapter.service.impl.mapper.UnicreditStartAuthorisationResponseMapper;
import de.adorsys.xs2a.adapter.service.impl.model.UnicreditAccountScaStatusResponse;
import de.adorsys.xs2a.adapter.service.impl.model.UnicreditStartScaProcessResponse;
import de.adorsys.xs2a.adapter.service.model.ScaStatusResponse;
import de.adorsys.xs2a.adapter.service.model.TransactionAuthorisation;
import de.adorsys.xs2a.adapter.service.model.UpdatePsuAuthentication;

import java.util.Map;

public class UnicreditAccountInformationService extends BaseAccountInformationService {
    private static final String AUTHENTICATION_CURRENT_NUMBER_QUERY_PARAM = "authenticationCurrentNumber";
    private static final String DEFAULT_PSU_IP_ADDRESS = "0.0.0.0";

    private final UnicreditCreateConsentResponseMapper createConsentResponseMapper = new UnicreditCreateConsentResponseMapper();
    private final UnicreditStartAuthorisationResponseMapper startAuthorisationResponseMapper = new UnicreditStartAuthorisationResponseMapper();
    private final ScaStatusResponseMapper scaStatusResponseMapper = new ScaStatusResponseMapper();

    public UnicreditAccountInformationService(String baseUri) {
        super(baseUri);
    }

    @Override
    public Response<ConsentCreationResponse> createConsent(RequestHeaders requestHeaders, Consents body) {
        return createConsent(requestHeaders, body, ConsentCreationResponse.class, createConsentResponseMapper::modifyResponse);
    }

    @Override
    public Response<StartScaProcessResponse> startConsentAuthorisation(String consentId, RequestHeaders requestHeaders, UpdatePsuAuthentication updatePsuAuthentication) {
        String uri = StringUri.fromElements(getConsentBaseUri(), consentId);
        Map<String, String> headersMap = populatePutHeaders(requestHeaders.toMap());
        String body = jsonMapper.writeValueAsString(updatePsuAuthentication);

        Response<UnicreditStartScaProcessResponse> response = httpClient.put(uri, body, headersMap, jsonResponseHandler(UnicreditStartScaProcessResponse.class));

        return new Response<>(response.getStatusCode(), startAuthorisationResponseMapper.modifyResponse(response.getResponseBody()), response.getResponseHeaders());
    }

    @Override
    public Response<ScaStatusResponse> updateConsentsPsuData(String consentId, String authorisationId, RequestHeaders requestHeaders, TransactionAuthorisation transactionAuthorisation) {
        return updateConsentsPsuData(consentId, authorisationId, requestHeaders, transactionAuthorisation, UnicreditAccountScaStatusResponse.class, scaStatusResponseMapper::toScaStatusResponse);
    }

    @Override
    public Response<ScaStatusResponse> getConsentScaStatus(String consentId, String authorisationId, RequestHeaders requestHeaders) {
        Response<ConsentStatusResponse> response = this.getConsentStatus(consentId, requestHeaders);
        return new Response<>(response.getStatusCode(), scaStatusResponseMapper.toScaStatusResponse(response.getResponseBody()), response.getResponseHeaders());
    }

    @Override
    protected String getUpdateConsentPsuDataUri(String consentId, String authorisationId) {
        String uri = StringUri.fromElements(getConsentBaseUri(), consentId);
        return StringUri.withQuery(uri, AUTHENTICATION_CURRENT_NUMBER_QUERY_PARAM, authorisationId);
    }

    @Override
    protected Map<String, String> populateGetHeaders(Map<String, String> map) {
        Map<String, String> headers = super.populateGetHeaders(map);
        headers.put(ACCEPT_HEADER, APPLICATION_JSON);
        headers.putIfAbsent(RequestHeaders.PSU_IP_ADDRESS, DEFAULT_PSU_IP_ADDRESS);

        return headers;
    }

    @Override
    protected Map<String, String> populatePostHeaders(Map<String, String> map) {
        Map<String, String> headers = super.populatePostHeaders(map);
        headers.put(CONTENT_TYPE_HEADER, APPLICATION_JSON);
        return headers;
    }

    @Override
    protected Map<String, String> populatePutHeaders(Map<String, String> headers) {
        headers.put(CONTENT_TYPE_HEADER, APPLICATION_JSON);
        return headers;
    }
}
