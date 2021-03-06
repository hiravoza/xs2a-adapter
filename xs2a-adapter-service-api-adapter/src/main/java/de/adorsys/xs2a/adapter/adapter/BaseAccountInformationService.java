/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.xs2a.adapter.adapter;

import de.adorsys.xs2a.adapter.adapter.link.identity.IdentityLinksRewriter;
import de.adorsys.xs2a.adapter.http.ContentType;
import de.adorsys.xs2a.adapter.http.HttpClient;
import de.adorsys.xs2a.adapter.http.Request;
import de.adorsys.xs2a.adapter.http.StringUri;
import de.adorsys.xs2a.adapter.service.AccountInformationService;
import de.adorsys.xs2a.adapter.service.RequestHeaders;
import de.adorsys.xs2a.adapter.service.RequestParams;
import de.adorsys.xs2a.adapter.service.Response;
import de.adorsys.xs2a.adapter.service.link.LinksRewriter;
import de.adorsys.xs2a.adapter.service.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static de.adorsys.xs2a.adapter.http.ResponseHandlers.jsonResponseHandler;
import static de.adorsys.xs2a.adapter.http.ResponseHandlers.stringResponseHandler;
import static java.util.function.Function.identity;

public class BaseAccountInformationService extends AbstractService implements AccountInformationService {

    private static final LinksRewriter DEFAULT_LINKS_REWRITER = new IdentityLinksRewriter();

    protected static final Logger logger = LoggerFactory.getLogger(BaseAccountInformationService.class);
    protected static final String V1 = "v1";
    protected static final String CONSENTS = "consents";
    protected static final String ACCOUNTS = "accounts";
    protected static final String TRANSACTIONS = "transactions";
    protected static final String BALANCES = "balances";
    protected static final String CARD_ACCOUNTS = "card-accounts";

    protected final Aspsp aspsp;
    protected final Request.Builder.Interceptor requestBuilderInterceptor;
    private final LinksRewriter linksRewriter;

    public BaseAccountInformationService(Aspsp aspsp, HttpClient httpClient) {
        this(aspsp, httpClient, null, DEFAULT_LINKS_REWRITER);
    }

    public BaseAccountInformationService(Aspsp aspsp,
                                         HttpClient httpClient,
                                         Request.Builder.Interceptor requestBuilderInterceptor) {
        this(aspsp, httpClient, requestBuilderInterceptor, DEFAULT_LINKS_REWRITER);
    }

    public BaseAccountInformationService(Aspsp aspsp,
                                         HttpClient httpClient,
                                         LinksRewriter linksRewriter) {
        this(aspsp, httpClient, null, linksRewriter);
    }

    public BaseAccountInformationService(Aspsp aspsp,
                                         HttpClient httpClient,
                                         Request.Builder.Interceptor requestBuilderInterceptor,
                                         LinksRewriter linksRewriter) {
        super(httpClient);
        this.aspsp = aspsp;
        this.requestBuilderInterceptor = requestBuilderInterceptor;
        this.linksRewriter = linksRewriter;
    }

    @Override
    public Response<ConsentCreationResponse> createConsent(RequestHeaders requestHeaders,
                                                           RequestParams requestParams,
                                                           Consents body) {
        return createConsent(requestHeaders,
            requestParams,
            body,
            identity(),
            jsonResponseHandler(ConsentCreationResponse.class));
    }

    protected <T> Response<ConsentCreationResponse> createConsent(RequestHeaders requestHeaders,
                                                                  RequestParams requestParams,
                                                                  Consents body,
                                                                  Class<T> klass,
                                                                  Function<T, ConsentCreationResponse> mapper) {
        return createConsent(requestHeaders, requestParams, body, mapper, jsonResponseHandler(klass));
    }

