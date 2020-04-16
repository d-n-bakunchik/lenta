package ru.bakunchik.lenta;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Объединяет имеющиеся(валидные) цены с импортированными (тоже уже свалидированными)
 * по периодам действия.
 * Поддерживает только коллекции менее 100 000 элементов;
 * при большей размерности рекомендуется работа батчами
 */
public class PriceByPeriodMerger implements  IPriceMerger {

    @Override
    public Collection<Price> merge(Collection<Price> basePrices, Collection<Price> importedPrices) {
        if (basePrices.size() > 100000 || importedPrices.size() > 100000)
            throw new UnsupportedOperationException("Size of collection for merge is very large");
        //группируем [товар-отдел-номер, объекты цены]
        Map<PriceKey, List<Price>> priceKeyMap = makePriceKeyMap(basePrices);
        for (Price price : importedPrices) {
            PriceKey priceKey = new PriceKey(price);
            if(!priceKeyMap.containsKey(priceKey))
                priceKeyMap.put(priceKey, new ArrayList<>());
            List<Price> keyPrices = priceKeyMap.get(priceKey);
            //сливаем по ключу товар-отдел-номер
            try {
                keyPrices = mergeByPriceGrouKey(keyPrices, price);
            } catch (PriceMergeException e) {
                //TODO: както обозначить что за цена и почему не вмержилась
                e.printStackTrace();
            }
            priceKeyMap.put(priceKey, keyPrices);
        }
        //простейшая валидация итоговой коллекции на дубли
        Set<Price> res = new HashSet<>(priceKeyMap.size());
        for(List<Price> prices : priceKeyMap.values())
            res.addAll(prices);
        return res;
    }

//    static Date addSecunds(final Date date, final int minutesAmound) {
//        long ms = date.getTime();
//        ms += minutesAmound*1000L;
//        return new Date(ms);
//    }

