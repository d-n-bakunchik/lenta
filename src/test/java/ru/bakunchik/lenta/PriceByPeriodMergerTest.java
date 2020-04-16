package ru.bakunchik.lenta;

import com.sun.org.apache.bcel.internal.generic.ANEWARRAY;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PriceByPeriodMergerTest {
    final private IPriceMerger merger = new PriceByPeriodMerger();
    final private PriceCreator creator = new PriceCreator();

    final private String [] baseJsonPrices = new String [] {
              "{id:-1, productCode:122856, number:1, depart:1, begin:2013-01-01-00-00-00, end:2013-01-31-23-59-59, value:11000}"
            , "{id:-1, productCode:122856, number:2, depart:1, begin:2013-01-10-00-00-00, end:2013-01-20-23-59-59, value:99000}"
            , "{id:-1, productCode:6654,   number:1, depart:2, begin:2013-01-01-00-00-00, end:2013-01-31-00-00-00, value:5000}"
    };

    final private String [] mergingJsonPrices = new String []
    {
              "{id:-1, productCode:122856, number:1, depart:1, begin:2013-01-20-00-00-00, end:2013-02-20-23-59-59, value:11000}"
            , "{id:-1, productCode:122856, number:2, depart:1, begin:2013-01-15-00-00-00, end:2013-01-25-23-59-59, value:92000}"
            , "{id:-1, productCode:6654,   number:1, depart:2, begin:2013-01-12-00-00-00, end:2013-01-13-00-00-00, value:4000}"
    };

    final private String [] ethalonJsonPrices = new String []
    {
              "{id:-1, productCode:122856, number:1, depart:1, begin:2013-01-01-00-00-00, end:2013-02-20-23-59-59, value:11000}"
            , "{id:-1, productCode:122856, number:2, depart:1, begin:2013-01-10-00-00-00, end:2013-01-15-00-00-00, value:99000}"
            , "{id:-1, productCode:122856, number:2, depart:1, begin:2013-01-15-00-00-00, end:2013-01-25-23-59-59, value:92000}"
            , "{id:-1, productCode:6654  , number:1, depart:2, begin:2013-01-01-00-00-00, end:2013-01-12-00-00-00, value:5000}"
            , "{id:-1, productCode:6654  , number:1, depart:2, begin:2013-01-12-00-00-00, end:2013-01-13-00-00-00, value:4000}"
            , "{id:-1, productCode:6654  , number:1, depart:2, begin:2013-01-13-00-00-00, end:2013-01-31-00-00-00, value:5000}"
    };

    @Test
    public void mergeTest() {
        System.out.println("mergeTest() ...");
        Collection<Price> basePrices = creator.createByJsonStrings(baseJsonPrices);
        Collection<Price> mergingPrices = creator.createByJsonStrings(mergingJsonPrices);
        Collection<Price> res = merger.merge(basePrices, mergingPrices);
        Collection<Price> ethalon = creator.createByJsonStrings(ethalonJsonPrices);

        assertTrue(calcEquals(res, ethalon));
    }

    @Test
    public void mergeByPriceGrouKeyInTest() {
        System.out.println("mergeByPriceGrouKeyInTest() ...");
        String [] inBaseJsonPrices = new String [] {
                 "{id:-1, productCode:6654,   number:1, depart:2, begin:2013-01-01-00-00-00, end:2013-01-31-00-00-00, value:5000}"
        };
        String inMergingJsonPrice =
                 "{id:-1, productCode:6654,   number:1, depart:2, begin:2013-01-12-00-00-00, end:2013-01-13-00-00-00, value:4000}";
       String [] inEthalonJsonPrices = new String [] {
                 "{id:-1, productCode:6654  , number:1, depart:2, begin:2013-01-01-00-00-00, end:2013-01-12-00-00-00, value:5000}"
               , "{id:-1, productCode:6654  , number:1, depart:2, begin:2013-01-12-00-00-00, end:2013-01-13-00-00-00, value:4000}"
               , "{id:-1, productCode:6654  , number:1, depart:2, begin:2013-01-13-00-00-00, end:2013-01-31-00-00-00, value:5000}"
       };

        List<Price> basePrices = creator.createByJsonStrings(inBaseJsonPrices);
        Collection<Price> res = null;
        try {
            res = merger.mergeByPriceGrouKey(basePrices, creator.createByJsonString(inMergingJsonPrice));
        } catch (IPriceMerger.PriceMergeException e) {
            e.printStackTrace();
        }
        Collection<Price> ethalon = creator.createByJsonStrings(inEthalonJsonPrices);

        assertTrue(calcEquals(res, ethalon));
    }

    @Test
    public void mergeByPriceGrouKeyBeforeTest() {
        System.out.println("mergeByPriceGrouKeyBeforeTest() ...");
        String [] beforeBaseJsonPrices = new String [] {
                 "{id:-1, productCode:6654,   number:1, depart:2, begin:2013-01-10-00-00-00, end:2013-01-31-00-00-00, value:5000}"
        };
        String beforeMergingJsonPrice =
                 "{id:-1, productCode:6654,   number:1, depart:2, begin:2013-01-05-00-00-00, end:2013-01-20-00-00-00, value:4000}";
       String [] beforeEthalonJsonPrices = new String [] {
                 "{id:-1, productCode:6654,   number:1, depart:2, begin:2013-01-05-00-00-00, end:2013-01-20-00-00-00, value:4000}"
               , "{id:-1, productCode:6654  , number:1, depart:2, begin:2013-01-20-00-00-00, end:2013-01-31-00-00-00, value:5000}"
       };

        List<Price> basePrices = creator.createByJsonStrings(beforeBaseJsonPrices);
        Collection<Price> res = null;
        try {
            res = merger.mergeByPriceGrouKey(basePrices, creator.createByJsonString(beforeMergingJsonPrice));
        } catch (IPriceMerger.PriceMergeException e) {
            e.printStackTrace();
        }
        Collection<Price> ethalon = creator.createByJsonStrings(beforeEthalonJsonPrices);

        assertTrue(calcEquals(res, ethalon));
    }

    @Test
    public void mergeByPriceGrouKeyAfterTest() {
        System.out.println("mergeByPriceGrouKeyAfterTest() ...");
        String [] afterBaseJsonPrices = new String [] {
                 "{id:-1, productCode:6654,   number:1, depart:2, begin:2013-01-01-00-00-00, end:2013-01-21-00-00-00, value:5000}"
        };
        String afterMergingJsonPrice =
                 "{id:-1, productCode:6654,   number:1, depart:2, begin:2013-01-05-00-00-00, end:2013-01-31-00-00-00, value:4000}";
       String [] afterEthalonJsonPrices = new String [] {
                 "{id:-1, productCode:6654,   number:1, depart:2, begin:2013-01-01-00-00-00, end:2013-01-05-00-00-00, value:5000}"
               , "{id:-1, productCode:6654  , number:1, depart:2, begin:2013-01-05-00-00-00, end:2013-01-31-00-00-00, value:4000}"
       };

        List<Price> basePrices = creator.createByJsonStrings(afterBaseJsonPrices);
        Collection<Price> res = null;
        try {
            res = merger.mergeByPriceGrouKey(basePrices, creator.createByJsonString(afterMergingJsonPrice));
        } catch (IPriceMerger.PriceMergeException e) {
            e.printStackTrace();
        }
        Collection<Price> ethalon = creator.createByJsonStrings(afterEthalonJsonPrices);

        assertTrue(calcEquals(res, ethalon));
    }

    private boolean calcEquals(Collection<Price> res, Collection<Price> ethalon) {
        Iterator<Price> ethalonIterator = ethalon.iterator();
        for(;ethalonIterator.hasNext();) {
            Price ethalonPrice = ethalonIterator.next();
            if (res.contains(ethalonPrice)) {
                res.remove(ethalonPrice);
                ethalonIterator.remove();
            }
        }
        System.out.println("Invalid in result: "  + res);
        System.out.println("Absent in result: "  + ethalon);
        return res.isEmpty() && ethalon.isEmpty();
    }
}