package com.felipiberdun.nfcecrawler.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class Sale {

    private String source;
    private Seller seller;
    private Customer customer;
    private List<Item> items;
    private String series;
    private String number;
    private LocalDateTime date;
    private String accessKey;
    private BigDecimal subTotal;
    private BigDecimal discount;
    private BigDecimal total;

}