    protected <T> Response<ConsentCreationResponse> createConsent(RequestHeaders requestHeaders,
                                                                  RequestParams requestParams,
                                                                  Consents body,
                                                                  Function<T, ConsentCreationResponse> mapper,
                                                                  HttpClient.ResponseHandler<T> responseHandler) {
        requireValid(validateCreateConsent(requestHeaders, requestParams, body));

        Map<String, String> headersMap = populatePostHeaders(requestHeaders.toMap());
        headersMap = addPsuIdHeader(headersMap);
        headersMap = addPsuIdTypeHeader(headersMap);

        String bodyString = jsonMapper.writeValueAsString(jsonMapper.convertValue(body, Consents.class));

        String uri = buildUri(getConsentBaseUri(), requestParams);
        Response<T> response = httpClient.post(uri)
            .jsonBody(bodyString)
            .headers(headersMap)
            .send(requestBuilderInterceptor, responseHandler);
        ConsentCreationResponse creationResponse = mapper.apply(response.getBody());
        creationResponse.setLinks(linksRewriter.rewrite(creationResponse.getLinks()));

        return new Response<>(response.getStatusCode(), creationResponse, response.getHeaders());
    }

    @Override
    public Response<ConsentInformation> getConsentInformation(String consentId,
                                                              RequestHeaders requestHeaders,
                                                              RequestParams requestParams) {
        return getConsentInformation(consentId, requestHeaders, requestParams, ConsentInformation.class, identity());
    }

    protected <T> Response<ConsentInformation> getConsentInformation(String consentId,
                                                                     RequestHeaders requestHeaders,
                                                                     RequestParams requestParams,
                                                                     Class<T> klass,
                                                                     Function<T, ConsentInformation> mapper) {
        requireValid(validateGetConsentInformation(consentId, requestHeaders, requestParams));

        String uri = StringUri.fromElements(getConsentBaseUri(), consentId);
        uri = buildUri(uri, requestParams);
        Map<String, String> headersMap = populateGetHeaders(requestHeaders.toMap());
        Response<T> response = httpClient.get(uri)
            .headers(headersMap)
            .send(requestBuilderInterceptor, jsonResponseHandler(klass));
        ConsentInformation consentInformation = mapper.apply(response.getBody());
        consentInformation.setLinks(linksRewriter.rewrite(consentInformation.getLinks()));

        return new Response<>(response.getStatusCode(), consentInformation, response.getHeaders());
    }

    @Override
    public Response<Void> deleteConsent(String consentId,
                                        RequestHeaders requestHeaders,
                                        RequestParams requestParams) {
        requireValid(validateDeleteConsent(consentId, requestHeaders, requestParams));

        String uri = StringUri.fromElements(getConsentBaseUri(), consentId);
        uri = buildUri(uri, requestParams);
        Map<String, String> headersMap = populateDeleteHeaders(requestHeaders.toMap());
        return httpClient.delete(uri)
            .headers(headersMap)
            .send(requestBuilderInterceptor, jsonResponseHandler(Void.class));
    }

    @Override
    public Response<ConsentStatusResponse> getConsentStatus(String consentId,
                                                            RequestHeaders requestHeaders,
                                                            RequestParams requestParams) {
        requireValid(validateGetConsentStatus(consentId, requestHeaders, requestParams));

        String uri = StringUri.fromElements(getConsentBaseUri(), consentId, STATUS);
        uri = buildUri(uri, requestParams);
        Map<String, String> headersMap = populateGetHeaders(requestHeaders.toMap());

        return httpClient.get(uri)
            .headers(headersMap)
            .send(requestBuilderInterceptor, jsonResponseHandler(ConsentStatusResponse.class));
    }

    @Override
    public Response<StartScaProcessResponse> startConsentAuthorisation(String consentId,
                                                                       RequestHeaders requestHeaders,
                                                                       RequestParams requestParams) {
        requireValid(validateStartConsentAuthorisation(consentId, requestHeaders, requestParams));

        String uri = StringUri.fromElements(getConsentBaseUri(), consentId, AUTHORISATIONS);
        uri = buildUri(uri, requestParams);
        Map<String, String> headersMap = populatePostHeaders(requestHeaders.toMap());

        Response<StartScaProcessResponse> response = httpClient.post(uri)
            .headers(headersMap)
            .emptyBody(true)
            .send(requestBuilderInterceptor, jsonResponseHandler(StartScaProcessResponse.class));

        Optional.ofNullable(response.getBody())
            .ifPresent(body -> body.setLinks(linksRewriter.rewrite(body.getLinks())));

        return response;
    }

