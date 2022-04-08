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
package com.petpet.c3po.utils;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.MetadataRecord.Status;
import com.petpet.c3po.api.model.helper.PropertyType;
import com.petpet.c3po.dao.mongo.MongoElementSerializer;

public class DataHelperTest {
  
  private static final Logger LOG = LoggerFactory.getLogger(DataHelperTest.class);

  PersistenceLayer pLayer;

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
    helpers.DataOps.removeData();
  }


  public void shouldTestElementParsing() throws Exception {
    final PersistenceLayer p = Configurator.getDefaultConfigurator().getPersistence();

   /* final Property property = p.getCache().getProperty("mimetype");
    Source source = p.getCache().getSource("Jhove", "1.5");
    Source source2 = p.getCache().getSource("ffident", "0.2");
    final Element e = new Element("test_collection", "uid1", "name1");
    e.setId("some id");
    final MetadataRecord mr = new MetadataRecord(property.getKey(), "application/pdf");
    mr.setStatus(MetadataRecord.Status.OK.name());
    mr.setSources(Arrays.asList(source.getId(), source2.getId()));
    e.setMetadata(Arrays.asList(mr));
    
    p.insert(e);
    
    Iterator<Element> iter = p.find(Element.class, new Filter(new FilterCondition("collection", "test_collection")));
    //  get first
    Element elmnt = iter.next();
  //  Assert.assertFalse(iter.hasNext());

    
  //  Assert.assertEquals(e.getCollection(), elmnt.getCollection());
  //  Assert.assertEquals(1, e.getMetadata().size());
    
  //  Assert.assertEquals(e.getMetadata().get(0).getProperty(), elmnt.getMetadata().get(0).getProperty());

    p.remove(elmnt);*/
  }
  

  public void shouldTestElementDocumentCreation() throws Exception {
    String collection = "test";
    String uid = "testuid";
    String name = "testname";
    String key1 = "pkey1";
    String key2 = "pkey2";
    Element e = new Element(collection, uid, name);

    Property p1 = new Property(key1);
    Property p2 = new Property(key2);

  //  MetadataRecord r1 = new MetadataRecord(p1.getKey(), "42");
  //  MetadataRecord r2 = new MetadataRecord(p2.getKey(), "21");

   // e.setMetadata(Arrays.asList(r1, r2));

//     DBObject document = new MongoElementSerializer().serialize(e);

   // Assert.assertEquals(uid, document.get("uid"));
   // Assert.assertEquals(name, document.get("name"));
   // Assert.assertEquals(collection, document.get("collection"));

  }
  

  public void shouldTestElementDocumentCreationWithConflictedMetadata() throws Exception {
    String collection = "test";
    String uid = "testuid";
    String name = "testname";
    String key1 = "pkey1";
    Element e = new Element(collection, uid, name);

    Property p1 = new Property(key1);
    Source s1 = new Source("tool", "v0.1");
    Source s2 = new Source("tool", "v0.2");

    MetadataRecord r1 = new MetadataRecord(p1.getKey(), "42");
    MetadataRecord r2 = new MetadataRecord(p1.getKey(), "21");
    r1.setStatus(Status.CONFLICT.name());
    r1.setSources(Arrays.asList(s1.getId()));
    r2.setStatus(Status.CONFLICT.name());
    r2.setSources(Arrays.asList(s2.getId()));

    e.setMetadata(Arrays.asList(r1, r2));

//    DBObject document = new MongoElementSerializer().serialize(e);

 //   Assert.assertEquals(uid, document.get("uid"));
 //   Assert.assertEquals(name, document.get("name"));
 //   Assert.assertEquals(collection, document.get("collection"));

  //  DBObject meta =  document;
  //  Assert.assertNotNull(meta);
  //  Assert.assertEquals(4, meta.keySet().size());

  //  Assert.assertTrue(meta.containsField(p1.getId()));

  //  BasicDBObject value = (BasicDBObject) meta.get(p1.getId());
  //  Assert.assertNotNull(value);
    
  //  Assert.assertNull(value.get("value"));
  //  List<Object> values = (List<Object>) value.get("values");
  //  Assert.assertNotNull(values);
  //  Assert.assertEquals(1, values.size());
  }


  public void shouldTestTypedValueRetrievalForBoolean() throws Exception {
    Element test = new Element("test", "me");
    Object res = DataHelper.getTypedValue(PropertyType.BOOL.name(), "yEs");

  //  Assert.assertNotNull(res);
  //  Assert.assertTrue(res instanceof Boolean);

    res = DataHelper.getTypedValue(PropertyType.BOOL.name(), "nO");
  //  Assert.assertNotNull(res);
  //  Assert.assertTrue(res instanceof Boolean);

    res = DataHelper.getTypedValue(PropertyType.BOOL.name(), "tRuE");
  //  Assert.assertNotNull(res);
  //  Assert.assertTrue(res instanceof Boolean);

    res = DataHelper.getTypedValue(PropertyType.BOOL.name(), "FalSe");
 //   Assert.assertNotNull(res);
  //  Assert.assertTrue(res instanceof Boolean);

    res = DataHelper.getTypedValue(PropertyType.BOOL.name(), "abc");
  //  Assert.assertNotNull(res);
  //  Assert.assertTrue(res instanceof String);

    res = DataHelper.getTypedValue(PropertyType.BOOL.name(), "1");
 //   Assert.assertNotNull(res);
 //   Assert.assertTrue(res instanceof String);
  }


  public void shouldTestTypedValueRetrievalForInteger() throws Exception {
    Element test = new Element("test", "me");
    Object res = DataHelper.getTypedValue(PropertyType.INTEGER.name(), "42");
  //  Assert.assertNotNull(res);
  //  Assert.assertTrue(res instanceof Long);

    res = DataHelper.getTypedValue(PropertyType.INTEGER.name(), Integer.MAX_VALUE + "");
  //  Assert.assertNotNull(res);
 //   Assert.assertTrue(res instanceof Long);

    res = DataHelper.getTypedValue(PropertyType.INTEGER.name(), Integer.MIN_VALUE + "");
 //   Assert.assertNotNull(res);
  //  Assert.assertTrue(res instanceof Long);

    res = DataHelper.getTypedValue(PropertyType.INTEGER.name(), Long.MAX_VALUE + "");
  //  Assert.assertNotNull(res);
  //  Assert.assertTrue(res instanceof Long);

    res = DataHelper.getTypedValue(PropertyType.INTEGER.name(), "abc");
  //  Assert.assertNotNull(res);
  //  Assert.assertTrue(res instanceof String);

  }
  

  public void shouldTestTypedValueRetrievalForFloat() throws Exception {
    Element test = new Element("test", "me");
    Object res = DataHelper.getTypedValue(PropertyType.FLOAT.name(), "42");
  //  Assert.assertNotNull(res);
  //  Assert.assertTrue(res instanceof Double);

    res = DataHelper.getTypedValue(PropertyType.FLOAT.name(), "42.0");
   // Assert.assertNotNull(res);
   // Assert.assertTrue(res instanceof Double);

    res = DataHelper.getTypedValue(PropertyType.FLOAT.name(), Double.MAX_VALUE + "");
  //  Assert.assertNotNull(res);
  //  Assert.assertTrue(res instanceof Double);

    res = DataHelper.getTypedValue(PropertyType.FLOAT.name(), "abc");
  //  Assert.assertNotNull(res);
  //  Assert.assertTrue(res instanceof String);
    
  }
  

  public void shouldTestTypedValueRetrivalForDate() throws Exception {
    Element test = new Element("test", "me");
    Object res = DataHelper.getTypedValue(PropertyType.DATE.name(), "20121221122121");
  //  Assert.assertNotNull(res);
  //  Assert.assertTrue(res instanceof Date);
    
    res = DataHelper.getTypedValue(PropertyType.DATE.name(), "1338474281528");
  //  Assert.assertNotNull(res);
  //  Assert.assertTrue(res instanceof Date);
    
    res = DataHelper.getTypedValue(PropertyType.DATE.name(), "blah");
  //  Assert.assertNotNull(res);
  //  Assert.assertTrue(res instanceof String);
    
  }
  
}
