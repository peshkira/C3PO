/*******************************************************************************
 * Copyright 2013 Petar Petrov <me@petarpetrov.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.petpet.c3po.dao;

import com.mongodb.*;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.PropertyType;
import com.petpet.c3po.dao.mongo.MongoPersistenceLayer;
import com.petpet.c3po.utils.Configurator;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.Assert.assertTrue;

public class MongoPersistenceLayerTest {
    PersistenceLayer pLayer;
    final Logger LOG = LoggerFactory.getLogger(MongoPersistenceLayerTest.class);

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        helpers.DataOps.removeData();
    }


    public void shouldTestFind() {
        if (this.pLayer.isConnected()) {
            Iterator<Element> iter = pLayer.find(Element.class, null);
            List<Element> elements = new ArrayList<Element>();
            while (iter.hasNext()) {
                elements.add(iter.next());
            }
            Assert.assertEquals(5, elements.size());
        }
    }


    public void shouldTestFindOne() throws Exception {
        //if (this.pLayer.isConnected()) {

           // this.insertTestData();

            Iterator<Element> find = pLayer.find(Element.class, new Filter(new FilterCondition("uid", "/home/petrov/taverna/tmp/303/303034.csv")));

            Assert.assertTrue(find.hasNext());

            Element next = find.next();
            Assert.assertEquals("/home/petrov/taverna/tmp/303/303034.csv", next.getUid());

            Assert.assertFalse(find.hasNext());
       // }
    }


    public void shouldTestRemoveAll() throws Exception {
        if (this.pLayer.isConnected()) {
            //  this.insertTestData();

            pLayer.remove(Element.class, null);
            Iterator<Element> find = pLayer.find(Element.class, null);

            Assert.assertFalse(find.hasNext());
        }
    }


    public void shouldTestRemoveOne() throws Exception {
        if (this.pLayer.isConnected()) {
            //   this.insertTestData();

            Iterator<Element> find = pLayer.find(Element.class, null);
            Element next = find.next();

            this.pLayer.remove(next);

            find = pLayer.find(Element.class, null);

            List<Element> elements = new ArrayList<Element>();
            while (find.hasNext()) {
                elements.add(find.next());
            }

            //     Assert.assertEquals(4, elements.size());
        }
    }


    public void shouldTestInsert() throws Exception {

            Iterator<Element> iter = pLayer.find(Element.class, null);
            iter = pLayer.find(Element.class, null);
            assertTrue(iter.hasNext());
    }


    public void shouldTestUpdate() throws Exception {
            Filter element1 = new Filter(new FilterCondition("uid", "/home/petrov/taverna/tmp/303/303034.csv"));
            Iterator<Element> iter = this.pLayer.find(Element.class, element1);
            Assert.assertTrue(iter.hasNext());

            Element e = iter.next();
            Assert.assertEquals("303034.csv", e.getName());
            String updated = "Updated Name";

            e.setName(updated);

            this.pLayer.update(e, element1);

            iter = this.pLayer.find(Element.class, element1);
            Assert.assertTrue(iter.hasNext());
            e = iter.next();

            Assert.assertEquals(updated, e.getName());
    }


    public void shouldTestUpdateAll() throws Exception {


            Filter filter = new Filter(new FilterCondition("collection", "test"));
            List<Element> elements = new ArrayList<Element>();
            Iterator<Element> iter = this.pLayer.find(Element.class, filter);

            while (iter.hasNext()) {
                elements.add(iter.next());
            }

            Element upadtedElement = new Element("changed", "", "");

            this.pLayer.update(upadtedElement, filter);

            iter = this.pLayer.find(Element.class, filter);

            while (iter.hasNext()) {
                Element e = iter.next();
                Assert.assertEquals("test", e.getCollection());
            }

    }


    public void shouldTestNumericAggregation() throws Exception {
        if (this.pLayer.isConnected()) {
            // this.insertTestData();
            Property property = this.pLayer.getCache().getProperty("pagecount");
            // NumericStatistics numericStatistics = this.pLayer.getNumericStatistics(property, new Filter(new FilterCondition(
            //     "collection", "test")));

            //    Assert.assertEquals(3, numericStatistics.getCount());
            //    Assert.assertEquals(42D, numericStatistics.getAverage());
            //    Assert.assertEquals(42D, numericStatistics.getMax());
            //    Assert.assertEquals(42D, numericStatistics.getMin());
            //   Assert.assertEquals(0D, numericStatistics.getStandardDeviation());
            //   Assert.assertEquals(0D, numericStatistics.getVariance());
        }
    }



    public void shouldTestAggregation() throws Exception {

        MongoPersistenceLayer pLayer = (MongoPersistenceLayer) this.pLayer;
        List<BasicDBObject> aggregationResult = pLayer.aggregate("created", null, false);
        Map<String, Long> histograms = pLayer.parseAggregation(aggregationResult , "created",false);
       // Assert.assertEquals(new Long(0),histograms.get("std"));

    }


    public void shouldDebugAggregation() throws Exception {


        MongoPersistenceLayer pLayer = (MongoPersistenceLayer) this.pLayer;
        DBCollection collection = pLayer.getCollection(Element.class);


        List<DBObject> list = new ArrayList<DBObject>();
        //first we unwind the array with metadata records
        list.add(new BasicDBObject("$unwind", "$metadata"));
        //then we find records which describe the property of interest, e.g. created
        list.add(new BasicDBObject("$match", new BasicDBObject("metadata.property", "created")));

        BasicDBList arrayElemAt = new BasicDBList();
        arrayElemAt.add("$metadata.sourcedValues");
        arrayElemAt.add(0);

        list.add(new BasicDBObject("$project", new BasicDBObject("status", "$metadata.status").append("property", "$metadata.property").append("sourcedValue", new BasicDBObject("$arrayElemAt", arrayElemAt))));
        list.add(new BasicDBObject("$project", new BasicDBObject("value", new BasicDBObject("$year", "$sourcedValue.value")).append("property",1).append("source", "$sourcedValue.source").append("status",1)));//  new BasicDBObject("$cond", cond)).append("property", 1)));
        //list.add(new BasicDBObject("$project", new BasicDBObject("value", "$sourcedValue.value").append("property", 1).append("status",1).append("source",1)));



        BasicDBList cond = new BasicDBList();
        BasicDBList eq = new BasicDBList();
        eq.add("$status");
        eq.add("CONFLICT");
        cond.add(new BasicDBObject("$eq", eq));
        cond.add("CONFLICT");
        cond.add("$value");
        //list.add(new BasicDBObject("$project", new BasicDBObject("value", new BasicDBObject("$cond", cond)).append("property", 1)));

        AggregationOutput aggregate = collection.aggregate(list);


    }


    public void shouldTestCountConflicts() throws Exception{
        MongoPersistenceLayer pLayer = (MongoPersistenceLayer) this.pLayer;
        List<String> props=new ArrayList<String>();
        props.add("format");
        props.add("format_version");
        props.add("creating_application_version");
        long result = pLayer.countConflicts(null, props);

        Assert.assertEquals(3,result);
    }


    public void SuperMapReduceTest() throws Exception {

        Map<String, List<Integer>> binThresholds = new HashMap<String, List<Integer>>();
        List<Integer> bins = new ArrayList<Integer>();
        bins.add(5);
        bins.add(20);
        bins.add(40);
        bins.add(100);
        bins.add(1000);
        bins.add(10000);
        bins.add(10000000);
        binThresholds.put("size", bins);
        binThresholds.put("wordcount", bins);
        binThresholds.put("pagecount", bins);

        List<String> properties = new ArrayList<String>();
        properties.add("mimetype");
        properties.add("format");
        properties.add("wordcount");
        Map<String, Map<String, Long>> stringMapMap = pLayer.getHistograms(properties, null, binThresholds);
        org.junit.Assert.assertEquals(stringMapMap.size(), 3);


    }

}