    protected <T> Response<StartScaProcessResponse> startConsentAuthorisation(String consentId,
                                                                              RequestHeaders requestHeaders,
                                                                              RequestParams requestParams,
                                                                              Class<T> klass,
                                                                              Function<T, StartScaProcessResponse> mapper) {
        requireValid(validateStartConsentAuthorisation(consentId, requestHeaders, requestParams));

        String uri = StringUri.fromElements(getConsentBaseUri(), consentId, AUTHORISATIONS);
        uri = buildUri(uri, requestParams);
        Map<String, String> headersMap = populatePostHeaders(requestHeaders.toMap());

        Response<T> response = httpClient.post(uri)
            .headers(headersMap)
            .send(requestBuilderInterceptor, jsonResponseHandler(klass));
        StartScaProcessResponse startScaProcessResponse = mapper.apply(response.getBody());
        startScaProcessResponse.setLinks(linksRewriter.rewrite(startScaProcessResponse.getLinks()));

        return new Response<>(response.getStatusCode(), startScaProcessResponse, response.getHeaders());
    }

    @Override
    public Response<StartScaProcessResponse> startConsentAuthorisation(String consentId,
                                                                       RequestHeaders requestHeaders,
                                                                       RequestParams requestParams,
                                                                       UpdatePsuAuthentication updatePsuAuthentication) {
        return startConsentAuthorisation(consentId,
            requestHeaders,
            requestParams,
            updatePsuAuthentication,
            StartScaProcessResponse.class,
            identity());
    }

    protected <T> Response<StartScaProcessResponse> startConsentAuthorisation(String consentId,
                                                                              RequestHeaders requestHeaders,
                                                                              RequestParams requestParams,
                                                                              UpdatePsuAuthentication updatePsuAuthentication,
                                                                              Class<T> klass,
                                                                              Function<T, StartScaProcessResponse> mapper) {
        requireValid(validateStartConsentAuthorisation(consentId, requestHeaders, requestParams, updatePsuAuthentication));

        String uri = StringUri.fromElements(getConsentBaseUri(), consentId, AUTHORISATIONS);
        uri = buildUri(uri, requestParams);
        Map<String, String> headersMap = populatePostHeaders(requestHeaders.toMap());
        String body = jsonMapper.writeValueAsString(updatePsuAuthentication);

        Response<T> response = httpClient.post(uri)
            .jsonBody(body)
            .headers(headersMap)
            .send(requestBuilderInterceptor, jsonResponseHandler(klass));
        StartScaProcessResponse startScaProcessResponse = mapper.apply(response.getBody());
        startScaProcessResponse.setLinks(linksRewriter.rewrite(startScaProcessResponse.getLinks()));

        return new Response<>(response.getStatusCode(), startScaProcessResponse, response.getHeaders());
    }

    @Override
    public Response<UpdatePsuAuthenticationResponse> updateConsentsPsuData(String consentId,
                                                                           String authorisationId,
                                                                           RequestHeaders requestHeaders,
                                                                           RequestParams requestParams,
                                                                           UpdatePsuAuthentication updatePsuAuthentication) {
        return updateConsentsPsuData(consentId,
            authorisationId,
            requestHeaders,
            requestParams,
            updatePsuAuthentication,
            UpdatePsuAuthenticationResponse.class,
            identity());
    }

