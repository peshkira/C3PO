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
package com.petpet.c3po.dao.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.BetweenFilterCondition;
import com.petpet.c3po.api.model.helper.BetweenFilterCondition.Operator;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.filtering.PropertyFilterCondition;
import com.petpet.c3po.utils.Configurator;

import java.util.*;

/**
 * The {@link MongoFilterSerializer} translates a filter object to a
 * {@link DBObject} query, so that the dataset is first filtered and then the
 * persistence layer function is applied.
 *
 * @author Petar Petrov <me@petarpetrov.org>
 */
public class MongoFilterSerializer {

    /**
     * A list of properties to exclude from wrapping. This is mongo specific.
     */
    private static final String[] EXCLUDE = {"_id", "uid", "collection", "name", "key", "version"};

    /**
     * A static exists query for Mongo.
     */
    private static final BasicDBObject EXISTS = new BasicDBObject("$exists", true);
    private static final BasicDBObject NOTEXISTS = new BasicDBObject("$exists", false);

    /**
     * Serializes the given filter according to the strategy proposed here:
     * {@link filtering}. If the filter is null, then an empty {@link DBObject} is
     * returned.
     *
     * @param filter the filter to serialize.
     * @return the Mongo {@link DBObject}
     */
    public DBObject serialize(Filter filter) {
        DBObject result = new BasicDBObject();

        if (filter != null) {
            if (filter.getRaw() != null) {
                Object o = com.mongodb.util.JSON.parse(filter.getRaw());
                return (DBObject) o;
            }

            List<FilterCondition> conditions = filter.getConditions();

            Map<String, Integer> distinctFields = this.getDistinctFields(conditions);
            List<BasicDBObject> and = new ArrayList<BasicDBObject>();

            for (String field : distinctFields.keySet()) {

                if (distinctFields.get(field) == 1) {

                    List<BasicDBObject> andQuery = getAndQuery(field, conditions.toArray(new FilterCondition[conditions.size()]));
                    and.addAll(andQuery);

                } else {

                    BasicDBObject orQuery = this.getOrQuery(conditions, field);
                    and.add(orQuery);

                }

            }

            if (and.size() > 0) {
                result.put("$and", and);
            }

        }

        return result;
    }


    public DBObject serializeNew(Filter filter) {
        DBObject result = new BasicDBObject();

        if (filter != null) {
            if (filter.getRaw() != null) {
                Object o = com.mongodb.util.JSON.parse(filter.getRaw());
                return (DBObject) o;
            }
        }

        List<PropertyFilterCondition> propertyFilterConditions = filter.getPropertyFilterConditions();
        List<BasicDBObject> and = new ArrayList<BasicDBObject>();

        for (PropertyFilterCondition propertyFilterCondition : propertyFilterConditions) {
            and.addAll(getSourcedValueList(propertyFilterCondition.getSourcedValues()));
            and.addAll(getPropertyList(propertyFilterCondition.getProperty())) ;
            and.addAll(getStatusList(propertyFilterCondition.getStatuses(), "$or"));
            and.addAll(getSourceList(propertyFilterCondition.getSources(), "$and"));
            and.addAll(getValueList(propertyFilterCondition.getValues(), "$and"));

        }
        result.put("$and", and);

        return result;


    }

    private List<BasicDBObject> getPropertyList(String property) {
        List<BasicDBObject> result = new ArrayList<BasicDBObject>();
        if (property!=null && property!="")
            result.add( new BasicDBObject("metadata.property", property));
        return result;
    }

    private List<BasicDBObject> getSourcedValueList(Map<String, String> sourcedValues) {
        List<BasicDBObject> result = new ArrayList<BasicDBObject>();

        for (Map.Entry<String, String> stringStringEntry : sourcedValues.entrySet()) {
            BasicDBObject elemMatch = produceElemMatch(stringStringEntry);
            result.add(elemMatch);
        }
        return result;
    }

    private BasicDBObject produceElemMatch(Map.Entry<String, String> stringStringEntry) {
        BasicDBObject result = new BasicDBObject();

        String key = stringStringEntry.getKey();
        Source s = null;
        if (key.contains(":")) {
            String[] split = key.split(":");
            s = Configurator.getDefaultConfigurator().getPersistence().getCache().getSource(split[0], split[1]);
        } else
            s = Configurator.getDefaultConfigurator().getPersistence().getCache().getSource(stringStringEntry.getKey());
        BasicDBObject elemMatch = new BasicDBObject();

        BasicDBObject content = new BasicDBObject();
        content.put("source", s.getId());
        content.put("value", stringStringEntry.getValue());
        elemMatch.put("$elemMatch", content);
        result.put("metadata.sourcedValues", elemMatch);
        return result;
    }

    private List<BasicDBObject> getValueList(List<String> values, String andOr) {
        List<BasicDBObject> result = new ArrayList<BasicDBObject>();
        if (values!=null && values.size()>0)
            result.add( getAndOrList(values, "metadata.sourcedValues.value",andOr));
        return result;
    }

    private List<BasicDBObject> getSourceList(List<String> sources, String andOr) {
        List<BasicDBObject> result = new ArrayList<BasicDBObject>();
        if (sources!=null && sources.size()>0)
            result.add(getAndOrList(sources, "metadata.sourcedValues.source",andOr));
        return result;
    }



