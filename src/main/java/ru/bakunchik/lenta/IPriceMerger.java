package ru.bakunchik.lenta;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * интерфейс объединения цен в группах
 */
public interface IPriceMerger {

    /**
     * Объединяет имеющиеся цены с импортированными с учетом группирующих признаков
     *
     * @param basePrices имеющиеся цены
     * @param importedPrices импортированные цены
     * @return объединенные цены
     */
    Collection<Price> merge(Collection<Price> basePrices, Collection<Price> importedPrices);

    /**
     * Объединяет имеющиеся(валидные) сгруппированные по {@link PriceKey} цены с подходящей по группировке ценой
     *
     * @param basePrices имеющиеся цены
     * @param price объединяемая цена
     * @return итоговые цены
     * @throws PriceMergeException в случае, если объединить не удалось
     */
    List<Price> mergeByPriceGrouKey(List<Price> basePrices, Price price) throws PriceMergeException;


    static class PriceMergeException extends Throwable {
        public PriceMergeException(String message) {
            super(message);
        }
    }

    final class PriceKey {
        final String productCode;
        final int number;
        final int depart;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PriceKey priceKey = (PriceKey) o;
            return number == priceKey.number &&
                    depart == priceKey.depart &&
                    Objects.equals(productCode, priceKey.productCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(productCode, number, depart);
        }

        public PriceKey(Price price) {
            this.productCode = price.getProductCode();
            this.number = price.getNumber();
            this.depart = price.getDepart();
        }
    }

    default Price cloneByPriceKey(Price price) {
        Price  clone = new Price();
        clone.setProductCode(price.getProductCode());
        clone.setDepart(price.getDepart());
        clone.setNumber(price.getNumber());
        return clone;
    }

}