    protected <T> Response<UpdatePsuAuthenticationResponse> updateConsentsPsuData(String consentId,
                                                                                  String authorisationId,
                                                                                  RequestHeaders requestHeaders,
                                                                                  RequestParams requestParams,
                                                                                  UpdatePsuAuthentication updatePsuAuthentication,
                                                                                  Class<T> klass,
                                                                                  Function<T, UpdatePsuAuthenticationResponse> mapper) {
        requireValid(validateUpdateConsentsPsuData(consentId, authorisationId, requestHeaders, requestParams, updatePsuAuthentication));

        String uri = StringUri.fromElements(getConsentBaseUri(), consentId, AUTHORISATIONS, authorisationId);
        uri = buildUri(uri, requestParams);
        Map<String, String> headersMap = populatePutHeaders(requestHeaders.toMap());
        headersMap = addPsuIdTypeHeader(headersMap);
        String body = jsonMapper.writeValueAsString(updatePsuAuthentication);

        Response<T> response = httpClient.put(uri)
                                   .jsonBody(body)
                                   .headers(headersMap)
                                   .send(requestBuilderInterceptor, jsonResponseHandler(klass));
        UpdatePsuAuthenticationResponse updatePsuAuthenticationResponse = mapper.apply(response.getBody());
        updatePsuAuthenticationResponse.setLinks(linksRewriter.rewrite(updatePsuAuthenticationResponse.getLinks()));

        return new Response<>(response.getStatusCode(), updatePsuAuthenticationResponse, response.getHeaders());
    }

    @Override
    public Response<SelectPsuAuthenticationMethodResponse> updateConsentsPsuData(String consentId,
                                                                                 String authorisationId,
                                                                                 RequestHeaders requestHeaders,
                                                                                 RequestParams requestParams,
                                                                                 SelectPsuAuthenticationMethod selectPsuAuthenticationMethod) {
        return updateConsentsPsuData(consentId,
            authorisationId,
            requestHeaders,
            requestParams,
            selectPsuAuthenticationMethod,
            SelectPsuAuthenticationMethodResponse.class,
            identity());
    }

    protected <T> Response<SelectPsuAuthenticationMethodResponse> updateConsentsPsuData(String consentId,
                                                                                        String authorisationId,
                                                                                        RequestHeaders requestHeaders,
                                                                                        RequestParams requestParams,
                                                                                        SelectPsuAuthenticationMethod selectPsuAuthenticationMethod,
                                                                                        Class<T> klass,
                                                                                        Function<T, SelectPsuAuthenticationMethodResponse> mapper) {
        requireValid(validateUpdateConsentsPsuData(consentId, authorisationId, requestHeaders, requestParams, selectPsuAuthenticationMethod));

        String uri = StringUri.fromElements(getConsentBaseUri(), consentId, AUTHORISATIONS, authorisationId);
        uri = buildUri(uri, requestParams);
        Map<String, String> headersMap = populatePutHeaders(requestHeaders.toMap());
        headersMap = addPsuIdTypeHeader(headersMap);
        String body = jsonMapper.writeValueAsString(selectPsuAuthenticationMethod);

        Response<T> response = httpClient.put(uri)
            .jsonBody(body)
            .headers(headersMap)
            .send(requestBuilderInterceptor, jsonResponseHandler(klass));
        SelectPsuAuthenticationMethodResponse selectPsuAuthenticationMethodResponse = mapper.apply(response.getBody());
        selectPsuAuthenticationMethodResponse.setLinks(linksRewriter.rewrite(selectPsuAuthenticationMethodResponse.getLinks()));

        return new Response<>(response.getStatusCode(), selectPsuAuthenticationMethodResponse, response.getHeaders());
    }

    @Override
    public Response<ScaStatusResponse> updateConsentsPsuData(String consentId,
                                                             String authorisationId,
                                                             RequestHeaders requestHeaders,
                                                             RequestParams requestParams,
                                                             TransactionAuthorisation transactionAuthorisation) {
        return updateConsentsPsuData(consentId,
            authorisationId,
            requestHeaders,
            requestParams,
            transactionAuthorisation,
            ScaStatusResponse.class,
            identity());
    }

