package de.adorsys.xs2a.tpp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.adorsys.xs2a.adapter.api.PaymentApi;
import de.adorsys.xs2a.adapter.mapper.*;
import de.adorsys.xs2a.adapter.model.*;
import de.adorsys.xs2a.adapter.service.PaymentInitiationService;
import de.adorsys.xs2a.adapter.service.RequestHeaders;
import de.adorsys.xs2a.adapter.service.RequestParams;
import de.adorsys.xs2a.adapter.service.Response;
import de.adorsys.xs2a.adapter.service.model.*;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PaymentController extends AbstractController implements PaymentApi {
    private final PaymentInitiationService paymentService;
    private final PaymentInitiationScaStatusResponseMapper paymentInitiationScaStatusResponseMapper;
    private final HeadersMapper headersMapper;

    private final PaymentInitiationRequestResponseMapper paymentInitiationRequestResponseMapper = Mappers.getMapper(PaymentInitiationRequestResponseMapper.class);
    private final SinglePaymentInformationMapper singlePaymentInformationMapper = Mappers.getMapper(SinglePaymentInformationMapper.class);
    private final PaymentInitiationStatusMapper paymentInitiationStatusMapper = Mappers.getMapper(PaymentInitiationStatusMapper.class);
    private final PaymentInitiationAuthorisationResponseMapper paymentInitiationAuthorisationResponseMapper = Mappers.getMapper(PaymentInitiationAuthorisationResponseMapper.class);

    public PaymentController(PaymentInitiationService paymentService,
                             PaymentInitiationScaStatusResponseMapper paymentInitiationScaStatusResponseMapper,
                             HeadersMapper headersMapper,
                             ObjectMapper objectMapper) {
        super(objectMapper);
        this.paymentService = paymentService;
        this.paymentInitiationScaStatusResponseMapper = paymentInitiationScaStatusResponseMapper;
        this.headersMapper = headersMapper;
    }

    @Override
    public ResponseEntity<PaymentInitationRequestResponse201TO> initiatePayment(PaymentServiceTO paymentService,
                                                                                PaymentProductTO paymentProduct,
                                                                                Map<String, String> parameters,
                                                                                Map<String, String> headers,
                                                                                ObjectNode body) {
        return initiatePaymentInternal(paymentService, paymentProduct, parameters, headers, body);
    }

    private ResponseEntity<PaymentInitationRequestResponse201TO> initiatePaymentInternal(PaymentServiceTO paymentService,
                                                                                         PaymentProductTO paymentProduct,
                                                                                         Map<String, String> parameters,
                                                                                         Map<String, String> headers,
                                                                                         Object body) {
        requireSinglePayment(paymentService);
        RequestHeaders requestHeaders = RequestHeaders.fromMap(headers);
        RequestParams requestParams = RequestParams.fromMap(parameters);

        Response<PaymentInitiationRequestResponse> response =
            this.paymentService.initiateSinglePayment(paymentProduct.toString(),
                requestHeaders,
                requestParams,
                body);

        return ResponseEntity.status(HttpStatus.CREATED)
                       .headers(headersMapper.toHttpHeaders(response.getHeaders()))
                       .body(paymentInitiationRequestResponseMapper.toPaymentInitationRequestResponse201TO(response.getBody()));
    }

    private void requireSinglePayment(PaymentServiceTO paymentService) {
        if (paymentService != PaymentServiceTO.PAYMENTS) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public ResponseEntity<PaymentInitationRequestResponse201TO> initiatePayment(PaymentServiceTO paymentService,
                                                                                PaymentProductTO paymentProduct,
                                                                                Map<String, String> parameters,
                                                                                Map<String, String> headers,
                                                                                String body) {
        return initiatePaymentInternal(paymentService, paymentProduct, parameters, headers, body);
    }

    @Override
    public ResponseEntity<Object> getPaymentInformation(PaymentServiceTO paymentService,
                                                        PaymentProductTO paymentProduct,
                                                        String paymentId,
                                                        Map<String, String> parameters,
                                                        Map<String, String> headers) {
        RequestHeaders requestHeaders = RequestHeaders.fromMap(headers);
        RequestParams requestParams = RequestParams.fromMap(parameters);

        Response<SinglePaymentInitiationInformationWithStatusResponse> response =
            this.paymentService.getSinglePaymentInformation(paymentProduct.toString(),
                paymentId,
                requestHeaders,
                requestParams);

        return ResponseEntity.status(HttpStatus.OK)
                       .headers(headersMapper.toHttpHeaders(response.getHeaders()))
                       .body(singlePaymentInformationMapper.toPaymentInitiationSctWithStatusResponse(response.getBody()));
    }

    @Override
    public ResponseEntity<ScaStatusResponseTO> getPaymentInitiationScaStatus(PaymentServiceTO paymentService,
                                                                             PaymentProductTO paymentProduct,
                                                                             String paymentId,
                                                                             String authorisationId,
                                                                             Map<String, String> parameters,
                                                                             Map<String, String> headers) {
        RequestHeaders requestHeaders = RequestHeaders.fromMap(headers);
        RequestParams requestParams = RequestParams.fromMap(parameters);

        Response<PaymentInitiationScaStatusResponse> response =
            this.paymentService.getPaymentInitiationScaStatus(paymentService.toString(),
                paymentProduct.toString(),
                paymentId,
                authorisationId,
                requestHeaders,
                requestParams);

        return ResponseEntity.status(HttpStatus.OK)
                       .headers(headersMapper.toHttpHeaders(response.getHeaders()))
                       .body(paymentInitiationScaStatusResponseMapper.mapToScaStatusResponse(response.getBody()));
    }

    @Override
    public ResponseEntity<Object> getPaymentInitiationStatus(PaymentServiceTO paymentService,
                                                             PaymentProductTO paymentProduct,
                                                             String paymentId,
                                                             Map<String, String> parameters,
                                                             Map<String, String> headers) {
        RequestHeaders requestHeaders = RequestHeaders.fromMap(headers);
        RequestParams requestParams = RequestParams.fromMap(parameters);

        if (requestHeaders.isAcceptJson()) {
            Response<PaymentInitiationStatus> response =
                this.paymentService.getSinglePaymentInitiationStatus(paymentProduct.toString(),
                    paymentId,
                    requestHeaders,
                    requestParams);

            return ResponseEntity.status(HttpStatus.OK)
                           .headers(headersMapper.toHttpHeaders(response.getHeaders()))
                           .body(paymentInitiationStatusMapper.toPaymentInitiationStatusResponse200Json(response.getBody()));
        }

        Response<String> response =
            this.paymentService.getSinglePaymentInitiationStatusAsString(paymentProduct.toString(),
                paymentId,
                requestHeaders,
                requestParams);

        return ResponseEntity
                       .status(HttpStatus.OK)
                       .headers(headersMapper.toHttpHeaders(response.getHeaders()))
                       .body(response.getBody());
    }

    @Override
    public ResponseEntity<AuthorisationsTO> getPaymentInitiationAuthorisation(PaymentServiceTO paymentService,
                                                                              PaymentProductTO paymentProduct,
                                                                              String paymentId,
                                                                              Map<String, String> parameters,
                                                                              Map<String, String> headers) {
        RequestHeaders requestHeaders = RequestHeaders.fromMap(headers);
        RequestParams requestParams = RequestParams.fromMap(parameters);

        Response<PaymentInitiationAuthorisationResponse> response =
            this.paymentService.getPaymentInitiationAuthorisation(paymentService.toString(),
                paymentProduct.toString(),
                paymentId,
                requestHeaders,
                requestParams);

        return ResponseEntity.status(HttpStatus.OK)
                       .headers(headersMapper.toHttpHeaders(response.getHeaders()))
                       .body(paymentInitiationAuthorisationResponseMapper.toAuthorisationsTO(response.getBody()));
    }

    @Override
    public ResponseEntity<StartScaprocessResponseTO> startPaymentAuthorisation(PaymentServiceTO paymentService,
                                                                               PaymentProductTO paymentProduct,
                                                                               String paymentId,
                                                                               Map<String, String> parameters,
                                                                               Map<String, String> headers,
                                                                               ObjectNode body) {
        RequestHeaders requestHeaders = RequestHeaders.fromMap(headers);
        RequestParams requestParams = RequestParams.fromMap(parameters);

        Response<?> response = handleAuthorisationBody(body,
                (UpdatePsuAuthenticationHandler) updatePsuAuthentication ->
                    this.paymentService.startSinglePaymentAuthorisation(paymentProduct.toString(),
                        paymentId,
                        requestHeaders,
                        requestParams,
                        updatePsuAuthentication)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .headers(headersMapper.toHttpHeaders(response.getHeaders()))
                .body(startScaProcessResponseMapper.toStartScaprocessResponseTO((StartScaProcessResponse) response.getBody()));
    }

    @Override
    public ResponseEntity<Object> updatePaymentPsuData(PaymentServiceTO paymentService,
                                                       PaymentProductTO paymentProduct,
                                                       String paymentId,
                                                       String authorisationId,
                                                       Map<String, String> parameters,
                                                       Map<String, String> headers,
                                                       ObjectNode body) {
        RequestHeaders requestHeaders = RequestHeaders.fromMap(headers);
        RequestParams requestParams = RequestParams.fromMap(parameters);

        Response<?> response = handleAuthorisationBody(body,
            (UpdatePsuAuthenticationHandler) updatePsuAuthentication ->
                this.paymentService.updatePaymentPsuData(paymentService.toString(),
                    paymentProduct.toString(),
                    paymentId,
                    authorisationId,
                    requestHeaders,
                    requestParams,
                    updatePsuAuthentication),
            (SelectPsuAuthenticationMethodHandler) selectPsuAuthenticationMethod ->
                this.paymentService.updatePaymentPsuData(paymentService.toString(),
                    paymentProduct.toString(),
                    paymentId,
                    authorisationId,
                    requestHeaders,
                    requestParams,
                    selectPsuAuthenticationMethod),
            (TransactionAuthorisationHandler) transactionAuthorisation ->
                this.paymentService.updatePaymentPsuData(paymentService.toString(),
                    paymentProduct.toString(),
                    paymentId,
                    authorisationId,
                    requestHeaders,
                    requestParams,
                    transactionAuthorisation)
        );

        return ResponseEntity
            .status(HttpStatus.OK)
            .headers(headersMapper.toHttpHeaders(response.getHeaders()))
            .body(response.getBody());
    }
}

