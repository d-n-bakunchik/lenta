package ru.bakunchik.lenta;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Price implements Serializable {

    private static final long serialVersionUID = -5952892727335843123L;

    private long id=-1L; // идентификатор в БД (считаем, что сохраненные в БД имеют уникальный, все остальные -1)
    private String productCode; // код товара
    private int number; // номер цены
    private int depart; // номер отдела
    private Date begin; // начало действия
    private Date end; // конец действия
    long value; // значение цены

    public Price () {
        super();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getDepart() {
        return depart;
    }

    public void setDepart(int depart) {
        this.depart = depart;
    }

    public Date getBegin() {
        return begin;
    }

    public void setBegin(Date begin) {
        this.begin = begin;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price price = (Price) o;
        return id == price.id &&
                number == price.number &&
                depart == price.depart &&
                value == price.value &&
                Objects.equals(productCode, price.productCode) &&
                Objects.equals(begin, price.begin) &&
                Objects.equals(end, price.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, productCode, number, depart, begin, end, value);
    }

    @Override
    public String toString() {
        return"{id:0" + id
                + ", productCode:" + productCode
                + ", number:" + number
                + ", depart:" + depart
                + ", begin:" + begin
                + ", end:" + end
                + ", value:" + value
                +"}";
    }

}