    private List<BasicDBObject> getStatusList(List<String> statuses, String andOr) {
        List<BasicDBObject> result = new ArrayList<BasicDBObject>();
        if (statuses!=null && statuses.size()>0)
            result.add(getAndOrList(statuses, "status", "$and"));
        return result;
    }

    private BasicDBObject getAndOrList(List<String> list, String expression, String andOr) {
        if (list == null || list.size() == 0)
            return null;
        List<BasicDBObject> basicList = new ArrayList<BasicDBObject>();
        for (String status : list) {
            basicList.add(new BasicDBObject(expression, status));
        }
        BasicDBObject result = new BasicDBObject(andOr, basicList);
        return result;

    }


    /**
     * Wraps the field within a metadata.[field].values if necessary, so that it
     * corresponds to the current element structure.
     *
     * @param f the field to wrap
     * @return the wrapped field.
     */
    public String mapFieldToProperty(String f, Object value) {
        if (Arrays.asList(EXCLUDE).contains(f)) {
            return f;
        }

        String result = f;

        if (value.equals(EXISTS)) {
            return result;
        } else if (value.equals("CONFLICT")) {
            return result + ".status";
        } else {
            return result + ".values";
        }

     /* if (value.getClass().isArray() && ((Object[]) value).length>1){
        return result + ".values";
      }
      else
        return result + ".value";*/
        // }
    }

    /**
     * Gets a {@link DBObject} that represents an or condition with all the values
     * of the given field.
     *
     * @param conditions the filter conditions to look at.
     * @param field      the field that has to be or concatenated.
     * @return the or condition.
     */
    private BasicDBObject getOrQuery(List<FilterCondition> conditions, String field) {
        List<BasicDBObject> or = new ArrayList<BasicDBObject>();

        for (FilterCondition fc : conditions) {
            if (field.equals(fc.getField())) {
                List<BasicDBObject> val = this.getAndQuery(field, fc);
                or.addAll(val);
            }
        }

        return new BasicDBObject("$or", or);
    }

    /**
     * Gets the distinct fields and the number of values for these fields within
     * the passed conditions.
     *
     * @param conditions the conditions to look at.
     * @return a map of the distinct fields and the number of occurrences.
     */
    private Map<String, Integer> getDistinctFields(List<FilterCondition> conditions) {
        Map<String, Integer> distinctFields = new HashMap<String, Integer>();

        for (FilterCondition fc : conditions) {
            Integer integer = distinctFields.get(fc.getField());
            int res = (integer == null) ? 0 : integer;
            distinctFields.put(fc.getField(), ++res);
        }

        return distinctFields;
    }

    /**
     * Gets the first value for a given field.
     *
     * @param conditions the conditions to look at.
     * @param field      the field to look at.
     * @return the value or null.
     */
    private List<BasicDBObject> getAndQuery(String field, FilterCondition... conditions) {
        for (FilterCondition fc : conditions) {
            if (fc.getField().equals(field)) {

                Object val = fc.getValue();
                List<BasicDBObject> res = new ArrayList<BasicDBObject>();
                if (fc instanceof BetweenFilterCondition) {
                    BetweenFilterCondition bfc = (BetweenFilterCondition) fc;
                    String mappedField = this.mapFieldToProperty(field, new Object());
                    BasicDBObject low = new BasicDBObject(mappedField, getBoundQuery(bfc.getLOperator(), bfc.getLValue()));
                    BasicDBObject high = new BasicDBObject(mappedField, getBoundQuery(bfc.getHOperator(), bfc.getHValue()));

                    List<BasicDBObject> and = new ArrayList<BasicDBObject>();
                    and.add(low);
                    and.add(high);
                    res.add(new BasicDBObject("$and", and));

                } else {
                    if (val == null || val.equals("Unknown")) {
                        val = NOTEXISTS;
                        res.add(new BasicDBObject(this.mapFieldToProperty(field, val), val));
                    } else {
                        if (val.getClass().isArray()) {
                            Object[] valArray = (Object[]) val;
                            if (valArray.length > 1)
                                res.add(new BasicDBObject(this.mapFieldToProperty(field, val), valArray));
                            else {
                                res.add(new BasicDBObject(this.mapFieldToProperty(field, val), valArray[0]));
                            }
                        } else {
                            if (Arrays.asList(EXCLUDE).contains(field)) {
                                res.add(new BasicDBObject(field, val));
                            } else {
                                if (val.equals("CONFLICT")) {
                                    field += ".status";
                                    res.add(new BasicDBObject(field, val));
                                } else {
                                    res.add(new BasicDBObject(field + ".values", val));
                                }
                            }

/*

                if (val !=null) {
                BasicDBObject elemMatch = new BasicDBObject("$elemMatch", val);
                res.add(new BasicDBObject(this.mapFieldToProperty(field, val), elemMatch));
              } else {
                res.add(new BasicDBObject(this.mapFieldToProperty(field, val), val));

              }*/


                        }
                    }
                }
                return res;
            }

        }

        return null;
    }

    /**
     * Retrieves a bound query with the given operator and value.
     *
     * @param op  the operator to use.
     * @param val the value to use.
     * @return the mongo query.
     */
    private BasicDBObject getBoundQuery(Operator op, Object val) {
        String operator = "";
        switch (op) {
            case GT:
                operator = "$gt";
                break;
            case GTE:
                operator = "$gte";
                break;
            case LT:
                operator = "$lt";
                break;
            case LTE:
                operator = "$lte";
                break;
        }

        return new BasicDBObject(operator, val);
    }
}
