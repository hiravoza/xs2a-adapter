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

package de.adorsys.xs2a.adapter.service.provider;

import de.adorsys.xs2a.adapter.http.HttpClientFactory;
import de.adorsys.xs2a.adapter.service.AccountInformationService;
import de.adorsys.xs2a.adapter.service.PaymentInitiationService;
import de.adorsys.xs2a.adapter.service.Pkcs12KeyStore;
import de.adorsys.xs2a.adapter.service.impl.UnicreditAccountInformationService;
import de.adorsys.xs2a.adapter.service.impl.UnicreditPaymentInitiationService;
import de.adorsys.xs2a.adapter.service.link.LinksRewriter;
import de.adorsys.xs2a.adapter.service.model.Aspsp;

public class UnicreditServiceProvider implements AccountInformationServiceProvider, PaymentInitiationServiceProvider {

    @Override
    public AccountInformationService getAccountInformationService(Aspsp aspsp,
                                                                  HttpClientFactory httpClientFactory,
                                                                  Pkcs12KeyStore keyStore,
                                                                  LinksRewriter linksRewriter) {
        return new UnicreditAccountInformationService(aspsp, httpClientFactory.getHttpClient(getAdapterId()),
            linksRewriter);
    }

    @Override
    public PaymentInitiationService getPaymentInitiationService(String baseUrl,
                                                                HttpClientFactory httpClientFactory,
                                                                Pkcs12KeyStore keyStore,
                                                                LinksRewriter linksRewriter) {
        return new UnicreditPaymentInitiationService(baseUrl, httpClientFactory.getHttpClient(getAdapterId()),
            linksRewriter);
    }

    @Override
    public String getAdapterId() {
        return "unicredit-adapter";
    }
}
