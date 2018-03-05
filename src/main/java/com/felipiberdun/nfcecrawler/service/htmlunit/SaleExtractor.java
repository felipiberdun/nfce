package com.felipiberdun.nfcecrawler.service.htmlunit;

import com.felipiberdun.nfcecrawler.model.Sale;
import com.gargoylesoftware.htmlunit.WebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;

@Component
public class SaleExtractor {

    private final WebClient webClient;

    @Autowired
    public SaleExtractor(final WebClient webClient) {
        this.webClient = webClient;
    }

    Sale getSaleFromSource(final String source) {
        try {
            return SalePage.from(webClient.getPage(source)).getSale();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