    protected <T> Response<ScaStatusResponse> updateConsentsPsuData(String consentId,
                                                                    String authorisationId,
                                                                    RequestHeaders requestHeaders,
                                                                    RequestParams requestParams,
                                                                    TransactionAuthorisation transactionAuthorisation,
                                                                    Class<T> klass,
                                                                    Function<T, ScaStatusResponse> mapper) {
        requireValid(validateUpdateConsentsPsuData(consentId,
            authorisationId,
            requestHeaders,
            requestParams,
            transactionAuthorisation));

        String uri = getUpdateConsentPsuDataUri(consentId, authorisationId);
        uri = buildUri(uri, requestParams);
        Map<String, String> headersMap = populatePutHeaders(requestHeaders.toMap());
        headersMap = addPsuIdTypeHeader(headersMap);
        String body = jsonMapper.writeValueAsString(transactionAuthorisation);

        Response<T> response = httpClient.put(uri)
            .jsonBody(body)
            .headers(headersMap)
            .send(requestBuilderInterceptor, jsonResponseHandler(klass));

        ScaStatusResponse scaStatusResponse = mapper.apply(response.getBody());
        return new Response<>(response.getStatusCode(), scaStatusResponse, response.getHeaders());
    }

    protected String getUpdateConsentPsuDataUri(String consentId, String authorisationId) {
        return StringUri.fromElements(getConsentBaseUri(), consentId, AUTHORISATIONS, authorisationId);
    }

    @Override
    public Response<AccountListHolder> getAccountList(RequestHeaders requestHeaders, RequestParams requestParams) {
        requireValid(validateGetAccountList(requestHeaders, requestParams));

        Map<String, String> headersMap = populateGetHeaders(requestHeaders.toMap());
        headersMap = addConsentIdHeader(headersMap);

        String uri = buildUri(getAccountsBaseUri(), requestParams);

        Response<AccountListHolder> response = httpClient.get(uri)
            .headers(headersMap)
            .send(requestBuilderInterceptor, jsonResponseHandler(AccountListHolder.class));

        Optional.ofNullable(response.getBody())
            .map(AccountListHolder::getAccounts)
            .ifPresent(accounts ->
                accounts.forEach(account -> account.setLinks(linksRewriter.rewrite(account.getLinks()))));

        return response;
    }

    @Override
    public Response<TransactionsReport> getTransactionList(String accountId,
                                                           RequestHeaders requestHeaders,
                                                           RequestParams requestParams) {
        return getTransactionList(accountId, requestHeaders, requestParams, TransactionsReport.class, identity());
    }

    private String getTransactionListUri(String accountId, RequestParams requestParams) {
        String uri = StringUri.fromElements(getAccountsBaseUri(), accountId, TRANSACTIONS);
        uri = buildUri(uri, requestParams);
        return uri;
    }

    protected <T> Response<TransactionsReport> getTransactionList(String accountId,
                                                                  RequestHeaders requestHeaders,
                                                                  RequestParams requestParams,
                                                                  Class<T> klass,
                                                                  Function<T, TransactionsReport> mapper) {
        requireValid(validateGetTransactionList(accountId, requestHeaders, requestParams));

        Map<String, String> headersMap = populateGetHeaders(requestHeaders.toMap());
        headersMap.put(ACCEPT_HEADER, ContentType.APPLICATION_JSON);

        String uri = getTransactionListUri(accountId, requestParams);

        Response<T> response = httpClient.get(uri)
            .headers(headersMap)
            .send(requestBuilderInterceptor, jsonResponseHandler(klass));
        TransactionsReport transactionsReport = mapper.apply(response.getBody());
        logTransactionsSize(transactionsReport);

        transactionsReport.setLinks(linksRewriter.rewrite(transactionsReport.getLinks()));

        Optional.ofNullable(transactionsReport.getTransactions())
            .ifPresent(report -> {
                report.setLinks(linksRewriter.rewrite(report.getLinks()));
                rewriteTransactionsLinks(report.getBooked());
                rewriteTransactionsLinks(report.getPending());
            });

        return new Response<>(response.getStatusCode(), transactionsReport, response.getHeaders());
    }

    private void rewriteTransactionsLinks(List<Transactions> transactions) {
        Optional.ofNullable(transactions)
            .ifPresent(ts ->
                           ts.forEach(transaction -> transaction.setLinks(linksRewriter.rewrite(transaction.getLinks())))
            );
    }

