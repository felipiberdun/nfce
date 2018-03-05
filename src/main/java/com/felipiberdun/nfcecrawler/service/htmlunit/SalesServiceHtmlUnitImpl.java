package com.felipiberdun.nfcecrawler.service.htmlunit;

import com.felipiberdun.nfcecrawler.model.Sale;
import com.felipiberdun.nfcecrawler.model.SaleSources;
import com.felipiberdun.nfcecrawler.service.SalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalesServiceHtmlUnitImpl implements SalesService {

    private final SaleExtractor saleExtractor;

    @Autowired
    public SalesServiceHtmlUnitImpl(final SaleExtractor saleExtractor) {
        this.saleExtractor = saleExtractor;
    }

    private Sale getSaleFromSource(final String source) {
        return saleExtractor.getSaleFromSource(source);
    }

    @Override
    public List<Sale> getSaleFromSources(final SaleSources saleSources) {
        return saleSources.getSources().stream()
                .map(this::getSaleFromSource)
                .collect(Collectors.toList());
    }
}