    @Override
    public List<Price> mergeByPriceGrouKey(List<Price> basePrices, Price price) throws PriceMergeException{
        if (basePrices.size() == 0) {
            basePrices.add(price);
            return basePrices;
        }

        Collections.sort(basePrices, (p1,p2) -> {
            return ((Price)p1).getBegin().compareTo(((Price)p2).getBegin());
        });

        //ищем первую операцию
        ListIterator<Price> iterator = basePrices.listIterator();
        MERGE_OPERATION firstOperation = null;
        Price basePrice = null;
        while (iterator.hasNext() && (firstOperation == null || firstOperation == MERGE_OPERATION.NO_INTERSECT)) {
            basePrice = iterator.next();
            firstOperation = MERGE_OPERATION.valueOf(new Date[] {basePrice.getBegin(),basePrice.getEnd()}
                    , new Date[] {price.getBegin(),price.getEnd()});
        }

        if (firstOperation == null)
            throw new PriceMergeException("Invalid merge price: " + price);

        if (firstOperation == MERGE_OPERATION.NO_INTERSECT) {
            basePrices.add(price);
            return basePrices;
        }
        //если первая операция расширение спереди, то достаточно только ее, следующие цены начнуться позднее окончания вмерживаемой
        if(firstOperation == MERGE_OPERATION.BEFORE) {
            //если цены совпадают, расширяем базовую
            if(basePrice.getValue() == price.getValue()) {
                basePrice.setBegin(price.getBegin());
            } else {
                basePrice.setBegin(price.getEnd());
                basePrices.add(price);
            }
            return basePrices;
        }

        //если первая операция разбиение, выполняем его, дальнейшей обработки не требуется
        if(firstOperation == MERGE_OPERATION.IN) {
            //если цены совпадают, то вообще ничего не требуется
            if(price.getValue() == basePrice.getValue()) {
                return basePrices;
            }
            Date baseEnd = basePrice.getEnd();
//            basePrice.setEnd(addSecunds(price.getBegin(), -1));
            basePrice.setEnd(price.getBegin());
            iterator.add(price);
            if(baseEnd.compareTo(price.getEnd()) > 0) {
                Price addPrice = cloneByPriceKey(price);
                addPrice.setValue(basePrice.getValue());
                addPrice.setBegin(price.getEnd());
                addPrice.setEnd(baseEnd);
                iterator.add(addPrice);
            }
            return basePrices;
        }

        //если первая операция расширение с конца необходимо исктать следующую цену с операцией расширение с начала или IN
        //и удалять все цены между ними
        if(firstOperation == MERGE_OPERATION.AFTER) {
            List<Price> dels = new ArrayList<>();
            Price nextPrice = iterator.hasNext() ? iterator.next() : null;
            MERGE_OPERATION nextOperation = nextPrice != null ? MERGE_OPERATION.valueOf(new Date[] {nextPrice.getBegin(),nextPrice.getEnd()}
                    , new Date[] {price.getBegin(),price.getEnd()}) : null;
            while (nextOperation != null && nextOperation != MERGE_OPERATION.BEFORE && nextOperation != MERGE_OPERATION.REPLACE) {
                dels.add(nextPrice);
                nextPrice = iterator.hasNext() ? iterator.next() : null;
                nextOperation = nextPrice != null ? MERGE_OPERATION.valueOf(new Date[] {nextPrice.getBegin(),nextPrice.getEnd()}
                        , new Date[] {price.getBegin(),price.getEnd()}) : null;
            }

            //следующая цена не найдена - просто расширяем
            if(nextOperation == null || nextOperation == MERGE_OPERATION.NO_INTERSECT) {
                if (basePrice.getValue() == price.getValue()) {
                    basePrice.setEnd(price.getEnd());
                } else {
                    basePrice.setEnd(price.getBegin());
                    iterator.add(price);
                }
                return basePrices;
            }
            //TODO: нижеследующая часть алгоритма непротестирована ввиду отсутствия времени
            {
                if(nextOperation == MERGE_OPERATION.BEFORE) {
                    if(!dels.isEmpty())
                        basePrices.removeAll(dels);
    //                basePrice.setEnd(addSecunds(price.getBegin(), -1));
                    basePrice.setEnd(price.getBegin());
                    iterator.add(price);
    //                nextPrice.setBegin(addSecunds(price.getEnd(), 1));
                    nextPrice.setBegin(price.getEnd());
                    return basePrices;
                }

                if(nextOperation == MERGE_OPERATION.REPLACE) {
                    dels.add(nextPrice);
                    basePrices.removeAll(dels);
                    iterator.add(price);
                    return basePrices;
                }
            }
        }

        //TODO: нижеследующая часть алгоритма непротестирована ввиду отсутствия времени
        if(firstOperation == MERGE_OPERATION.REPLACE) {
            List<Price> dels = new ArrayList<>();
            Price nextPrice = iterator.hasNext() ? iterator.next() : null;
            MERGE_OPERATION nextOperation = nextPrice != null ? MERGE_OPERATION.valueOf(new Date[] {nextPrice.getBegin(),nextPrice.getEnd()}
                    , new Date[] {price.getBegin(),price.getEnd()}) : null;
            while (nextOperation != null && nextOperation != MERGE_OPERATION.BEFORE && nextOperation != MERGE_OPERATION.REPLACE) {
                dels.add(nextPrice);
                nextPrice = iterator.hasNext() ? iterator.next() : null;
                nextOperation = nextPrice != null ? MERGE_OPERATION.valueOf(new Date[] {nextPrice.getBegin(),nextPrice.getEnd()}
                        , new Date[] {price.getBegin(),price.getEnd()}) : null;
            }

            if (nextOperation == null || nextOperation == MERGE_OPERATION.NO_INTERSECT) {
                basePrices.remove(basePrice);
                basePrices.add(price);
                return basePrices;
            }

            if (nextOperation == MERGE_OPERATION.BEFORE) {
                dels.add(basePrice);
                basePrices.removeAll(dels);
                basePrices.add(price);
//                nextPrice.setBegin(addSecunds(price.getEnd(), 1));
                nextPrice.setBegin(price.getEnd());
            }
        }

        throw new PriceMergeException("Unhanled price " + price);
    }

    private Map<PriceKey, List<Price>> makePriceKeyMap(Collection<Price> prices) {
        return prices.stream().collect(Collectors.groupingBy(
                PriceKey::new, Collectors.toList())
        );
    }

    enum MERGE_OPERATION {
        REPLACE
        , BEFORE
        , IN
        , AFTER
        , NO_INTERSECT;
        static MERGE_OPERATION valueOf(Date [] basePeriod, Date [] importedPeriod) {
            if(basePeriod == null)
                return null;
            if(basePeriod[0].compareTo(importedPeriod[0]) >= 0 && basePeriod[1].compareTo(importedPeriod[1]) <= 0) {
                return REPLACE;
                //закончился ранее или начался позднее => не пересекаются
            } else if(basePeriod[1].compareTo(importedPeriod[0]) < 0 || basePeriod[0].compareTo(importedPeriod[1]) > 0)
                return NO_INTERSECT;
            //начался до начала базового и закончился после окончания  => новый внутри старого
            else if (basePeriod[0].compareTo(importedPeriod[0]) <= 0 && basePeriod[1].compareTo(importedPeriod[1]) >= 0) {
                return IN;
                //начался до начала базового и закончился раньше окончания => новый спереди
            } else if (basePeriod[0].compareTo(importedPeriod[0]) >= 0 && basePeriod[1].compareTo(importedPeriod[1]) > 0) {
                return BEFORE;
                //начался после начала базового и закончился после окончания
            } else if (basePeriod[0].compareTo(importedPeriod[0]) < 0 && basePeriod[1].compareTo(importedPeriod[1]) <= 0) {
                return AFTER;
            }
            throw new IllegalArgumentException("Unknown MERGE_OPERATION for periods: " + basePeriod.toString() + " " + importedPeriod.toString());
        }
    }
}
