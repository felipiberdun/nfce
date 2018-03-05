package com.felipiberdun.nfcecrawler.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class Item {

    private Product product;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal total;

}
