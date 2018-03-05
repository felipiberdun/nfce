package com.felipiberdun.nfcecrawler.service;

import com.felipiberdun.nfcecrawler.model.Sale;
import com.felipiberdun.nfcecrawler.model.SaleSources;

import java.util.List;

public interface SalesService {

    List<Sale> getSaleFromSources(final SaleSources saleSources);

}
