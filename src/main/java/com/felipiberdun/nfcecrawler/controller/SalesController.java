package com.felipiberdun.nfcecrawler.controller;

import com.felipiberdun.nfcecrawler.model.Sale;
import com.felipiberdun.nfcecrawler.model.SaleSources;
import com.felipiberdun.nfcecrawler.service.SalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController(value = "/sales")
public class SalesController {

    private final SalesService salesService;

    @Autowired
    public SalesController(final SalesService salesService) {
        this.salesService = salesService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Sale> getSaleFromSource(@RequestBody final SaleSources saleSources) {
        return salesService.getSaleFromSources(saleSources);
    }
}