    private void logTransactionsSize(TransactionsReport transactionsReport) {
        int size = 0;
        if (transactionsReport != null && transactionsReport.getTransactions() != null) {
            AccountReport transactions = transactionsReport.getTransactions();
            if (transactions.getPending() != null) {
                size += transactions.getPending().size();
            }
            if (transactions.getBooked() != null) {
                size += transactions.getBooked().size();
            }
        }
        logger.info("<-- There are {} transactions in the response", size);
    }

    @Override
    public Response<TransactionDetails> getTransactionDetails(String accountId,
                                                              String transactionId,
                                                              RequestHeaders requestHeaders,
                                                              RequestParams requestParams) {
        requireValid(validateGetTransactionDetails(accountId, transactionId, requestHeaders, requestParams));

        String uri = StringUri.fromElements(getAccountsBaseUri(), accountId, TRANSACTIONS, transactionId);
        uri = buildUri(uri, requestParams);

        Response<TransactionDetails> response = httpClient.get(uri)
            .headers(populateGetHeaders(requestHeaders.toMap()))
            .send(requestBuilderInterceptor, jsonResponseHandler(TransactionDetails.class));

        Optional.ofNullable(response.getBody())
            .map(TransactionDetails::getTransactionsDetails)
            .ifPresent(t -> t.setLinks(linksRewriter.rewrite(t.getLinks())));

        return new Response<>(response.getStatusCode(), response.getBody(), response.getHeaders());
    }

    @Override
    public Response<String> getTransactionListAsString(String accountId, RequestHeaders requestHeaders, RequestParams requestParams) {
        requireValid(validateGetTransactionListAsString(accountId, requestHeaders, requestParams));

        String uri = getTransactionListUri(accountId, requestParams);
        Map<String, String> headers = populateGetHeaders(requestHeaders.toMap());
        Response<String> response = httpClient.get(uri)
                                    .headers(headers)
                                    .send(requestBuilderInterceptor, stringResponseHandler());
        logger.info("<-- There is no information about transactions");
        return response;
    }

    @Override
    public Response<ScaStatusResponse> getConsentScaStatus(String consentId,
                                                           String authorisationId,
                                                           RequestHeaders requestHeaders,
                                                           RequestParams requestParams) {
        requireValid(validateGetConsentScaStatus(consentId, authorisationId, requestHeaders, requestParams));

        String uri = StringUri.fromElements(getConsentBaseUri(), consentId, AUTHORISATIONS, authorisationId);
        uri = buildUri(uri, requestParams);
        Map<String, String> headers = populateGetHeaders(requestHeaders.toMap());
        return httpClient.get(uri)
            .headers(headers)
            .send(requestBuilderInterceptor, jsonResponseHandler(ScaStatusResponse.class));
    }

    @Override
    public Response<BalanceReport> getBalances(String accountId,
                                               RequestHeaders requestHeaders,
                                               RequestParams requestParams) {
        return getBalances(accountId, requestHeaders, requestParams, BalanceReport.class, identity());
    }

    @Override
    public Response<CardAccountList> getCardAccountList(RequestHeaders requestHeaders, RequestParams requestParams) {
        requireValid(validateGetCardAccountList(requestHeaders, requestParams));

        String uri = StringUri.fromElements(getBaseUri(), V1, CARD_ACCOUNTS);
        uri = buildUri(uri, requestParams);
        Map<String, String> headers = populateGetHeaders(requestHeaders.toMap());
        Response<CardAccountList> response = httpClient.get(uri)
            .headers(headers)
            .send(requestBuilderInterceptor, jsonResponseHandler(CardAccountList.class));
        Optional.ofNullable(response.getBody())
            .map(CardAccountList::getCardAccounts)
            .ifPresent(accounts ->
                accounts.forEach(account ->
                    account.setLinks(linksRewriter.rewrite(account.getLinks()))));
        return response;
    }

