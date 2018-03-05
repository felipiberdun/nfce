package com.felipiberdun.nfcecrawler.service.htmlunit;

import com.felipiberdun.nfcecrawler.model.*;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class SalePage {

    private static final String XPATH_SELLER = ".//div[@id='conteudo']/div[@id='avisos']/following-sibling::div[1]";
    private static final String XPATH_SELLER_NAME = XPATH_SELLER + "/div[1]";
    private static final String XPATH_SELLER_DOCUMENT = XPATH_SELLER + "/div[2]";
    private static final String XPATH_SELLER_ADDRESS = XPATH_SELLER + "/div[3]";
    private static final String XPATH_COSTUMER = ".//div[@id='infos']/div[h4[contains(., 'Consumidor')]]";
    private static final String XPATH_COSTUMER_CPF = XPATH_COSTUMER + "//li[contains(., 'CPF')]";
    private static final String XPATH_ITEMS = ".//div[@id='conteudo']/table//tr[contains(@id, 'Item')]";
    private static final String XPATH_SPAN_PROD_INFO = ".//span[@class='%s']";
    private static final String XPATH_PRODUCT_ID = String.format(XPATH_SPAN_PROD_INFO, "RCod");
    private static final String XPATH_PRODUCT_NAME = String.format(XPATH_SPAN_PROD_INFO, "txtTit");
    private static final String XPATH_ITEM_QUANTITY = String.format(XPATH_SPAN_PROD_INFO, "Rqtd");
    private static final String XPATH_ITEM_UNIT = String.format(XPATH_SPAN_PROD_INFO, "RUN");
    private static final String XPATH_ITEM_UNIT_PRICE = String.format(XPATH_SPAN_PROD_INFO, "RvlUnit");
    private static final String XPATH_ITEM_TOTAL = ".//td[@class='txtTit noWrap']/span";
    private static final String XPATH_INFO_NFE = ".//div[@id='infos']/div[h4[contains(., 'Informações gerais da Nota')]]//li[contains(., 'Emissão')]";
    private static final String XPATH_ACCESS_KEY = ".//div[@id='infos']/div[h4[contains(., 'Chave de acesso')]]//span[@class='chave']";
    private static final String XPATH_SUBTOTAL = ".//div[@id='conteudo']/div[@id='totalNota']/div[contains(., 'Valor total')]/span";
    private static final String XPATH_DISCOUNT = ".//div[@id='conteudo']/div[@id='totalNota']/div[contains(., 'Descontos')]/span";
    private static final String XPATH_TOTAL = ".//div[@id='conteudo']/div[@id='totalNota']/div[contains(., 'Valor a pagar')]/span";

    private static final Pattern SELLER_DOCUMENT_PATTERN = Pattern.compile("\\d{2}\\.\\d{3}\\.\\d{3}\\/\\d{4}-\\d{2}");
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("\\(Código:\\s?(\\w+)\\)");
    private static final Pattern QUANTITY_PATTERN = Pattern.compile("Qtde\\.:\\s*([\\d,\\.]+)");
    private static final Pattern UNIT_PATTERN = Pattern.compile("UN:\\s*(\\w+)");
    private static final Pattern UNIT_PRICE_PATTERN = Pattern.compile("(\\d{1,}[\\d\\.,]*)");
    private static final Pattern ITEM_TOTAL_PATTERN = Pattern.compile("(\\d{1,}[\\d\\.,]*)");
    private static final Pattern CPF_PATTERN = Pattern.compile("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("Número:[\\s\\t\\r]*(\\d+)");
    private static final Pattern SERIES_PATTERN = Pattern.compile("Série:[\\s\\t\\r]*(\\d+)");
    private static final Pattern DATE_PATTERN = Pattern.compile("Emissão:[\\s\\t\\r]*(\\d{1,2}\\/\\d{1,2}\\/\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2})");

    private static final DateTimeFormatter BRAZILIAN_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final int FIRST_GROUP = 1;

    private final HtmlPage htmlPage;

    private SalePage(final HtmlPage htmlPage) {
        this.htmlPage = htmlPage;
    }

    static SalePage from(final HtmlPage htmlPage) {
        assert htmlPage != null;

        return new SalePage(htmlPage);
    }

    Sale getSale() {
        final Sale sale = new Sale();
        sale.setSource(htmlPage.getWebResponse().getWebRequest().getUrl().toString());
        sale.setSeller(getSellerInformation());
        sale.setCustomer(getCustumerInformation());
        sale.setItems(getItemsInformation());

        sale.setSeries(getSeries());
        sale.setNumber(getNumber());
        sale.setDate(getDate());
        sale.setAccessKey(getAccessKey());
        sale.setSubTotal(getSubTotal());
        sale.setDiscount(getDiscount());
        sale.setTotal(getTotal());

        return sale;
    }

    private String getSeries() {
        return applyRegex(maybeExtractFromNode(XPATH_INFO_NFE), SERIES_PATTERN, FIRST_GROUP)
                .orElse(null);
    }

    private String getNumber() {
        return applyRegex(maybeExtractFromNode(XPATH_INFO_NFE), NUMBER_PATTERN, FIRST_GROUP)
                .orElse(null);
    }

    private LocalDateTime getDate() {
        return applyRegex(maybeExtractFromNode(XPATH_INFO_NFE), DATE_PATTERN, FIRST_GROUP)
                .map(text -> LocalDateTime.parse(text, BRAZILIAN_DATE_TIME_FORMAT))
                .orElse(null);
    }

    private String getAccessKey() {
        return maybeExtractFromNode(XPATH_ACCESS_KEY)
                .map(text -> text.replaceAll("\\s", ""))
                .orElse(null);
    }

    private BigDecimal getSubTotal() {
        return maybeExtractFromNode(XPATH_SUBTOTAL)
                .map(this::formatDecimalToBigDecimal)
                .map(BigDecimal::new)
                .orElse(null);
    }

    private BigDecimal getDiscount() {
        return maybeExtractFromNode(XPATH_DISCOUNT)
                .map(this::formatDecimalToBigDecimal)
                .map(BigDecimal::new)
                .orElse(null);
    }

    private BigDecimal getTotal() {
        return maybeExtractFromNode(XPATH_TOTAL)
                .map(this::formatDecimalToBigDecimal)
                .map(BigDecimal::new)
                .orElse(null);
    }

    private List<Item> getItemsInformation() {
        return htmlPage.<HtmlTableRow>getByXPath(XPATH_ITEMS).stream()
                .map(this::getItemInformation)
                .collect(Collectors.toList());
    }

    private Item getItemInformation(final HtmlTableRow tr) {
        final Item item = new Item();
        item.setProduct(getProductInformation(tr));
        item.setQuantity(getItemQuantity(tr));
        item.setUnit(getItemUnit(tr));
        item.setUnitPrice(getUnitPrice(tr));
        item.setTotal(getItemTotal(tr));

        return item;
    }

    private BigDecimal getItemTotal(final HtmlTableRow tr) {
        return applyRegex(maybeExtractFromNode(tr, XPATH_ITEM_TOTAL), ITEM_TOTAL_PATTERN, FIRST_GROUP)
                .map(this::formatDecimalToBigDecimal)
                .map(BigDecimal::new)
                .orElse(null);
    }

    private BigDecimal getUnitPrice(final HtmlTableRow tr) {
        return applyRegex(maybeExtractFromNode(tr, XPATH_ITEM_UNIT_PRICE), UNIT_PRICE_PATTERN, FIRST_GROUP)
                .map(this::formatDecimalToBigDecimal)
                .map(BigDecimal::new)
                .orElse(null);
    }

    private String getItemUnit(final HtmlTableRow tr) {
        return applyRegex(maybeExtractFromNode(tr, XPATH_ITEM_UNIT), UNIT_PATTERN, FIRST_GROUP)
                .orElse(null);
    }

    private BigDecimal getItemQuantity(final HtmlTableRow tr) {
        return applyRegex(maybeExtractFromNode(tr, XPATH_ITEM_QUANTITY), QUANTITY_PATTERN, FIRST_GROUP)
                .map(this::formatDecimalToBigDecimal)
                .map(BigDecimal::new)
                .orElse(null);
    }

    private Product getProductInformation(final HtmlTableRow tr) {
        final Product product = new Product();
        product.setId(getProductId(tr));
        product.setName(getProductName(tr));

        return product;
    }

    private String getProductName(final HtmlTableRow tr) {
        return extractFromNodeOrNull(tr, XPATH_PRODUCT_NAME);
    }

    private String getProductId(final HtmlTableRow tr) {
        return applyRegex(maybeExtractFromNode(tr, XPATH_PRODUCT_ID), PRODUCT_ID_PATTERN, FIRST_GROUP)
                .orElse(null);
    }

    private Customer getCustumerInformation() {
        final Customer customer = new Customer();
        customer.setDocument(getCustomerDocument());

        return customer;
    }

    private String getCustomerDocument() {
        return applyRegex(maybeExtractFromNode(XPATH_COSTUMER_CPF), CPF_PATTERN, null)
                .orElse(null);
    }

    private Seller getSellerInformation() {
        final Seller seller = new Seller();
        seller.setName(getSellerName());
        seller.setDocument(getSellerDocument());
        seller.setFullAddress(getSellerAddress());

        return seller;
    }

    private String getSellerName() {
        return extractFromNodeOrNull(XPATH_SELLER_NAME);
    }

    private String getSellerDocument() {
        return applyRegex(maybeExtractFromNode(XPATH_SELLER_DOCUMENT), SELLER_DOCUMENT_PATTERN, null)
                .orElse(null);
    }

    private String getSellerAddress() {
        return maybeExtractFromNode(XPATH_SELLER_ADDRESS)
                .map(this::removeUnecessarySpace)
                .orElse(null);
    }

    private String formatDecimalToBigDecimal(final String content) {
        return content.replaceAll("\\.", "").replace(",", ".");
    }

    private String removeUnecessarySpace(final String content) {
        return content == null ? null : content.replaceAll("\\s{2,}", " ");
    }

    private Optional<String> maybeExtractFromNode(final String xPath) {
        return maybeExtractFromNode(htmlPage, xPath);
    }

    private Optional<String> maybeExtractFromNode(final DomNode startingNode, final String xPath) {
        return Optional.ofNullable(startingNode.<DomNode>getFirstByXPath(xPath))
                .map(DomNode::getTextContent);
    }

    private String extractFromNodeOrNull(final String divXPath) {
        return maybeExtractFromNode(divXPath).orElse(null);
    }

    private String extractFromNodeOrNull(final DomNode startingNode, final String divXPath) {
        return maybeExtractFromNode(startingNode, divXPath).orElse(null);
    }

    private Optional<String> applyRegex(final Optional<String> content, final Pattern regex, final Integer group) {
        final Optional<Matcher> filteredContent = content
                .map(regex::matcher)
                .filter(Matcher::find);

        if (group == null || group == 0) {
            return filteredContent.map(Matcher::group);
        }

        return filteredContent.map(m -> m.group(group));
    }

}