    @Override
    public Response<CardAccountDetailsHolder> getCardAccountDetails(String accountId,
                                                                    RequestHeaders requestHeaders,
                                                                    RequestParams requestParams) {
        requireValid(validateGetCardAccountDetails(accountId, requestHeaders, requestParams));

        String uri = StringUri.fromElements(getBaseUri(), V1, CARD_ACCOUNTS, accountId);
        uri = buildUri(uri, requestParams);
        Map<String, String> headers = populateGetHeaders(requestHeaders.toMap());
        Response<CardAccountDetailsHolder> response = httpClient.get(uri)
            .headers(headers)
            .send(requestBuilderInterceptor, jsonResponseHandler(CardAccountDetailsHolder.class));
        Optional.ofNullable(response.getBody())
            .map(CardAccountDetailsHolder::getCardAccount)
            .ifPresent(account -> account.setLinks(linksRewriter.rewrite(account.getLinks())));
        return response;
    }

    @Override
    public Response<CardAccountBalanceReport> getCardAccountBalances(String accountId,
                                                                     RequestHeaders requestHeaders,
                                                                     RequestParams requestParams) {
        requireValid(validateGetCardAccountBalances(accountId, requestHeaders, requestParams));

        String uri = StringUri.fromElements(getBaseUri(), V1, CARD_ACCOUNTS, accountId, BALANCES);
        uri = buildUri(uri, requestParams);
        Map<String, String> headers = populateGetHeaders(requestHeaders.toMap());
        return httpClient.get(uri)
            .headers(headers)
            .send(requestBuilderInterceptor, jsonResponseHandler(CardAccountBalanceReport.class));
    }

    @Override
    public Response<CardAccountsTransactions> getCardAccountTransactionList(String accountId,
                                                                            RequestHeaders requestHeaders,
                                                                            RequestParams requestParams) {
        requireValid(validateGetCardAccountTransactionList(accountId, requestHeaders, requestParams));

        String uri = StringUri.fromElements(getBaseUri(), V1, CARD_ACCOUNTS, accountId, TRANSACTIONS);
        uri = buildUri(uri, requestParams);
        Map<String, String> headers = populateGetHeaders(requestHeaders.toMap());
        Response<CardAccountsTransactions> response = httpClient.get(uri)
            .headers(headers)
            .send(requestBuilderInterceptor, jsonResponseHandler(CardAccountsTransactions.class));
        CardAccountsTransactions body = response.getBody();
        if (body != null) {
            body.setLinks(linksRewriter.rewrite(body.getLinks()));
            CardAccountReport cardTransactions = body.getCardTransactions();
            if (cardTransactions != null) {
                cardTransactions.setLinks(linksRewriter.rewrite(cardTransactions.getLinks()));
            }
        }
        return response;
    }

    protected <T> Response<BalanceReport> getBalances(String accountId,
                                                      RequestHeaders requestHeaders,
                                                      RequestParams requestParams,
                                                      Class<T> klass,
                                                      Function<T, BalanceReport> mapper) {
        requireValid(validateGetBalances(accountId, requestHeaders, requestParams));

        String uri = StringUri.fromElements(getAccountsBaseUri(), accountId, BALANCES);
        uri = buildUri(uri, requestParams);
        Map<String, String> headers = populateGetHeaders(requestHeaders.toMap());
        Response<T> response = httpClient.get(uri)
            .headers(headers)
            .send(requestBuilderInterceptor, jsonResponseHandler(klass));
        BalanceReport balanceReport = mapper.apply(response.getBody());
        return new Response<>(response.getStatusCode(), balanceReport, response.getHeaders());
    }

    protected String getBaseUri() {
        return aspsp.getUrl();
    }

    protected String getIdpUri() {
        return aspsp.getIdpUrl();
    }

    protected String getConsentBaseUri() {
        return StringUri.fromElements(getBaseUri(), V1, CONSENTS);
    }

    protected String getAccountsBaseUri() {
        return StringUri.fromElements(getBaseUri(), V1, ACCOUNTS);
    }
}
